package io.github.fifi2.chessmorize.error.exception.lichess;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class LichessException extends RuntimeException {

    private final String message;
    private final HttpStatus status;
    private final Throwable cause;

}
