package io.github.fifi2.chessmorize.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class LineNotFoundException extends RuntimeException {

    private final String message;

    public LineNotFoundException(final UUID bookId, final UUID lineId) {

        this.message = "Line " + lineId + " not found in Book " + bookId + ".";
    }

}
