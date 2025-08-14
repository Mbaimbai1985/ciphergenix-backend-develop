package notificationservice.service;
import notificationservice.model.Message;

import java.util.List;
public interface NotificationService {
    Message sendMessage(String fromUserUuid, String toEmail, String subject, String message);
    List<Message> getMessages(String userUuid);
    List<Message> getConversations(String userUuid, String conversationId);
}