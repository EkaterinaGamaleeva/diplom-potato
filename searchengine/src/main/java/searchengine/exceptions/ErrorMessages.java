package searchengine.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorMessages {

    SITE_IS_UNAVAILABLE("Ошибка индексации: главная страница сайта не доступна"),
    MAIN_PAGE_IS_UNAVAILABLE("Ошибка индексации: сайт не доступен"),
    NO_ERROR(""),
    STOPPED_BY_THE_USER("Индексация остановлена пользователем"),
    INDEXING_ALREADY_STARTED("Индексация уже запущена"),
    INDEXING_NOT_STARTED("Индексация не запущена"),
    PAGE_NOT_FOUND("Данная страница находится за пределами сайтов, указанных в конфигурационном файле"),
    EMPTY_SEARCH("Задан пустой поисковый запрос"),
    NOT_FOUND("Ничего не найдено");
    private final String value;

}
