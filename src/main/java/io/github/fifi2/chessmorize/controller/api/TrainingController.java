package io.github.fifi2.chessmorize.controller.api;

import io.github.fifi2.chessmorize.controller.api.dto.NextCalendarSlotRequest;
import io.github.fifi2.chessmorize.controller.api.dto.TrainingResultRequest;
import io.github.fifi2.chessmorize.error.exception.NoTrainingLineException;
import io.github.fifi2.chessmorize.model.Line;
import io.github.fifi2.chessmorize.service.TrainingService;
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

import java.util.UUID;

@RestController
@RequestMapping(
    path = "/api/training",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Training", description = "Training operations on books")
@RequiredArgsConstructor
public class TrainingController extends AbstractController {

    private final TrainingService trainingService;

    @GetMapping(path = "/next-line/{bookId}")
    @Operation(
        summary = "Get a line to train",
        description = "Get the next line to train from a book, from the book ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The next line to train"),
            @ApiResponse(responseCode = "204", description = "No more line to train"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or parameters"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public Mono<ResponseEntity<Line>> nextLine(
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
    @Operation(
        summary = "Set the result of a training line",
        description = "Save the result of a training to the line.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Result set successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or parameters"),
            @ApiResponse(responseCode = "404", description = "Book or Line not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public Mono<ResponseEntity<Line>> setResult(
        @RequestBody @Valid @NotNull final TrainingResultRequest requestBody,
        @Autowired final ServerHttpRequest request) {

        return this.trainingService.setLineResult(
                requestBody.getBookId(),
                requestBody.getLineId(),
                requestBody.getResult())
            .map(ResponseEntity::ok)
            .doOnError(e -> logError(request, e));
    }

    @PostMapping(path = "/next-calendar-slot")
    @Operation(
        summary = "Move to the next calendar slot",
        description = "Move the position in the training calendar to the next slot.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Result set successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or parameters"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public Mono<ResponseEntity<Void>> nextCalendarSlot(
        @RequestBody @Valid @NotNull final NextCalendarSlotRequest requestBody,
        @Autowired final ServerHttpRequest request) {

        return this.trainingService.nextCalendarSlot(requestBody.getBookId())
            .flatMap(book -> Mono.just(ResponseEntity.ok().<Void>build()))
            .doOnError(e -> logError(request, e));
    }

}
