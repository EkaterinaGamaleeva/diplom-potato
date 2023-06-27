package searchengine.services.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.exceptions.ErrorMessages;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.services.SiteModelService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Setter
@Getter
@Log4j2
public class WebsiteParser extends RecursiveAction implements Callable<SiteModel> {
    private final LinkParser linkParser;
    private final ObjectProvider<WebsiteParser> provider;
    private final Set<String> links;
    private final ParseState state;
    private final SiteModelService siteModelService;
    private String url;
    private SiteModel siteModel;

    @Override
    protected void compute() {
        if (state.isStopped()) {
            log.warn("Indexing is stopped " + Thread.currentThread().getName());
            siteModel.setStatus(Status.FAILED);
            siteModel.setLastError(ErrorMessages.STOPPED_BY_THE_USER.getValue());
            siteModel.setStatusTime(LocalDateTime.now());
            return;
        }
        siteModelService.updateTime(siteModel);
        List<WebsiteParser> taskList = new ArrayList<>();
        Set<String> links = linkParser.getLinks(url, siteModel);
        links.forEach(link -> initParser(taskList, link));
        taskList.forEach(ForkJoinTask::join);
    }

    private void initParser(List<WebsiteParser> taskList, String link) {
        provider.forEach(websiteParser -> {
            websiteParser.setUrl(link);
            websiteParser.setSiteModel(siteModel);
            taskList.add(websiteParser);
            websiteParser.fork();
        });
    }

    @Override
    public SiteModel call() {
        links.clear();
        compute();
        if (siteModel.getStatus() == Status.FAILED) {
            return siteModel;
        }
        siteModel.setStatusTime(LocalDateTime.now());
        siteModel.setStatus(Status.INDEXED);
        return siteModel;
    }
}
