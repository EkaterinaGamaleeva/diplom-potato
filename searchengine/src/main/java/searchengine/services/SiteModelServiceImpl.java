package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.exceptions.ApiRequestException;
import searchengine.exceptions.ErrorMessages;
import searchengine.exceptions.FaultResponse;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repository.SiteRepository;
import searchengine.services.connection.WebSiteConnection;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SiteModelServiceImpl implements SiteModelService {
    private final SiteRepository siteRepository;
    private final WebSiteConnection connection;
    private final SitesList sitesList;

    @Override
    public SiteModel reSaveSite(Site site) {
        SiteModel siteModel = init(site);
        siteRepository.findFirstByUrlIgnoreCase(siteModel.getUrl()).ifPresent(siteRepository::delete);
        siteRepository.save(siteModel);
        return siteModel;
    }

    @Override
    public void save(SiteModel siteModel) {
        siteRepository.saveAndFlush(siteModel);
    }

    @SneakyThrows
    @Override
    public SiteModel init(Site site) {
        return SiteModel.builder()
                .status(getResponseCode(site) >= 400 ? Status.FAILED : Status.INDEXING)
                .statusTime(LocalDateTime.now())
                .lastError(getResponseCode(site) >= 400 ? ErrorMessages.SITE_IS_UNAVAILABLE.getValue() : null)
                .url(site.getUrl())
                .name(site.getName())
                .pageModels(new HashSet<>())
                .lemmas(new HashSet<>())
                .build();
    }

    @Override
    @Async
    public void updateTime(SiteModel siteModel) {
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    @Override
    public Optional<SiteModel> matchAndGetModel(String url) {
        if (getSite(url).isEmpty()) {
            return Optional.empty();
        }
        Optional<SiteModel> siteModel = getSite(url)
                .flatMap(site -> siteRepository.findFirstByUrlIgnoreCase(site.getUrl()));
        if (siteModel.isEmpty()) {
            return Optional.of(siteRepository.save(init(getSite(url).get())));
        }
        return siteModel;
    }

    @Override
    public SiteModel findSiteByUrl(String site) {
        Optional<SiteModel> siteOptional = siteRepository.findFirstByUrlIgnoreCase(site);
        return siteOptional.orElse(null);
    }

    private Optional<Site> getSite(String url) {
        return Optional.ofNullable(sitesList.getSites().stream()
                .filter(site -> getHost(site.getUrl()).equals(getHost(url)))
                .findAny()
                .orElseThrow(() -> new ApiRequestException(HttpStatus.NOT_FOUND,
                        new FaultResponse(false, ErrorMessages.PAGE_NOT_FOUND.getValue()))));
    }

    private String getHost(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new ApiRequestException(HttpStatus.NOT_FOUND,
                    new FaultResponse(false, ErrorMessages.PAGE_NOT_FOUND.getValue()));
        }
    }

    private int getResponseCode(@NotNull Site site) {
        return connection.getHTMLDocument(site.getUrl()).connection().response().statusCode();
    }


}
