package searchengine.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FaultResponse {
    private final boolean result;
    private final String error;
}
