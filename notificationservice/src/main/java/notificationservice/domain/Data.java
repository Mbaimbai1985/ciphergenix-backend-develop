package notificationservice.domain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Data {
    private String name;
    private String email;
    private String token;
    private String ticketTitle;
    private String ticketNumber;
    private String priority;
    private String comment;
    private String date;
    private String files;
}