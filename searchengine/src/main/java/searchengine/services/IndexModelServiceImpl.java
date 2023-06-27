package searchengine.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.IndexRepository;
import searchengine.services.parser.ParseState;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Scope("prototype")
@Log4j2
public class IndexModelServiceImpl implements IndexModelService {
    private final IndexRepository indexRepository;
    private final LemmaModelService lemmaModelService;
    private final ParseState parseState;

    public IndexModelServiceImpl(IndexRepository indexRepository,
                                 @Lazy LemmaModelService lemmaModelService,
                                 ParseState parseState) {
        this.indexRepository = indexRepository;
        this.lemmaModelService = lemmaModelService;
        this.parseState = parseState;
    }

    @Override
    public Set<Index> save(List<Lemma> lemmas, PageModel pageModel) {
        if (parseState.isStopped()) {
            return new HashSet<>();
        }
        return collect(lemmas, pageModel);
    }

    @Override
    public Set<Index> collect(List<Lemma> lemmas, PageModel pageModel) {
        return lemmas
                .parallelStream()
                .map(lemma -> init(pageModel, lemma))
                .collect(Collectors.toSet());
    }

    @Override
    public Index init(PageModel pageModel, Lemma lemma) {
        return Index.builder()
                .pageModel(pageModel)
                .lemma(lemma)
                .id(new Index.IndexKey(pageModel.getId(), lemma.getId()))
                .rank(lemma.getRank())
                .build();
    }

    @Override
    public void delete(PageModel pageModel, SiteModel siteModel) {
        Optional<List<Index>> optionalIndexList = indexRepository.findAllByPageModel(pageModel);
        optionalIndexList.ifPresent(indices -> lemmaModelService.deleteLemmas(updateLemmas(indices)));
    }

    private List<Lemma> updateLemmas(List<Index> indices) {
        return indices.stream()
                .map(Index::getLemma)
                .peek(lemma -> lemma.setFrequency(lemma.getFrequency() - 1))
                .filter(lemma -> lemma.getFrequency() == 0)
                .toList();
    }

    @Override
    public List<Index> collectIndexesByPages(Set<PageModel> pages) {
        return pages.stream()
                .flatMap(page -> indexRepository.findAllByPageModel(page).stream())
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public Set<PageModel> findPagesByLemma(Lemma lem) {
        return indexRepository.findAllByLemma(lem)
                .stream()
                .map(Index::getPageModel)
                .collect(Collectors.toSet());
    }

    @Override
    public void saveAll(Set<Index> indices) {
        indexRepository.saveAllAndFlush(indices);
    }
}
