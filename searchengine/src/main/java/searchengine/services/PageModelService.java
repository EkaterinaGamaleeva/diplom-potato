package searchengine.services;

import searchengine.dto.search.SearchRequestDto;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import java.util.Set;

public interface PageModelService {
    PageModel get(String url, SiteModel model);

    PageModel update(String url, SiteModel model);

    PageModel init(String url, SiteModel site);

    Set<PageModel> getSearchQueryPages(SearchRequestDto searchRequestDto);

}
