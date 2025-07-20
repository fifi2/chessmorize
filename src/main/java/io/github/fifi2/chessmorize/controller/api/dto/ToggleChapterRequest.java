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
@Schema(description = "Request to enable or disable a chapter in a book")
public class ToggleChapterRequest {

    @NotNull
    @Schema(
        description = "The book id",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d",
        requiredMode = REQUIRED)
    private UUID bookId;

    @NotNull
    @Schema(
        description = "The chapter id to enable/disable",
        example = "f1e2d3c4-b5a6-7e8f-4a2b-9c1d2e3f4a5b",
        requiredMode = REQUIRED)
    private UUID chapterId;

    @NotNull
    @Schema(
        description = "The expected enabled state of the chapter",
        example = "false",
        requiredMode = REQUIRED)
    private Boolean enabled;

}
