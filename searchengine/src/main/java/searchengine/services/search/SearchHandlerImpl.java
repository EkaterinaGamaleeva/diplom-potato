package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResponse;
import searchengine.exceptions.ApiRequestException;
import searchengine.exceptions.ErrorMessages;
import searchengine.exceptions.FaultResponse;
import searchengine.model.Index;
import searchengine.model.PageModel;
import searchengine.services.IndexModelService;
import searchengine.services.PageModelService;
import searchengine.services.snippetcreator.SnippetCreator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class SearchHandlerImpl implements SearchHandler {
    private final PageModelService pageModelService;
    private final SnippetCreator snippetCreator;
    private final IndexModelService indexModelService;


    @Override
    public SearchResponse getResult(SearchRequestDto searchRequestDto) {
        String query = searchRequestDto.getQuery();
        if (query.isEmpty() || query.trim().isBlank()) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST,
                    new FaultResponse(false, ErrorMessages.EMPTY_SEARCH.getValue()));
        }
        return getSearchResponse(searchRequestDto);
    }


    @NotNull
    private SearchResponse getSearchResponse(SearchRequestDto searchRequestDto) {
        Set<PageModel> pageModels = pageModelService.getSearchQueryPages(searchRequestDto);
        Map<PageModel, Double> relevance = getRelativeRelevance(pageModels);
        List<PageModel> resultList = sortDesc(relevance);
        List<PageModel> pageable = getPageable(searchRequestDto, resultList);
        List<SearchData> dataList = collectDataList(searchRequestDto.getQuery(), relevance, pageable);
        return new SearchResponse(true, resultList.size(), dataList);
    }


    @NotNull
    private List<SearchData> collectDataList(String query, Map<PageModel, Double> rRel, List<PageModel> pageModels) {
        return pageModels
                .stream()
                .map(page -> initSearchData(query, rRel, page))
                .toList();
    }


    private SearchData initSearchData(String query, Map<PageModel, Double> rRel, PageModel pageModel) {
        return SearchData.builder()
                .title(Jsoup.parse(pageModel.getContent()).title())
                .site(pageModel.getSite().getUrl().replaceFirst("/$", ""))
                .uri(pageModel.getPath())
                .snippet(snippetCreator.getSnippet(pageModel.getContent(), query))
                .siteName(pageModel.getSite().getName())
                .relevance(rRel.get(pageModel))
                .build();
    }


    private List<PageModel> sortDesc(Map<PageModel, Double> rRel) {
        return rRel.entrySet()
                .stream()
                .sorted(Map.Entry.<PageModel, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    private Map<PageModel, Double> getAbsoluteRelevance(Set<PageModel> pageModels) {
        return indexModelService.collectIndexesByPages(pageModels).stream()
                .collect(Collectors.groupingBy(Index::getPageModel, Collectors.summingDouble(Index::getRank)));
    }


    private Map<PageModel, Double> getRelativeRelevance(Set<PageModel> pageModels) {
        Map<PageModel, Double> absoluteRelevance = getAbsoluteRelevance(pageModels);
        return absoluteRelevance.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        map -> map.getValue() / absoluteRelevance.values().stream().max(Double::compareTo).get()));
    }


    private List<PageModel> getPageable(SearchRequestDto searchRequestDto, List<PageModel> result) {
        return result.stream().skip(searchRequestDto.getOffset()).limit(searchRequestDto.getLimit()).toList();
    }
}