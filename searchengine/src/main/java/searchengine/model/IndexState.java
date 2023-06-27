package searchengine.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Data
@Component
@ToString
public class IndexState {
    private boolean isIndexing;
}
