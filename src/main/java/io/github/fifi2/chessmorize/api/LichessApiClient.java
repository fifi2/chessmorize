package io.github.fifi2.chessmorize.api;

import io.github.fifi2.chessmorize.error.exception.lichess.*;
import io.github.resilience4j.retry.annotation.Retry;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;

@Component
@Slf4j
public class LichessApiClient {

    private static final String LICHESS_API_NAME = "lichess";

    private final WebClient webClient;

    public LichessApiClient(
        @Qualifier("lichessWebClient") final WebClient webClient) {

        this.webClient = webClient;
    }

    /**
     * Get a full study from Lichess as a PGN with one game per chapter.
     *
     * @param studyId is the study id.
     * @return A Mono of String for the PGN with all games
     * (application/x-chess-pgn).
     */
    @Retry(name = LICHESS_API_NAME)
    public Mono<String> getStudyPGN(final String studyId) {

        final Instant start = Instant.now();
        log.info("Getting study {} from Lichess", studyId);

        return this.webClient
            .get()
            .uri("/study/{studyId}.pgn", studyId)
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(pgn -> log.info(
                "Get study {} from Lichess in {} ms.",
                studyId,
                Duration.between(start, Instant.now()).toMillis()))
            .onErrorMap(
                WebClientResponseException.class,
                e -> {
                    final int code = e.getStatusCode().value();
                    final HttpStatus status = HttpStatus.resolve(code);
                    final String message = "Error "
                        + code
                        + " during call to Lichess";
                    log.error(message);

                    if (status == HttpStatus.NOT_FOUND)
                        return new LichessNotFoundException(studyId, e);
                    if (status == HttpStatus.REQUEST_TIMEOUT)
                        return new LichessTimeoutException(status, e);
                    if (e.getStatusCode().is4xxClientError())
                        return new Lichess4xxException(message, status, e);
                    return new Lichess5xxException(message, status, e);
                })
            .onErrorMap(
                isTimeoutException(),
                e -> new LichessTimeoutException(null, e))
            .switchIfEmpty(Mono.error(new LichessEmptyResponseException()));
    }

    /**
     * Returns a Predicate of Throwable able to recognize timeout exceptions.
     *
     * @return A Predicate of Throwable.
     */
    private Predicate<Throwable> isTimeoutException() {

        return e -> isTimeoutException(e)
            || (e instanceof WebClientRequestException && isTimeoutException(e.getCause()));
    }

    /**
     * Test if a Throwable is an instance of any king of timeout.
     *
     * @param e The Throwable to check.
     * @return A boolean: true if the Throwable is a timeout.
     */
    private boolean isTimeoutException(Throwable e) {

        return e instanceof ReadTimeoutException
            || e instanceof ConnectTimeoutException;
    }

}
