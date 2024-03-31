package io.github.fifi2.chessmorize.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fifi2.chessmorize.error.exception.BookNotFoundException;
import io.github.fifi2.chessmorize.error.exception.BookSerDeException;
import io.github.fifi2.chessmorize.model.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private static final String INSERT_ONE_BOOK = """
        INSERT INTO book (id, name, state)
        VALUES (:id, :name, :state)
        """;
    private static final String UPDATE_ONE_BOOK = """
        UPDATE book
        SET state = :state
        WHERE id = :id
        """;
    private static final String SELECT_ONE_BOOK = """
        SELECT state
        FROM book
        WHERE id = :id
        """;
    private static final String SELECT_ALL_BOOKS = """
        SELECT id, name
        FROM book
        """;
    private static final String DELETE_ONE_BOOK = """
        DELETE FROM book
        WHERE id = :id
        """;
    private static final String DELETE_ALL_BOOKS = "DELETE FROM book";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_STATE = "state";

    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;

    /**
     * Save a Book in database.
     *
     * @param book is the Book to persist.
     * @return a Mono of the saved Book.
     */
    public Mono<Book> save(final Book book) {

        return Mono.just(book)
            .map(this::convertToJson)
            .flatMap(state -> this.databaseClient
                .sql(INSERT_ONE_BOOK)
                .bind(FIELD_ID, book.getId())
                .bind(FIELD_NAME, book.getName())
                .bind(FIELD_STATE, state)
                .fetch()
                .rowsUpdated())
            .flatMap(rowsUpdated -> rowsUpdated > 0
                ? Mono.just(book)
                : Mono.empty());
    }

    /**
     * Update a Book in database.
     *
     * @param book is the Book to update.
     * @return a Mono of the updated Book (empty if no record has been updated).
     */
    public Mono<Book> update(final Book book) {

        return Mono.just(book)
            .map(this::convertToJson)
            .flatMap(state -> this.databaseClient
                .sql(UPDATE_ONE_BOOK)
                .bind(FIELD_ID, book.getId())
                .bind(FIELD_STATE, state)
                .fetch()
                .rowsUpdated())
            .flatMap(rowsUpdated -> rowsUpdated > 0
                ? Mono.just(book)
                : Mono.empty());
    }

    /**
     * Find a Book in database by its id.
     *
     * @param bookId is the book id.
     * @return A Mono of the Book.
     */
    public Mono<Book> findById(final UUID bookId) {

        return this.databaseClient
            .sql(SELECT_ONE_BOOK)
            .bind(FIELD_ID, bookId)
            .map(row -> row.get(FIELD_STATE, String.class))
            .one()
            .switchIfEmpty(Mono.error(new BookNotFoundException(bookId)))
            .map(this::readFromJson);
    }

    /**
     * Find all Books in database.
     *
     * @return A Flux of all Books.
     */
    public Flux<Book> findAll() {

        return this.databaseClient
            .sql(SELECT_ALL_BOOKS)
            .map(row -> Book.builder()
                .id(row.get(FIELD_ID, UUID.class))
                .name(row.get(FIELD_NAME, String.class))
                .build())
            .all();
    }

    /**
     * Delete a Book from database.
     *
     * @param bookId is the id of the Book to delete.
     * @return A Mono containing the id of the deleted Book.
     */
    public Mono<UUID> deleteById(final UUID bookId) {

        return this.databaseClient
            .sql(DELETE_ONE_BOOK)
            .bind(FIELD_ID, bookId)
            .fetch()
            .rowsUpdated()
            .flatMap(rowsUpdated -> rowsUpdated > 0
                ? Mono.just(bookId)
                : Mono.error(new BookNotFoundException(bookId)));
    }

    /**
     * Delete all books (for testing).
     *
     * @return A Mono of Boolean as the query success status.
     */
    Mono<Boolean> deleteAll() {

        return this.databaseClient
            .sql(DELETE_ALL_BOOKS)
            .fetch()
            .rowsUpdated()
            .map(rowsUpdated -> true)
            .onErrorReturn(false);
    }

    /**
     * Serialize a Book into a JSON
     *
     * @param book The Book
     * @return The JSON as a String
     */
    private String convertToJson(final Book book) {

        try {
            return objectMapper.writeValueAsString(book);
        } catch (JsonProcessingException e) {
            throw new BookSerDeException("Json serialization failure", e);
        }
    }

    /**
     * Map a JSON to a Book.
     *
     * @param json The json
     * @return A Book
     */
    private Book readFromJson(final String json) {

        try {
            return objectMapper.readValue(json, Book.class);
        } catch (JsonProcessingException e) {
            throw new BookSerDeException("Json deserialization failure", e);
        }
    }

}
