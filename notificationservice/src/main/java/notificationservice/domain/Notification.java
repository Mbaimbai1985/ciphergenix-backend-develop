package notificationservice.domain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notificationservice.event.Event;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Map;
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification implements Serializable {
    private Event payload;
    private Map<String, String> headers;

    public boolean equals(@Nullable Object other) {
        boolean var10000;
        if (this != other) {
            label28: {
                if (other instanceof Notification) {
                    var that = (Notification)other;
                    if (ObjectUtils.nullSafeEquals(this.payload, that.payload) && this.headers.equals(that.headers)) {
                        break label28;
                    }
                }
                var10000 = false;
                return var10000;
            }
        }
        var10000 = true;
        return var10000;
    }

    public int hashCode() {
        return ObjectUtils.nullSafeHash(this.payload, this.headers);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append(" [payload=");
        Object var3 = this.payload;
        if (var3 instanceof byte[] bytes) {
            sb.append("byte[").append(bytes.length).append(']');
        } else {
            sb.append(this.payload);
        }
        sb.append(", headers=").append(this.headers).append(']');
        return sb.toString();
    }
}