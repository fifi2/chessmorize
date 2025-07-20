package io.github.fifi2.chessmorize.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
@Schema(description = "Request to set the result of a training line")
public class TrainingResultRequest {

    @NotNull
    @Schema(
        description = "The book id",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d",
        requiredMode = REQUIRED)
    private UUID bookId;

    @NotNull
    @Schema(
        description = "The line id to save the result for",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d",
        requiredMode = REQUIRED)
    private UUID lineId;

    @NotNull
    @Schema(
        description = "The result of the training (true for success, false for failure)",
        example = "true",
        requiredMode = REQUIRED)
    private Boolean result;

}
