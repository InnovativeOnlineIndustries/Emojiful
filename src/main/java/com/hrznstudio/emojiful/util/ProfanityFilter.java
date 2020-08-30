package com.hrznstudio.emojiful.util;

import com.hrznstudio.emojiful.Emojiful;
import com.hrznstudio.emojiful.EmojifulConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfanityFilter {

    static Map<String, String[]> words = new HashMap<>();

    static int largestWordLength = 0;

    public static void loadConfigs() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://docs.google.com/spreadsheets/d/1Ufoero85kpr4caXLLaPpcgwB4tX44GgoGJ4F-bVfdI8/export?format=csv").openConnection().getInputStream()));
            String line = "";
            int counter = 0;
            while((line = reader.readLine()) != null) {
                counter++;
                String[] content = null;
                try {
                    content = line.split(",");
                    if(content.length == 0) {
                        continue;
                    }
                    String word = content[0];
                    String[] ignore_in_combination_with_words = new String[]{};
                    if(content.length > 1) {
                        ignore_in_combination_with_words = content[1].split("_");
                    }
                    if(word.length() > largestWordLength) {
                        largestWordLength = word.length();
                    }
                    words.put(word.replaceAll(" ", ""), ignore_in_combination_with_words);
                } catch(Exception e) {
                    Emojiful.LOGGER.catching(e);
                }
            }
            Emojiful.LOGGER.info("Loaded " + counter + " words to filter out");
        } catch (IOException e) {
            Emojiful.LOGGER.catching(e);
        }

    }

    public static ArrayList<String> badWordsFound(String input) {
        if(input == null) {
            return new ArrayList<>();
        }
        ArrayList<String> badWords = new ArrayList<>();

        // iterate over each letter in the word
        for (final String word : input.split(" ")) {
            for (int start = 0; start < word.length(); ++start) {
                for (int offset = 1; offset < word.length() + 1 - start && offset < largestWordLength; ++offset) {
                    String wordToCheck = word.substring(start, start + offset);
                    wordToCheck = wordToCheck.replaceAll("1","i")
                            .replaceAll("!","i")
                            .replaceAll("3","e")
                            .replaceAll("4","a")
                            .replaceAll("@","a")
                            .replaceAll("5","s")
                            .replaceAll("7","t")
                            .replaceAll("0","o")
                            .replaceAll("9","g");
                    wordToCheck = wordToCheck.toLowerCase().replaceAll("[^a-zA-Z]", "");
                    if(words.containsKey(wordToCheck)) {
                        // for example, if you want to say the word bass, that should be possible.
                        String[] ignoreCheck = words.get(wordToCheck);
                        boolean ignore = false;
                        for(int s = 0; s < ignoreCheck.length; s++ ) {
                            if(input.contains(ignoreCheck[s])) {
                                ignore = true;
                                break;
                            }
                        }
                        if(!ignore) {
                            badWords.add(input.substring(start, start + offset));
                        }
                    }
                }
            }
        }
        return badWords;

    }

    public static String filterText(String input) {
        List<String> badWords = badWordsFound(input);
        if(badWords.size() > 0) {
            for (String badWord : badWords) {
                input = input.replaceAll(EmojiUtil.cleanStringForRegex(badWord), EmojifulConfig.getInstance().profanityFilterReplacement.get());
            }
            return input;
        }
        return input;
    }
}
