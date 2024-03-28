package io.github.fifi2.chessmorize.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class BookNotFoundException extends RuntimeException {

    private final String message;

    public BookNotFoundException(final UUID bookId) {

        this.message = "Book " + bookId.toString() + " not found.";
    }

}
