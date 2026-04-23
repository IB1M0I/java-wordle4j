package ru.yandex.practicum;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {
    private List<String> words;

    private Set<String> wordSet; // HashSet для O(1) проверки contains()

    public WordleDictionary(List<String> words) {
        this.words = words;
        this.wordSet = new HashSet<>(words); // Создаем HashSet для быстрой проверки
    }

    // Получить размер словаря
    public int size() {
        return words.size();
    }

    // Получить слово по индексу
    public String get(int index) {
        return words.get(index);
    }

    public boolean contains(String word) {
        //А зачем если в Wordle:64 есть нормализвация?
        return wordSet.contains(word.toLowerCase().replace("ё", "е").trim());
    }

    public List<String> getAll() {
        return words;
    }


}
