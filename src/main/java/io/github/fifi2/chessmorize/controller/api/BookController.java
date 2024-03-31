package io.github.fifi2.chessmorize.controller.api;

import com.fasterxml.jackson.annotation.JsonView;
import io.github.fifi2.chessmorize.controller.api.dto.BookCreationRequest;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
    path = "/api/books",
    produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BookController extends AbstractController {

    private final BookService bookService;

    @PostMapping
    public Mono<ResponseEntity<Book>> postBook(
        @RequestBody @Valid @NotNull final BookCreationRequest requestBody,
        @Autowired final ServerHttpRequest request) {

        return this.bookService.createBook(requestBody.getStudyId())
            .map(book -> ResponseEntity
                .created(URI.create("/api/books/" + book.getId()))
                .body(book))
            .doOnError(e -> logError(request, e));
    }

    @GetMapping(path = "/{bookId}")
    public Mono<ResponseEntity<Book>> getBook(
        @PathVariable @NotNull final UUID bookId,
        @Autowired final ServerHttpRequest request) {

        return this.bookService.getOneBook(bookId)
            .map(ResponseEntity::ok)
            .doOnError(e -> logError(request, e));
    }

    @GetMapping
    @JsonView(Views.BookList.class)
    public Mono<ResponseEntity<List<Book>>> getAllBooks(
        @Autowired final ServerHttpRequest request) {

        return this.bookService.getAllBooks()
            .collectList()
            .map(ResponseEntity::ok)
            .doOnError(e -> logError(request, e));
    }

    @DeleteMapping(path = "/{bookId}")
    public Mono<ResponseEntity<Void>> deleteBook(
        @PathVariable @NotNull final UUID bookId,
        @Autowired final ServerHttpRequest request) {

        return this.bookService.deleteOneBook(bookId)
            .map(dummy -> ResponseEntity.noContent().<Void>build())
            .doOnError(e -> logError(request, e));
    }

}
