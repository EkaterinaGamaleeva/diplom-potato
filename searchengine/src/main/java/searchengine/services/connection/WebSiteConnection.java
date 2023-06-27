package searchengine.services.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.JsoupConfiguration;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Getter
@Setter
@Log4j2
public class WebSiteConnection {
    private final JsoupConfiguration getConfiguration;


    public Document getHTMLDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(getConfiguration.getUserAgent())
                    .referrer(getConfiguration.getReferrer())
                    .followRedirects(false)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
