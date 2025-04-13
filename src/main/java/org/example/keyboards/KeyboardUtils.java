package org.example.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class KeyboardUtils {
    public List<List<InlineKeyboardButton>> buildCalendar(LocalDate startDate) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.from(startDate);

        List<InlineKeyboardButton> headerRow = new ArrayList<>();
        headerRow.add(createButton(month.getMonth().toString() + " " + month.getYear(), "noop"));
        keyboard.add(headerRow);

        List<InlineKeyboardButton> daysRow = new ArrayList<>();
        for (String day : new String[]{"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"}) {
            daysRow.add(createButton(day, "noop"));
        }
        keyboard.add(daysRow);

        LocalDate firstOfMonth = month.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 = Monday
        List<InlineKeyboardButton> weekRow = new ArrayList<>();

        for (int i = 0; i < dayOfWeek; i++) {
            weekRow.add(createButton(" ", "noop"));
        }

        int maxDays = month.lengthOfMonth();
        for (int day = 1; day <= maxDays; day++) {
            LocalDate date = month.atDay(day);
            if (date.isBefore(today)) {
                weekRow.add(createButton(String.valueOf(day), "noop")); // Нельзя выбрать
            } else {
                weekRow.add(createButton(String.valueOf(day), "date_" + date.toString()));
            }
            if (weekRow.size() == 7) {
                keyboard.add(weekRow);
                weekRow = new ArrayList<>();
            }
        }

        while (weekRow.size() < 7) {
            weekRow.add(createButton(" ", "noop"));
        }
        if (!weekRow.isEmpty()) {
            keyboard.add(weekRow);
        }

        return keyboard;
    }

    public InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}