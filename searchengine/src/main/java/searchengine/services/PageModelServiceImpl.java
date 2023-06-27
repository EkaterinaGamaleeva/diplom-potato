package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchRequestDto;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.PageRepository;
import searchengine.services.connection.WebSiteConnection;
import searchengine.services.lemmatisator.LemmaFinder;
import searchengine.services.parser.ParseState;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PageModelServiceImpl implements PageModelService {
    private final PageRepository pageRepository;
    private final ParseState parseState;
    private final WebSiteConnection webSiteConnection;
    private final IndexModelService indexModelService;
    private final LemmaFinder lemmaFinder;
    private final LemmaModelService lemmaModelService;

    @Override
    public PageModel get(String url, SiteModel model) {
        PageModel pageModel = init(url, model);
        if (parseState.isStopped()) {
            return pageModel;
        }
        return pageRepository.save(pageModel);
    }

    @Override
    public PageModel update(String url, SiteModel siteModel) {
        PageModel pageModel = init(url, siteModel);
        Optional<PageModel> pageOptional = pageRepository.findFirstByPathAndSite(pageModel.getPath(), siteModel);
        pageOptional.ifPresent(model -> {
            pageRepository.deleteById(model.getId());
            indexModelService.delete(model, siteModel);
        });
        return pageRepository.save(pageModel);
    }

    @Override
    public PageModel init(String url, SiteModel site) {
        Document htmlDocument = webSiteConnection.getHTMLDocument(url);
        return PageModel.builder()
                .url(url)
                .path(URI.create(htmlDocument.location()).getPath())
                .site(site)
                .code(htmlDocument.connection().response().statusCode())
                .content(htmlDocument.html())
                .build();
    }

    @Override
    public Set<PageModel> getSearchQueryPages(SearchRequestDto searchRequestDto) {
        Map<Lemma, Double> queryLemmas = lemmaFinder.getSearchQueryLemma(searchRequestDto);
        Set<Lemma> lemmas = lemmaModelService.getLemmasByQuery(queryLemmas);
        Optional<Lemma> lemmaOptional = lemmas.stream().findFirst();
        return lemmaOptional.map(lemma -> getPagesBySearch(queryLemmas.keySet(), lemma)).orElse(Collections.emptySet());
    }

    @NotNull
    private Set<PageModel> getPagesBySearch(Set<Lemma> lemmas, Lemma lem) {
        Set<PageModel> result = indexModelService.findPagesByLemma(lem);
        lemmas.stream().map(lemma -> indexModelService.findPagesByLemma(lem)).forEach(result::retainAll);
        return result;
    }
}
