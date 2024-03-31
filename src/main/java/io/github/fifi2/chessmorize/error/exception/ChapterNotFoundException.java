package io.github.fifi2.chessmorize.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class ChapterNotFoundException extends RuntimeException {

    private final String message;

    public ChapterNotFoundException(final UUID bookId, final UUID chapterId) {

        this.message = "Chapter %s not found in Book %s.".formatted(
            chapterId,
            bookId);
    }

}
