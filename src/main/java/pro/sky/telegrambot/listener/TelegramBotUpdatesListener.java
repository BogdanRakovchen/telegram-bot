package pro.sky.telegrambot.listener;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.Model.NotificationTask;
import pro.sky.telegrambot.Repository.NotificationTaskRepository;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
//    repository
    private final NotificationTaskRepository notificationTaskRepository;
//  object our class
    private NotificationTask notificationTaskObject;
    private TelegramBot telegramBot;
    private static final Pattern pattern1 = Pattern.compile("[А-Я|а-я]+");
    private static final Pattern pattern2 = Pattern.compile("[0-9\\.\\s:]+");
//    injections
    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskRepository notificationTaskRepository
                                      ) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }
    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
//          если сообщение старт, то отправляем приветственное сообщение
            if (update.message().text().contains("/start")) {
                SendMessage message = new SendMessage(update.message().chat().id(), "hello");
                telegramBot.execute(message);
            } else {
//          иначе обрабатываем полученное сообщение установленного типа
//                вызов функции matcherRegEx и вычление даты с удаление пробелов по краям
                String date = matcherRegEx("[0-9\\.\\s:]+", update).trim();
//                преобразуем дату
                LocalDateTime dateResult = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
//              вызов той же функции matcherRegEx, но уже для вычленения текста
                String notification = matcherRegEx("[А-Я|а-я]+", update).trim();
//              добавляем всё в экземпляр нашей сущности
//              сохранение в базе данных
                logger.info(dateResult + " " + notification);
                notificationTaskObject = new NotificationTask(
                    update.message().chat().id(),
                    notification,
                    dateResult
               );
                notificationTaskRepository.save(notificationTaskObject);
           }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
//    вспомогательная функция для обработки сообщения пользователя
    private static String matcherRegEx(String regex, Update update) {
        String input = update.message().text();
        Matcher matcher = pattern1.matcher(input);
        StringBuilder stringBuilder = new StringBuilder();
//        если значение даты
        if(regex.contains("[0-9\\.\\s:]+")) {
            input = update.message().text();
            matcher = pattern2.matcher(input);
            while (matcher.find()) {
                String matchedValue = matcher.group();
                return matchedValue;
            }
        }
        while (matcher.find()) {
            String matchedValue = matcher.group();
            stringBuilder.append(" " + matchedValue.trim());
        }
        String notificationResult = stringBuilder.toString();
        return notificationResult;
    }
    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String resultDate = "в " + time.getHour() + ":" +
                                   time.getMinute() + " " +
                                   time.getDayOfMonth() + " " +
                                   time.getMonth().getDisplayName(TextStyle.FULL, new Locale("ru")) + " " +
                                   time.getYear() + " года " + "\n" +
                  notificationTaskRepository.findByFirstnameContaining(LocalDateTime.now()
            .truncatedTo(ChronoUnit.MINUTES));
        String str = notificationTaskRepository.findByFirstnameContaining(LocalDateTime.now()
      .truncatedTo(ChronoUnit.MINUTES));
        if(!str.equals(null)) {
        SendMessage message = new SendMessage(notificationTaskObject.getIdChat(), 
        resultDate);
         telegramBot.execute(message);   
       }      
    }
    
}