package searchengine.dto.search;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class SearchResponse {
    private final boolean result;
    private final int count;
    private final List<SearchData> data;
}
