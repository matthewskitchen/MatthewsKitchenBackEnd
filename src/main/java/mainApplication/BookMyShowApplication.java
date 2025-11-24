package mainApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "services", "repository", "dbmodel", "configuration"})
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "dbmodel")
public class BookMyShowApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookMyShowApplication.class, args);
        System.out.println("🚀 Barakah Bites Server Started on Port 8081");
    }
}
