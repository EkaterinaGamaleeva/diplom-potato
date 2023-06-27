package searchengine.services.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.IndexState;
import searchengine.model.SiteModel;
import searchengine.services.SiteModelService;
import searchengine.services.index.PageIndexerHandlerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
@RequiredArgsConstructor
@Log4j2
public class AsyncParserServiceImpl implements AsyncParserService {
    private final SiteModelService siteModelService;
    private final ObjectProvider<WebsiteParser> provider;
    private final ParseState parseState;
    private final IndexState state;
    private final PageIndexerHandlerImpl indexer;


    @Async
    @Override
    public void startIndexingPages(SitesList list) {
        parseState.setState(false);
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<WebsiteParser> parsers = initService(list);
        try {
            for (Future<SiteModel> siteModelFuture : executorService.invokeAll(parsers)) {
                siteModelService.save(siteModelFuture.get());
            }
            executorService.shutdown();
            state.setIndexing(false);
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void startIndexingPage(String url, SiteModel siteModel) {
        indexer.indexPage(url, siteModel);
    }


    @Async
    @Override
    public void stopIndexing() {
        parseState.setState(true);
    }


    private List<WebsiteParser> initService(SitesList list) {
        List<WebsiteParser> parsers = new ArrayList<>();
        list.getSites().forEach(site -> provider.forEach(service -> {
            service.setUrl(site.getUrl());
            service.setSiteModel(siteModelService.reSaveSite(site));
            parsers.add(service);
        }));
        return parsers;
    }
}

