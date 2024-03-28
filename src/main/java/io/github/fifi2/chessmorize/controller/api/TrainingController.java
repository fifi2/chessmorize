package io.github.fifi2.chessmorize.controller.api;

import io.github.fifi2.chessmorize.controller.api.dto.TrainingResultRequest;
import io.github.fifi2.chessmorize.error.exception.NoTrainingLineException;
import io.github.fifi2.chessmorize.model.Line;
import io.github.fifi2.chessmorize.service.TrainingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping(
    path = "/api/training/book/{bookId}",
    produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TrainingController extends AbstractController {

    private final TrainingService trainingService;

    @GetMapping(path = "/next-line")
    public Mono<ResponseEntity<Line>> getNextLine(
        @PathVariable @NotNull final UUID bookId,
        @Autowired final ServerHttpRequest request) {

        return this.trainingService.getNextLine(bookId)
            .map(ResponseEntity::ok)
            .onErrorResume(
                NoTrainingLineException.class,
                e -> Mono.just(ResponseEntity.noContent().build()))
            .doOnError(e -> logError(request, e));
    }

    @PostMapping(path = "/set-result")
    public Mono<ResponseEntity<Line>> setLineResult(
        @PathVariable @NotNull final UUID bookId,
        @RequestBody @Valid @NotNull final TrainingResultRequest requestBody,
        @Autowired final ServerHttpRequest request) {

        return this.trainingService.setLineResult(
                bookId,
                requestBody.getLineId(),
                requestBody.getResult())
            .map(ResponseEntity::ok)
            .doOnError(e -> logError(request, e));
    }

    @PostMapping(path = "/next-slot")
    public Mono<ResponseEntity<Void>> nextCalendarSlot(
        @PathVariable @NotNull final UUID bookId,
        @Autowired final ServerHttpRequest request) {

        return this.trainingService.nextCalendarSlot(bookId)
            .flatMap(book -> Mono.just(ResponseEntity.ok().<Void>build()))
            .doOnError(e -> logError(request, e));
    }

}
