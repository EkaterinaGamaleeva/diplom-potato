package searchengine.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Configuration
@RequiredArgsConstructor
public class AppConfiguration {

    @Bean(value = "links")
    public Set<String> getLinks() {
        return new CopyOnWriteArraySet<>();
    }

    @Bean(value = "lock")
    public ReentrantReadWriteLock getLockService() {
        return new ReentrantReadWriteLock();
    }

    @Bean
    @SneakyThrows
    public LuceneMorphology getluceneMorphology() {
        return new RussianLuceneMorphology();
    }

    @Bean
    @ConfigurationProperties(prefix = "indexing-settings")
    public SitesList siteList(SitesList sitesList) {
        return sitesList;
    }

    @Bean
    @ConfigurationProperties(prefix = "jsoup-connection-settings")
    public JsoupConfiguration getConfiguration(JsoupConfiguration configuration) {
        return configuration;
    }
}
