package searchengine.services.lemmatisator;

import org.apache.lucene.morphology.LuceneMorphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import searchengine.dto.search.SearchRequestDto;
import searchengine.model.Lemma;
import searchengine.model.SiteModel;
import searchengine.services.LemmaModelService;
import searchengine.services.SiteModelService;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Component
public class LemmaFinder {
    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ", "МС", "МС-П", "ВВОДН"};
    private static final String EXCESS_TAGS = "<br>|<p>|&[a-z]+;";
    private final LuceneMorphology luceneMorphology;
    private final LemmaModelService lemmaModelService;
    private final SiteModelService siteModelService;

    public LemmaFinder(LuceneMorphology luceneMorphology,
                       @Lazy LemmaModelService lemmaModelService,
                       SiteModelService siteModelService) {
        this.luceneMorphology = luceneMorphology;
        this.lemmaModelService = lemmaModelService;
        this.siteModelService = siteModelService;
    }

    public Map<String, Integer> collect(String text) {
        Predicate<String> filter = getStringPredicate();
        Map<String, Integer> lemmas = new ConcurrentHashMap<>();
        Arrays.stream(arrayContainsRussianWords(htmlToText(text)))
                .filter(filter)
                .map(word -> luceneMorphology.getNormalForms(word).get(0))
                .forEach(word -> lemmas.merge(word, 1, Integer::sum));
        return lemmas;
    }

    @NotNull
    private Predicate<String> getStringPredicate() {
        return s -> !s.isEmpty()
                && hasParticleProperty(s)
                && !luceneMorphology.getNormalForms(s).isEmpty()
                && s.length() > 1;
    }

    public Map<String, String> collectLemmasAndQueryWord(String text) {
        Map<String, String> lemmas = new ConcurrentHashMap<>();

        Arrays.stream(arrayContainsRussianWords(htmlToText(text)))
                .filter(getStringPredicate())
                .forEach(s -> put(lemmas));
        return lemmas;
    }

    private void put(Map<String, String> lemmas) {
        Consumer<String> consumer = s -> {
            if (!luceneMorphology.getNormalForms(s).isEmpty()) {
                lemmas.put(luceneMorphology.getNormalForms(s).get(0), s);
            }
        };
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean hasParticleProperty(String value) {
        return luceneMorphology
                .getMorphInfo(value)
                .stream().noneMatch(word -> Arrays.stream(PARTICLES_NAMES)
                        .anyMatch(part -> word.toUpperCase().contains(part)));
    }

    public Map<Lemma, Double> getSearchQueryLemma(SearchRequestDto searchRequestDto) {
        String query = searchRequestDto.getQuery();
        String site = searchRequestDto.getSite();
        SiteModel siteModel = siteModelService.findSiteByUrl(site);
        Set<String> lemmas = collect(query).keySet();
        if (siteModel != null) {
            return lemmaModelService.getLemmaBySite(lemmas, siteModel);
        }
        return lemmaModelService.getLemmaAllSite(lemmas);
    }

    private String htmlToText(String html) {
        String body = html.replaceAll(EXCESS_TAGS, " ");
        return Jsoup.clean(
                body, "", Safelist.none(),
                new Document.OutputSettings().prettyPrint(true)
        ).trim();
    }
}
