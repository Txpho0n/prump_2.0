package org.example.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KeyboardUtils {

    public InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public List<List<InlineKeyboardButton>> buildCalendar(LocalDate date) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Форматтер для заголовка
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("ru"));

        // Кнопки переключения месяцев
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        navigationRow.add(createButton("<", "prev_" + date.minusMonths(1).toString()));
        navigationRow.add(createButton(date.format(monthFormatter), "noop")); // Просто текст без действия
        navigationRow.add(createButton(">", "next_" + date.plusMonths(1).toString()));
        keyboard.add(navigationRow);

        // Дни недели
        String[] daysOfWeek = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        List<InlineKeyboardButton> headerRow = new ArrayList<>();
        for (String day : daysOfWeek) {
            headerRow.add(createButton(day, "noop"));
        }
        keyboard.add(headerRow);

        // Дни месяца
        LocalDate firstDay = date.withDayOfMonth(1);
        int shift = firstDay.getDayOfWeek().getValue() - 1; // Смещение для начала недели (Пн = 0)
        int daysInMonth = date.lengthOfMonth();

        int dayCounter = 1 - shift; // Начинаем с учетом смещения
        for (int i = 0; i < 6; i++) { // Максимум 6 недель
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                if (dayCounter > 0 && dayCounter <= daysInMonth) {
                    String dayStr = String.valueOf(dayCounter);
                    row.add(createButton(dayStr, "date_" + date.withDayOfMonth(dayCounter).toString()));
                } else {
                    row.add(createButton(" ", "noop")); // Пустые клетки
                }
                dayCounter++;
            }
            keyboard.add(row);
            if (dayCounter > daysInMonth) break; // Прерываем, если месяц закончился
        }

        return keyboard;
    }
}
