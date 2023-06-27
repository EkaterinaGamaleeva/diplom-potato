package searchengine.services;

import org.jetbrains.annotations.NotNull;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public interface LemmaModelService {

    CompletableFuture<Set<Index>> indexPage(PageModel pageModel);

    Lemma init(PageModel pageModel, Map.Entry<String, Integer> map);

    @NotNull
    Set<Lemma> getLemmasByQuery(Map<Lemma, Double> queryLemmas);

    @NotNull
    Map<Lemma, Double> getLemmaAllSite(Set<String> lemmas);

    Map<Lemma, Double> getLemmaBySite(Set<String> lemmas, SiteModel siteModel);

    void deleteLemmas(List<Lemma> lemmas);

}
