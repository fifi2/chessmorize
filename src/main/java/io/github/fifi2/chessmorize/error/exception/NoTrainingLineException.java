package io.github.fifi2.chessmorize.error.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class NoTrainingLineException extends RuntimeException {

    private final String message;

    public NoTrainingLineException(final UUID bookId) {

        this.message = "No line to train in book " + bookId.toString();
    }

}
