package searchengine.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class ApiRequestException extends ResponseStatusException {
    private final FaultResponse faultResponse;

    public ApiRequestException(HttpStatus status, FaultResponse faultResponse) {
        super(status);
        this.faultResponse = faultResponse;
    }
}
