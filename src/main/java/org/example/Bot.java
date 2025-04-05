package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Bot extends TelegramLongPollingBot {




    @Override
    public String getBotUsername() {
        return "cu_algo_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String messageText = update.getMessage().getText();

            switch (messageText){
                case "/start":
                    // TODO: проверка есть ли пользователь в БД
                    // TODO: если нет, то добавить его в БД, попросить данные и дозаполнить БД
                    // TODO: если есть, то отправить приветственное сообщение c менюшкой
                    // TODO: первичная оценка ранга
                    break;

                case "/help":
                    // TODO: отправить сообщение с описание тем и команд или еще что-то
                    // TODO: отправка вопросов админу
                    break;

                case "/interview":
                    // TODO: тут все что связано с планированием тренировки
                    //  TODO: чтение темы из json
                    // именно вот здесь происходит выбор человека в партнеры
                    // шедулинг тоже здесь
                    break;
            }
        }
    }
}