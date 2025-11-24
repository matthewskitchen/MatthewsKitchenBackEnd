package dbmodel;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import repository.UserRepo;

@Entity
@Table(name = "users")
public class users {

    @Id
    private String email;
    private String password;
	private String role;
    private boolean enabled = false;
	


    public users() {}

    public users(String email, String password,String role) {
        this.email = email;
        this.password = password;
        this.role=role;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }  
    public void setRole(String role) { this.role = role; }


    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
