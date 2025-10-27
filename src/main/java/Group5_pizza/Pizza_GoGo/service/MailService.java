package Group5_pizza.Pizza_GoGo.service;

public interface MailService {
    void sendMail(String to, String subject, String text);
    void sendResetLink(String to, String resetUrl);
    void sendConfirmationLink(String to, String confirmUrl);  // Thêm
}