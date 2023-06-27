package searchengine.services.snippetcreator;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.services.lemmatisator.LemmaFinder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SnippetCreator {
    private final LemmaFinder finder;

    public String getSnippet(String html, String searchQuery) {
        String text = cleanText(html);
        List<String> searchWords = getSearchWords(text, searchQuery);
        List<String> strings = Arrays.stream(text.split("[.]")).toList();
        return substring(searchWords, strings);
    }

    @NotNull
    private String substring(List<String> searchWords, List<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String searchWord : searchWords) {
            for (String string : strings) {
                if (string.toLowerCase().contains(searchWord)) {
                    String splitText = getSplitText(searchWord, string.toLowerCase());
                    stringBuilder.append(splitText);
                    break;
                }
            }
        }
        return setBoldWordInText(searchWords, stringBuilder.toString());
    }

    private String setBoldWordInText(List<String> searchWords, String text) {
        return Arrays.stream(text.split("\\s")).map(str -> {
            for (String searchWord : searchWords) {
                if (str.toLowerCase().contains(searchWord)) {
                    String matchWord = str.toLowerCase();
                    String cleanWord = matchWord.replaceAll("[^" + matchWord + "]", "");
                    return "<b>".concat(cleanWord).concat("</b>");
                }
            }
            return str;
        }).collect(Collectors.joining(" "));
    }

    @NotNull
    private List<String> getSearchWords(String cleanText, String searchQuery) {
        Map<String, String> stringStringMap = finder.collectLemmasAndQueryWord(cleanText);
        Map<String, String> searchLemmas = finder.collectLemmasAndQueryWord(searchQuery);
        return searchLemmas
                .keySet().stream()
                .filter(stringStringMap::containsKey)
                .map(stringStringMap::get)
                .toList();
    }

    @NotNull
    private String cleanText(String html) {
        return Jsoup.parse(html).body().text();
    }

    private String getSplitText(String word, String cleanText) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = getMatcher(word, cleanText);
        if (matcher.find()) {
            builder.append(matcher.group(0).trim()).append(" ... ");
        }
        return builder.toString();
    }

    @NotNull
    private Matcher getMatcher(String word, String cleanText) {
        String regex = "(" + word + ".*?\\s?(\\s*.*?\\s){0,5})|(.*?\\s+){0,5}+(" + word + ".*?)\\s?(\\s*.*?\\s){0,5}";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        return pattern.matcher(cleanText);
    }
}
