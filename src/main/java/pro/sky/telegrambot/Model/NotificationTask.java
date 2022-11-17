package pro.sky.telegrambot.Model;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "notification_task")
@Component
public class NotificationTask {

    @Id
    private Long id;


    private String notifications;

    private LocalDateTime date;

    public NotificationTask() {

    }

    public NotificationTask(Long id, String notifications, LocalDateTime date) {
        this.id = id;
        this.notifications = notifications;
        this.date = date;

    }

    public Long getId() {
        return id;
    }
    public LocalDateTime getDate() {
        return date;
    }

    public String getNotifications() {
        return notifications;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTask that = (NotificationTask) o;
        return Objects.equals(id, that.id) && Objects.equals(notifications, that.notifications) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, notifications, date);
    }

}
