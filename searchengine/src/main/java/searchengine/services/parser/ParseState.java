package searchengine.services.parser;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ParseState {
    private boolean state;

    public boolean isStopped() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
