package io.github.fifi2.chessmorize;

import io.github.fifi2.chessmorize.error.exception.BookNotFoundException;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.repository.BookRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractSpringBootTest {

    @Autowired
    protected BookRepository bookRepository;

    protected void saveBook(final Book book) {

        this.bookRepository.save(book)
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    protected Book getBook(final UUID bookId) {

        return this.bookRepository.findById(bookId)
            .block();
    }

    protected void assertNoBook(final UUID bookId) {

        this.bookRepository.findById(bookId)
            .as(StepVerifier::create)
            .expectError(BookNotFoundException.class)
            .verify();
    }

    protected void cleanDatabase() {

        this.bookRepository.deleteAll()
            .as(StepVerifier::create)
            .expectNextMatches(Boolean.TRUE::equals)
            .verifyComplete();
    }

}
