package searchengine.services.index;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.services.IndexModelService;
import searchengine.services.LemmaModelService;
import searchengine.services.PageModelService;
import searchengine.services.parser.ParseState;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Log4j2
public class PageIndexerHandlerImpl implements PageIndexerHandler {
    private final PageModelService pageModelService;
    private final ParseState parseState;
    private final ObjectProvider<LemmaModelService> provider;
    private final IndexModelService indexModelService;


    @Override
    public void indexPages(String url, SiteModel siteModel) {
        PageModel page = pageModelService.get(url, siteModel);
        if (isApproved(page)) {
            return;
        }
        saveIndexes(page);
    }

    @Override
    public void indexPage(String url, SiteModel siteModel) {
        PageModel page = pageModelService.update(url, siteModel);
        if (isApproved(page)) {
            return;
        }
        saveIndexes(page);
    }

    private void saveIndexes(PageModel page) {
        Set<Index> collect = provider
                .stream()
                .flatMap(lemmaModelService -> lemmaModelService.indexPage(page).join()
                        .stream())
                .collect(Collectors.toSet());
        indexModelService.saveAll(collect);
    }

    private boolean isApproved(PageModel pageModel) {
        return parseState.isStopped() || pageModel.getCode() >= 400;
    }
}
