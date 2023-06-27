package searchengine.services.search;

import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResponse;

public interface SearchHandler {
    SearchResponse getResult(SearchRequestDto searchRequestDto);
}
