package searchengine.dto.search;


import lombok.Data;

@Data
@SearchRequest()
public class SearchRequestDto {
    private String query;
    private String site;
    private int offset;
    private int limit;
}
