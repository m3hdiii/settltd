package io.settld.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ParserUtils {

    public static int numOfDotsCalculator(String content) {
        String[] splitArray = content.split("\\.", -1);
        return splitArray.length - 1;
    }

    public static int numOfWordsCalculator(String content) {
        int wordCount = 0;

        boolean word = false;
        int endOfLine = content.length() - 1;

        for (int i = 0; i < content.length(); i++) {
            if (Character.isLetter(content.charAt(i)) && i != endOfLine) {
                word = true;
            } else if (!Character.isLetter(content.charAt(i)) && word) {
                wordCount++;
                word = false;
            } else if (Character.isLetter(content.charAt(i)) && i == endOfLine) {
                wordCount++;
            }
        }
        return wordCount;
    }

    public static int numOfOccurrencesCalculator(String content, char occurrence) {
        String removedRepeated = content.replace(String.valueOf(occurrence), "");
        int count = content.length() - removedRepeated.length();
        return count;
    }

    public static String mostUsedWord(String content) {

        String words[] = content.toLowerCase().split(" ");
        Map<String, Integer> wordCountMap = new HashMap<>();
        for (String word : words) {
            if (wordCountMap.containsKey(word)) {
                wordCountMap.put(word, wordCountMap.get(word) + 1);
            } else {
                wordCountMap.put(word, 1);
            }
        }

        Optional<Map.Entry<String, Integer>> maxEntry = wordCountMap.entrySet()
                .parallelStream()
                .max(Comparator.comparing(Map.Entry::getValue));
        return maxEntry.get().getKey();
    }

    public static String removeSpaceTabAndNewLine(String str) {
        return str
                .replaceAll("(?m)^[ \t]*\r?\n", "")
                .replaceAll("(?m)(^\\s+|[\\t\\f ](?=[\\t\\f ])|[\\t\\f ]$|\\s+\\z)", "");
    }
}
