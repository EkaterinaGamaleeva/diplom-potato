package searchengine.services.index;

import searchengine.dto.index.IndexResponse;

public interface IndexHandler {
    IndexResponse startIndexing();

    IndexResponse stopIndexing();

    IndexResponse indexPage(String url);
}
