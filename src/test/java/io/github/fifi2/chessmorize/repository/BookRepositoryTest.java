package io.github.fifi2.chessmorize.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fifi2.chessmorize.AbstractSpringBootTest;
import io.github.fifi2.chessmorize.error.exception.BookNotFoundException;
import io.github.fifi2.chessmorize.error.exception.BookSerDeException;
import io.github.fifi2.chessmorize.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

class BookRepositoryTest extends AbstractSpringBootTest {

    @Autowired
    private BookRepository bookRepository;

    @SpyBean
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {

        this.bookRepository.deleteAll().block();
    }

    @Test
    void save() {

        final UUID bookId = UUID.randomUUID();

        StepVerifier
            .create(this.insertBook(bookId))
            .expectNextMatches(book -> bookId.equals(book.getId()))
            .verifyComplete();
    }

    @Test
    void save_withSerializationFailure() throws JsonProcessingException {

        Mockito
            .when(this.objectMapper.writeValueAsString(Mockito.any()))
            .thenThrow(JsonProcessingException.class);

        StepVerifier
            .create(this.bookRepository.save(Book.builder()
                .id(UUID.randomUUID())
                .studyId("studyId")
                .name("name")
                .build()))
            .expectErrorMatches(error -> error instanceof BookSerDeException e
                && e.getCause() instanceof JsonProcessingException
                && "Json serialization failure".equals(e.getMessage()))
            .verify();
    }

    @Test
    void update() {

        final UUID bookId = UUID.randomUUID();

        StepVerifier
            .create(this.insertBook(bookId)
                .doOnNext(book -> book.setCalendarSlot(123))
                .flatMap(book -> this.bookRepository.update(book)))
            .expectNextMatches(book -> book.getCalendarSlot() == 123)
            .verifyComplete();
    }

    @Test
    void update_withNoBookFound() {

        StepVerifier
            .create(this.bookRepository.update(Book.builder()
                .id(UUID.randomUUID())
                .build()))
            .verifyComplete();
    }

    @Test
    void update_withSerializationFailure() throws JsonProcessingException {

        Mockito
            .when(this.objectMapper.writeValueAsString(Mockito.any()))
            .thenThrow(JsonProcessingException.class);

        StepVerifier
            .create(this.bookRepository.update(Book.builder()
                .id(UUID.randomUUID())
                .studyId("studyId")
                .name("name")
                .build()))
            .expectErrorMatches(error -> error instanceof BookSerDeException e
                && e.getCause() instanceof JsonProcessingException
                && "Json serialization failure".equals(e.getMessage()))
            .verify();
    }

    @Test
    void findById() {

        final UUID bookId = UUID.randomUUID();

        StepVerifier
            .create(this.insertBook(bookId)
                .flatMap(dummy -> this.bookRepository.findById(bookId)))
            .expectNextMatches(book -> bookId.equals(book.getId()))
            .verifyComplete();
    }

    @Test
    void findById_withBookNotFound() {

        StepVerifier
            .create(this.bookRepository.findById(UUID.randomUUID()))
            .expectError(BookNotFoundException.class)
            .verify();
    }

    @Test
    void findById_withDeserializationFailure() throws JsonProcessingException {

        Mockito
            .doThrow(JsonProcessingException.class)
            .when(this.objectMapper)
            .readValue(Mockito.anyString(), Mockito.eq(Book.class));

        StepVerifier
            .create(this.insertBook(UUID.randomUUID())
                .flatMap(book -> this.bookRepository.findById(book.getId())))
            .expectErrorMatches(error -> error instanceof BookSerDeException e
                && e.getCause() instanceof JsonProcessingException
                && "Json deserialization failure".equals(e.getMessage()))
            .verify();
    }

    @Test
    void findAll() {

        final UUID bookId1 = UUID.randomUUID();
        final UUID bookId2 = UUID.randomUUID();

        StepVerifier
            .create(this.insertBook(bookId1)
                .then(this.insertBook(bookId2))
                .thenMany(this.bookRepository.findAll()))
            .expectNextMatches(book -> bookId1.equals(book.getId()))
            .expectNextMatches(book -> bookId2.equals(book.getId()))
            .verifyComplete();
    }

    @Test
    void deleteById() {

        final UUID bookId = UUID.randomUUID();

        StepVerifier
            .create(this.insertBook(bookId)
                .flatMap(book -> this.bookRepository.deleteById(book.getId())))
            .expectNextMatches(bookId::equals)
            .verifyComplete();

        StepVerifier
            .create(this.bookRepository.findById(bookId))
            .expectError(BookNotFoundException.class)
            .verify();
    }

    @Test
    void deleteById_withBookNotFound() {

        StepVerifier
            .create(this.bookRepository.deleteById(UUID.randomUUID()))
            .expectError(BookNotFoundException.class)
            .verify();
    }

    @Test
    void deleteAll() {

        StepVerifier
            .create(this.insertBook(UUID.randomUUID())
                .then(this.insertBook(UUID.randomUUID()))
                .flatMap(dummy -> this.bookRepository.deleteAll()))
            .expectNextMatches(Boolean.TRUE::equals)
            .verifyComplete();

        StepVerifier
            .create(this.bookRepository.findAll())
            .verifyComplete();
    }

    private Mono<Book> insertBook(final UUID bookId) {

        return this.bookRepository.save(Book.builder()
            .id(bookId)
            .studyId(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .chapters(List.of())
            .build());
    }

}
