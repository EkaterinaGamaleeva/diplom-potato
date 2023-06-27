package searchengine.services.parser;

import searchengine.config.SitesList;
import searchengine.model.SiteModel;

public interface AsyncParserService {
    void startIndexingPages(SitesList list);

    void startIndexingPage(String url, SiteModel siteModel);

    void stopIndexing();
}
