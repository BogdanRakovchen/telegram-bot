package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.commons.lang3.RegExUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

//    repository
    private final NotificationTaskRepository notificationTaskRepository;
//   model
    private NotificationTask notificationTask;
//  object our class
    private NotificationTask notificationTaskObject;

    private TelegramBot telegramBot;

//    injections

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskRepository notificationTaskRepository,
                                      NotificationTask notificationTask) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
        this.notificationTask = notificationTask;
    }


    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }


    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
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
                 notificationTaskObject = new NotificationTask(
                        update.message().chat().id(),
                         notification,
                        dateResult
                );
//              сохранение в базе данных
                notificationTaskRepository.save(notificationTaskObject);

            }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

//    вспомогательная функция для обработки сообщения пользователя
    private String matcherRegEx(String regex, Update update) {

        String input = update.message().text();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuilder stringBuilder = new StringBuilder();

//        если значение даты
        if(regex.contains("[0-9\\.\\s:]+")) {
            input = update.message().text();
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(input);

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

        Collection<NotificationTask> notificationTaskCollection = notificationTaskRepository.findAll();

        boolean equalsTime = notificationTaskCollection.stream()
               .anyMatch(e -> e.getDate().toString()
                       .contentEquals(LocalDateTime.now()
                               .truncatedTo(ChronoUnit.MINUTES)
                               .toString()));

        NotificationTask notification = notificationTaskObject;

        String resultDate = "в " + notification.getDate().getHour() + ":" +
                notification.getDate().getMinute() + " часов " +
                notification.getDate().getDayOfMonth() + " " +
                notification.getDate().getMonth().getDisplayName(TextStyle.FULL, new Locale("ru")) + " " +
                notification.getDate().getYear() + " года " +
                notificationTaskObject.getNotifications();

        logger.info(resultDate);

        if(equalsTime) {
            SendMessage message = new SendMessage(notificationTaskObject.getId(), resultDate);
            SendResponse response = telegramBot.execute(message);
            logger.info(response.isOk() ? "true" : "false");
        }


    }




    }



