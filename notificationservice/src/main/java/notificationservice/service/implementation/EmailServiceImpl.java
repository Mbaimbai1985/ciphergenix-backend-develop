package notificationservice.service.implementation;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notificationservice.exception.ApiException;
import notificationservice.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.Map;

import static notificationservice.utils.NotificationUtils.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    public static final String NEW_USER_ACCOUNT_VERIFICATION = "New Account Verification";
    public static final String UTF_8_ENCODING = "UTF-8";
    public static final String ACCOUNT_VERIFICATION_TEMPLATE = "newaccount";
    public static final String PASSWORD_RESET_TEMPLATE = "resetpassword";
    public static final String NEW_TICKET_TEMPLATE = "newticket";
    public static final String NEW_COMMENT_TEMPLATE = "newcomment";
    public static final String NEW_FILE_TEMPLATE = "newfile";
    public static final String NEW_TICKET_REQUEST = "New Support Ticket";
    public static final String PASSWORD_RESET_REQUEST = "Password Reset Request";
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendNewAccountHtmlEmail(String name, String to, String token) {
        try {
            var context = new Context();
            context.setVariables(Map.of("name", name, "url", getVerificationUrl(host, token)));
            var text = templateEngine.process(ACCOUNT_VERIFICATION_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setText(text, true);
            emailSender.send(message);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Unable to send email");
        }
    }

    @Override
    @Async
    public void sendPasswordResetHtmlEmail(String name, String to, String token) {
        try {
            var context = new Context();
            context.setVariables(Map.of("name", name, "url", getResetPasswordUrl(host, token)));
            var text = templateEngine.process(PASSWORD_RESET_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject(PASSWORD_RESET_REQUEST);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setText(text, true);
            emailSender.send(message);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Unable to send email");
        }
    }

    @Override
    @Async
    public void sendNewTicketHtmlEmail(String name, String email, String ticketTitle, String ticketNumber, String priority) {
        try {
            var context = new Context();
            context.setVariables(Map.of("name", name, "priority", priority, "ticketTitle", ticketTitle, "ticketNumber", "Ticket #" + ticketNumber.toUpperCase().split("-")[4], "date", new Date(), "url", getTicketUrl(host, ticketNumber)));
            var text = templateEngine.process(NEW_TICKET_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject(NEW_TICKET_REQUEST);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setText(text, true);
            emailSender.send(message);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Unable to send email");
        }
    }

    @Override
    @Async
    public void sendNewFilesHtmlEmail(String name, String email, String files, String ticketTitle, String ticketNumber, String priority, String date) {
        try {
            var ticketNum = "Ticket #" + ticketNumber.toUpperCase().split("-")[4];
            var context = new Context();
            context.setVariables(Map.of("name", name, "priority", priority, "files", files.split(","), "ticketTitle", ticketTitle, "ticketNumber", ticketNum, "date", date, "url", getTicketUrl(host, ticketNumber)));
            var text = templateEngine.process(NEW_FILE_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject("New File Uploaded on " + ticketNum);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setText(text, true);
            emailSender.send(message);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Unable to send email");
        }
    }

    @Override
    @Async
    public void sendNewCommentHtmlEmail(String name, String email, String comment, String ticketTitle, String ticketNumber, String priority, String date) {
        try {
            var ticketNum = "Ticket #" + ticketNumber.toUpperCase().split("-")[4];
            var context = new Context();
            context.setVariables(Map.of("name", name, "priority", priority, "comment", comment, "ticketTitle", ticketTitle, "ticketNumber", ticketNum, "date", date, "url", getTicketUrl(host, ticketNumber)));
            var text = templateEngine.process(NEW_COMMENT_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject("New Comment on " + ticketNum);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setText(text, true);
            emailSender.send(message);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Unable to send email");
        }
    }

    private MimeMessage getMimeMessage() {
        return emailSender.createMimeMessage();
    }
}