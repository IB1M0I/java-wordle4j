package ru.yandex.practicum;

import java.util.*;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {
    public WordleDictionary(List<String> words) {
        this.words = words;
        this.wordSet = new HashSet<>(words); // Создаем HashSet для быстрой проверки
    }

    private List<String> words;
    private Set<String> wordSet; // HashSet для O(1) проверки contains()

    // Получить размер словаря
    public int size() {
        return words.size();
    }

    // Получить слово по индексу
    public String get(int index) {
        return words.get(index);
    }

    public boolean contains(String word) {
        return wordSet.contains(word); // O(1) проверка вместо O(n)
    }

    public List<String> getAll() {
        return words;
    }


}
