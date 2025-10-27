package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Override
    public void sendResetLink(String to, String resetUrl) {
        String subject = "Password Reset Request - Pizza GoGo";
        String text = "Hello,\n\nPlease click the link below to reset your password:\n"
                + resetUrl + "\n\nThis link will expire in 10 minutes.\n\nPizza GoGo Team";
        sendMail(to, subject, text);
    }

    @Override
    public void sendConfirmationLink(String to, String confirmUrl) {
        String subject = "Confirm Your Registration - Pizza GoGo";
        String text = "Hello,\n\nThank you for registering! Please click the link below to confirm your email:\n"
                + confirmUrl + "\n\nThis link will expire in 10 minutes.\n\nPizza GoGo Team";
        sendMail(to, subject, text);
    }
}