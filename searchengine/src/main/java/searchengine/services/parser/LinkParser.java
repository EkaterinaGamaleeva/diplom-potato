package searchengine.services.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.model.SiteModel;
import searchengine.services.connection.WebSiteConnection;
import searchengine.services.index.PageIndexerHandlerImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Component
@Scope("prototype")
@RequiredArgsConstructor
@Getter
public class LinkParser {
    private static final String REGEX = "[-\\w+/=~_|!:,.;]*[^#?]/?+";
    private static final String[] SUFFIX = new String[]{"jpeg", "jpg", "pdf", "doc", "docx", "mp4", "JPG", "png", "PDF"};
    private final Set<String> links;
    private final WebSiteConnection connection;
    private final PageIndexerHandlerImpl pageIndexerHandlerImpl;
    private final ParseState parseState;

    public Set<String> getLinks(String url, SiteModel site) {
        if (parseState.isStopped()) {
            log.warn("Indexing is stopped " + Thread.currentThread().getName());
            return new HashSet<>();
        }
        try {
            Thread.sleep(45);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        return collect(site, url);
    }

    private Set<String> collect(SiteModel site, String url) {
        return connection.getHTMLDocument(url).select("a")
                .parallelStream()
                .map(element -> element.attr("abs:href"))
                .distinct()
                .filter(link -> filter(site, link))
                .peek(log::info)
                .peek(link -> pageIndexerHandlerImpl.indexPages(link, site))
                .collect(Collectors.toSet());
    }

    private boolean filter(SiteModel site, String link) {
        return link.matches(site.getUrl() + REGEX)
                && !parseState.isStopped()
                && links.add(link)
                && Arrays.stream(SUFFIX).noneMatch(link::endsWith);
    }
}
