package searchengine.services;

import lombok.SneakyThrows;
import searchengine.config.Site;
import searchengine.model.SiteModel;

import java.util.Optional;

public interface SiteModelService {
    SiteModel reSaveSite(Site site);

    void save(SiteModel siteModel);

    @SneakyThrows
    SiteModel init(Site site);

    void updateTime(SiteModel siteModel);

    Optional<SiteModel> matchAndGetModel(String url);

    SiteModel findSiteByUrl(String site);
}
