package services;

import java.util.List;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import dbmodel.Order;
import dbmodel.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.OrderRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private OrderRepository orderRepo;

    // --------------------------------------------------------------------
    // 📌 GET REPORT FOR A SINGLE DAY
    // --------------------------------------------------------------------
    public Map<String, Object> getReportForDay(LocalDate date) {

        List<Order> ordersForDay = orderRepo.findAll().stream()
                // ✅ Skip orders with null orderedAt
                .filter(o -> o.getOrderedAt() != null)
                .filter(o -> o.getOrderedAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .equals(date))
                .collect(Collectors.toList());

        // Delivered only
        double deliveredRevenue = ordersForDay.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(o -> o.getFinalAmount().doubleValue())
                .sum();

        Map<String, Object> map = new HashMap<>();
        map.put("date", date.toString());
        map.put("deliveredRevenue", deliveredRevenue);
        map.put("totalDelivered", ordersForDay.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count());
        map.put("totalOrders", ordersForDay.size());

        return map;
    }

    // --------------------------------------------------------------------
    // 📌 GET REPORT FOR A MONTH — format: 2025-11
    // --------------------------------------------------------------------
    public Map<String, Object> getReportForMonth(String month) {

        YearMonth ym = YearMonth.parse(month); // ISO format YYYY-MM
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Order> monthlyOrders = orderRepo.findAll().stream()
                // ✅ Skip null orderedAt
                .filter(o -> o.getOrderedAt() != null)
                .filter(o -> {
                    LocalDate d = o.getOrderedAt()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return (!d.isBefore(start) && !d.isAfter(end));
                })
                .collect(Collectors.toList());

        // Delivered revenue
        double deliveredRevenue = monthlyOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(o -> o.getFinalAmount().doubleValue())
                .sum();

        Map<String, Object> map = new HashMap<>();
        map.put("month", month);
        map.put("deliveredRevenue", deliveredRevenue);
        map.put("totalDelivered", monthlyOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count());
        map.put("totalOrders", monthlyOrders.size());

        return map;
    }

    // --------------------------------------------------------------------
    // 📌 GENERIC DATE RANGE REPORT
    // --------------------------------------------------------------------
    public Map<String, Object> generateReport(LocalDate from, LocalDate to) {

        List<Order> orders = orderRepo.findAll().stream()
                // ✅ Skip null orderedAt
                .filter(o -> o.getOrderedAt() != null)
                .filter(o -> {
                    LocalDate d = o.getOrderedAt()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return (!d.isBefore(from) && !d.isAfter(to));
                })
                .collect(Collectors.toList());

        double deliveredRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(o -> o.getFinalAmount().doubleValue())
                .sum();

        Map<String, Object> report = new HashMap<>();
        report.put("from", from.toString());
        report.put("to", to.toString());
        report.put("deliveredRevenue", deliveredRevenue);
        report.put("totalOrders", orders.size());
        report.put("delivered", orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count());
        report.put("preparing", orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PREPARING)
                .count());

        return report;
    }

    // --------------------------------------------------------------------
    // 📌 PDF REPORT (Correct + Clean + Uses delivered only)
    // --------------------------------------------------------------------
    public byte[] generatePDFReport(LocalDate from, LocalDate to) {

        Map<String, Object> data = generateReport(from, to);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD);

            Paragraph title = new Paragraph("Mathews Kitchen - Sales Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("\nPeriod: " + from + " to " + to));
            doc.add(new Paragraph("\n"));

            // Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(70);

            table.addCell("Delivered Revenue");
            table.addCell("₹ " + data.get("deliveredRevenue"));

            table.addCell("Total Delivered Orders");
            table.addCell(data.get("delivered").toString());

            table.addCell("Total Orders");
            table.addCell(data.get("totalOrders").toString());

            doc.add(table);
            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
