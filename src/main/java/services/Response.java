package services;

public class Response {
    private String message;
    private String role;
    private int status;

    public Response() {}

    public Response(String message, String role, int status) {
        this.message = message;
        this.role = role;
        this.status = status;
    }

    // ✅ Getters & Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
