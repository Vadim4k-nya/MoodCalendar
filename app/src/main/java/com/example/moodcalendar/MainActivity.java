package com.example.moodcalendar;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private GridLayout calendarGridLayout;
    private ImageView moodEmojiImageView;
    private TextView currentMonthYearTextView;
    private Button prevMonthButton, nextMonthButton;

    private Calendar currentCalendar; // Для отслеживания текущего месяца
    private SharedPreferences sharedPreferences;

    // Массив цветов настроений, инициализируется в onCreate
    private int[] moodColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация UI элементов
        moodEmojiImageView = findViewById(R.id.moodEmojiImageView);
        currentMonthYearTextView = findViewById(R.id.currentMonthYearTextView);
        prevMonthButton = findViewById(R.id.prevMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);
        calendarGridLayout = findViewById(R.id.calendarGridLayout);

        // Инициализация SharedPreferences для хранения данных
        sharedPreferences = getSharedPreferences("MoodCalendarPrefs", MODE_PRIVATE);

        // Инициализация массива цветов настроений
        moodColors = new int[5];
        moodColors[0] = getResources().getColor(R.color.mood_very_sad);
        moodColors[1] = getResources().getColor(R.color.mood_sad);
        moodColors[2] = getResources().getColor(R.color.mood_neutral);
        moodColors[3] = getResources().getColor(R.color.mood_happy);
        moodColors[4] = getResources().getColor(R.color.mood_very_happy);


        currentCalendar = Calendar.getInstance(); // Устанавливаем текущий месяц
        setupCalendar();

        // Слушатели для кнопок переключения месяцев
        prevMonthButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1); // Переключаем на предыдущий месяц
            setupCalendar();
        });

        nextMonthButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1); // Переключаем на следующий месяц
            setupCalendar();
        });
    }

    // Настраивает и отображает календарь для текущего месяца
    private void setupCalendar() {
        // Очищаем все предыдущие представления из GridLayout
        calendarGridLayout.removeAllViews();

        // Устанавливаем текст для текущего месяца и года (например, "Июнь 2025")
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("ru", "RU"));
        currentMonthYearTextView.setText(monthYearFormat.format(currentCalendar.getTime()));

        // Генерируем список объектов Date для каждого дня в текущем месяце, включая пустые ячейки для выравнивания
        List<Date> daysInMonth = generateDaysInMonth(currentCalendar);

        // Определяем ширину ячейки, чтобы они равномерно распределялись по ширине
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        // Отступы 16dp слева и справа
        int cellWidthPx = (int) ((dpWidth - 32) / 7 * displayMetrics.density); // 32dp = 16dp padding on left + 16dp padding on right

        // Добавляем TextView для каждого дня в GridLayout
        for (Date day : daysInMonth) {
            TextView dayTextView = new TextView(this);
            // Применяем стиль CalendarDay
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textViewStyle, typedValue, true);
            dayTextView.setTextAppearance(this, typedValue.resourceId);
            dayTextView.setGravity(Gravity.CENTER);
            dayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            dayTextView.setText("26"); // Временный текст, будет обновлен

            // Установка LayoutParams для каждой ячейки
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cellWidthPx;
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, displayMetrics); // Высота 60dp
            params.setMargins(2, 2, 2, 2); // Небольшие отступы между ячейками
            dayTextView.setLayoutParams(params);


            if (day == null) {
                dayTextView.setText(""); // Пустая ячейка для дней предыдущего/следующего месяца
                dayTextView.setBackgroundColor(Color.TRANSPARENT); // Прозрачный фон
                dayTextView.setClickable(false);
            } else {
                SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
                dayTextView.setText(dayFormat.format(day));
                dayTextView.setClickable(true);
                dayTextView.setTag(day.getTime()); // Сохраняем миллисекунды даты в теге

                // Устанавливаем слушатель кликов
                dayTextView.setOnClickListener(v -> {
                    long clickedDateMillis = (long) v.getTag();
                    onDayClick(new Date(clickedDateMillis));
                });

                // Получаем настроение для текущего дня из SharedPreferences
                int mood = sharedPreferences.getInt("mood_" + day.getTime(), -1); // -1 если нет настроения
                if (mood != -1) {
                    setDayBackground(dayTextView, mood);
                } else {
                    // Устанавливаем фон по умолчанию
                    setDayBackground(dayTextView, -1); // Передаем -1 для дефолтного фона
                }
            }
            calendarGridLayout.addView(dayTextView);
        }
        // Обновляем общий смайлик для месяца
        updateOverallMoodEmoji();
    }


    // Генерирует список объектов Date для каждого дня в текущем месяце, включая пустые ячейки для выравнивания
    private List<Date> generateDaysInMonth(Calendar calendar) {
        List<Date> days = new ArrayList<>();
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1); // Устанавливаем на первый день месяца

        // Определяем день недели для 1-го числа (понедельник=1, воскресенье=7)
        // В Java Calendar Calendar.MONDAY = 2, Calendar.SUNDAY = 1
        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        // Рассчитываем смещение, чтобы календарь начинался с понедельника
        // Если 1-е число - понедельник, смещение = 0. Если вторник, смещение = 1 и т.д.
        int dayOffset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - Calendar.MONDAY;

        // Добавляем null-элементы для пустых ячеек (дней предыдущего месяца)
        for (int i = 0; i < dayOffset; i++) {
            days.add(null);
        }

        // Добавляем все дни текущего месяца
        int maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            tempCalendar.set(Calendar.DAY_OF_MONTH, i);
            days.add(tempCalendar.getTime());
        }
        return days;
    }

    // Обработчик клика по дню в календаре
    public void onDayClick(Date date) {
        if (date != null) {
            // Создаем Intent для перехода на MoodSelectionActivity
            Intent intent = new Intent(MainActivity.this, MoodSelectionActivity.class);
            // Передаем выбранную дату в миллисекундах
            intent.putExtra("selected_date_millis", date.getTime());
            startActivity(intent); // Запускаем активность
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем календарь при возвращении из MoodSelectionActivity
        // Это гарантирует, что цвета ячеек обновятся после сохранения настроения
        setupCalendar();
    }

    // Устанавливает фон для TextView дня календаря в зависимости от настроения
    private void setDayBackground(TextView dayTextView, int moodLevel) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics())); // 4dp radius
        drawable.setStroke((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()), Color.parseColor("#CCCCCC")); // 1dp stroke

        if (moodLevel >= 0 && moodLevel < moodColors.length) {
            drawable.setColor(moodColors[moodLevel]);
        } else {
            // Если настроение не задано (-1) или некорректно, используем цвет по умолчанию
            drawable.setColor(getResources().getColor(R.color.mood_default));
        }
        dayTextView.setBackground(drawable);
    }

    // Обновляет смайлик в верхней части экрана на основе среднего настроения за месяц
    private void updateOverallMoodEmoji() {
        int totalMood = 0;
        int moodCount = 0;

        // Определяем начало и конец текущего месяца для запроса к SharedPreferences
        Calendar startOfMonth = (Calendar) currentCalendar.clone();
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);

        Calendar endOfMonth = (Calendar) currentCalendar.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);
        endOfMonth.set(Calendar.MILLISECOND, 999);

        // Итерируем по всем дням месяца, чтобы собрать настроения
        Calendar tempCal = (Calendar) startOfMonth.clone();
        while (tempCal.getTimeInMillis() <= endOfMonth.getTimeInMillis()) {
            int mood = sharedPreferences.getInt("mood_" + tempCal.getTimeInMillis(), -1);
            if (mood != -1) {
                totalMood += mood;
                moodCount++;
            }
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (moodCount == 0) {
            moodEmojiImageView.setImageResource(R.drawable.ic_neutral_emoji); // Если нет данных, показываем нейтральный
            return;
        }

        // Вычисляем среднее настроение
        int averageMood = totalMood / moodCount;

        // Устанавливаем соответствующий смайлик
        switch (averageMood) {
            case 0: moodEmojiImageView.setImageResource(R.drawable.ic_very_sad_emoji); break;
            case 1: moodEmojiImageView.setImageResource(R.drawable.ic_sad_emoji); break;
            case 2: moodEmojiImageView.setImageResource(R.drawable.ic_neutral_emoji); break;
            case 3: moodEmojiImageView.setImageResource(R.drawable.ic_happy_emoji); break;
            case 4: moodEmojiImageView.setImageResource(R.drawable.ic_very_happy_emoji); break;
            default: moodEmojiImageView.setImageResource(R.drawable.ic_neutral_emoji); break;
        }
    }
}
