package notificationservice.service;
public interface EmailService {
    void sendNewAccountHtmlEmail(String name, String to, String token);
    void sendPasswordResetHtmlEmail(String name, String to, String token);
    void sendNewTicketHtmlEmail(String name, String email, String ticketTitle, String ticketNumber, String priority);
    void sendNewFilesHtmlEmail(String name, String email, String files, String ticketTitle, String ticketNumber, String priority, String date);
    void sendNewCommentHtmlEmail(String name, String email, String comment, String ticketTitle, String ticketNumber, String priority, String date);
}