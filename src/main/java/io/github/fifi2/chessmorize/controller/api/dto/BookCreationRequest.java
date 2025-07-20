package io.github.fifi2.chessmorize.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
@Schema(description = "Request to create a book from a Lichess study")
public class BookCreationRequest {

    @NotBlank
    @Size(min = 8, max = 8)
    @Schema(
        description = "The lichess study id",
        requiredMode = REQUIRED)
    private String studyId;

}
