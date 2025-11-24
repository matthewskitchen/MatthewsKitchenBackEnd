package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import services.ReportService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/admin/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // 📌 Single Day Report (yyyy-MM-dd)
    @GetMapping("/day")
    public Map<String, Object> getDayReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        return reportService.getReportForDay(date);
    }

    // 📌 Monthly Report (YYYY-MM)
    @GetMapping("/month")
    public Map<String, Object> getMonthReport(@RequestParam String month) {

        return reportService.getReportForMonth(month);
    }

    // 📌 Custom Date Range (yyyy-MM-dd)
    @GetMapping("/range")
    public Map<String, Object> getRangeReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {

        return reportService.generateReport(from, to);
    }

    // 📌 Download PDF
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPDF(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {

        byte[] pdfBytes = reportService.generatePDFReport(from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("report-" + from + "_to_" + to + ".pdf")
                        .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
