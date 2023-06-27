package searchengine.services.index;

import searchengine.model.SiteModel;

public interface PageIndexerHandler {
    void indexPages(String url, SiteModel siteModel);

    void indexPage(String url, SiteModel siteModel);
}
