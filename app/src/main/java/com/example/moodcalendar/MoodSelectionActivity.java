package com.example.moodcalendar; // Замените на имя вашего пакета

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodSelectionActivity extends AppCompatActivity {

    private TextView selectedDateTextView;
    private SeekBar moodSeekBar;
    private ImageView moodEmojiDisplayImageView;
    private Button saveMoodButton;

    private long selectedDateMillis; // Дата, переданная из MainActivity (в миллисекундах)
    private int currentMoodLevel = 2; // Начальное значение настроения (нейтральное: 0-очень грустно, 4-очень радостно)

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_selection);

        // Инициализация UI элементов
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        moodSeekBar = findViewById(R.id.moodSeekBar);
        moodEmojiDisplayImageView = findViewById(R.id.moodEmojiDisplayImageView);
        saveMoodButton = findViewById(R.id.saveMoodButton);

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences("MoodCalendarPrefs", MODE_PRIVATE);

        // Получаем дату из Intent, переданную из MainActivity
        if (getIntent().hasExtra("selected_date_millis")) {
            selectedDateMillis = getIntent().getLongExtra("selected_date_millis", 0);
            Date selectedDate = new Date(selectedDateMillis);
            // Форматируем дату для отображения
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru", "RU"));
            selectedDateTextView.setText(dateFormat.format(selectedDate));

            // Загружаем сохраненное настроение для этой даты, если оно есть
            loadMoodForSelectedDate();
        }

        // Устанавливаем слушатель для SeekBar для отслеживания изменений ползунка
        moodSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentMoodLevel = progress; // Обновляем текущий уровень настроения
                updateMoodEmoji(currentMoodLevel); // Обновляем отображаемый смайлик
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Вызывается, когда пользователь начинает взаимодействовать с ползунком
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Вызывается, когда пользователь отпускает ползунок
            }
        });

        // Инициализируем смайлик в соответствии с начальным или загруженным настроением
        updateMoodEmoji(currentMoodLevel);

        // Устанавливаем слушатель для кнопки сохранения
        saveMoodButton.setOnClickListener(v -> saveMood());
    }

    // Загружает настроение для выбранной даты из SharedPreferences
    private void loadMoodForSelectedDate() {
        // Ключ для SharedPreferences будет "mood_" + миллисекунды даты
        int existingMood = sharedPreferences.getInt("mood_" + selectedDateMillis, -1); // -1 если нет настроения
        if (existingMood != -1) {
            currentMoodLevel = existingMood;
            moodSeekBar.setProgress(currentMoodLevel);
            updateMoodEmoji(currentMoodLevel);
        } else {
            // Если настроения нет, устанавливаем нейтральное и обновляем UI
            currentMoodLevel = 2; // Нейтральное по умолчанию
            moodSeekBar.setProgress(currentMoodLevel);
            updateMoodEmoji(currentMoodLevel);
        }
    }

    // Обновляет отображаемый смайлик на основе переданного уровня настроения
    private void updateMoodEmoji(int moodLevel) {
        switch (moodLevel) {
            case 0: // Очень грустно
                moodEmojiDisplayImageView.setImageResource(R.drawable.ic_very_sad_emoji);
                break;
            case 1: // Грустно
                moodEmojiDisplayImageView.setImageResource(R.drawable.ic_sad_emoji);
                break;
            case 2: // Нейтрально
                moodEmojiDisplayImageView.setImageResource(R.drawable.ic_neutral_emoji);
                break;
            case 3: // Радостно
                moodEmojiDisplayImageView.setImageResource(R.drawable.ic_happy_emoji);
                break;
            case 4: // Очень радостно
                moodEmojiDisplayImageView.setImageResource(R.drawable.ic_very_happy_emoji);
                break;
        }
    }

    // Сохраняет выбранное настроение в SharedPreferences
    private void saveMood() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Сохраняем настроение по ключу, связанному с датой
        editor.putInt("mood_" + selectedDateMillis, currentMoodLevel);
        editor.apply(); // Применяем изменения

        Toast.makeText(this, "Настроение сохранено!", Toast.LENGTH_SHORT).show();
        finish(); // Закрываем текущую активность и возвращаемся к предыдущей
    }
}
