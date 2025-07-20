package io.github.fifi2.chessmorize.controller.api;

import io.github.fifi2.chessmorize.controller.api.dto.BookCreationRequest;
import io.github.fifi2.chessmorize.controller.api.dto.BookLight;
import io.github.fifi2.chessmorize.controller.api.dto.ToggleChapterRequest;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Books", description = "Create and manage books")
@RequiredArgsConstructor
public class BookController extends AbstractController {

    private final BookService bookService;

    @PostMapping
    @Operation(
        summary = "Create a book",
        description = "Create a book from a Lichess study.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
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
    @Operation(
        summary = "Get a book from its ID",
        description = "Retrieve a book and its information.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Book retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public Mono<ResponseEntity<Book>> getBook(
        @PathVariable @NotNull final UUID bookId,
        @Autowired final ServerHttpRequest request) {

        return this.bookService.getOneBook(bookId)
            .map(ResponseEntity::ok)
            .doOnError(e -> logError(request, e));
    }

    @GetMapping
    @Operation(
        summary = "Get all books",
        description = "Retrieve an overview of all books (id and name only).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No existing book"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public Mono<ResponseEntity<List<BookLight>>> getAllBooks(
        @Autowired final ServerHttpRequest request) {

        return this.bookService.getAllBooks()
            .map(book -> BookLight.builder()
                .id(book.getId())
                .name(book.getName())
                .build())
            .collectList()
            .map(list -> list.isEmpty()
                ? ResponseEntity.noContent().<List<BookLight>>build()
                : ResponseEntity.ok(list))
            .doOnError(e -> logError(request, e));
    }

    @DeleteMapping(path = "/{bookId}")
    @Operation(
        summary = "Delete a book",
        description = "Delete a book from its ID.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or parameters"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public Mono<ResponseEntity<Void>> deleteBook(
        @PathVariable @NotNull final UUID bookId,
        @Autowired final ServerHttpRequest request) {

        return this.bookService.deleteOneBook(bookId)
            .map(dummy -> ResponseEntity.noContent().<Void>build())
            .doOnError(e -> logError(request, e));
    }

    @PutMapping(path = "/toggle-chapter")
    @Operation(
        summary = "Enable or disable a chapter in a book",
        description = "Disabling a chapter will remove the related lines from the book.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Chapter toggled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or parameters"),
            @ApiResponse(responseCode = "404", description = "Book or chapter not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public Mono<ResponseEntity<Void>> toggleChapter(
        @RequestBody @Valid @NotNull final ToggleChapterRequest requestBody,
        @Autowired final ServerHttpRequest request) {

        return this.bookService.toggleChapter(
                requestBody.getBookId(),
                requestBody.getChapterId(),
                requestBody.getEnabled())
            .map(dummy -> ResponseEntity.noContent().<Void>build())
            .doOnError(e -> logError(request, e));
    }

}
