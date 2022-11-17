package pro.sky.telegrambot.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.Model.NotificationTask;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

}
