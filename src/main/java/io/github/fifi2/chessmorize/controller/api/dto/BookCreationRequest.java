package io.github.fifi2.chessmorize.controller.api.dto;

import io.github.fifi2.chessmorize.model.Color;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Schema(
        description = """
            The color of the book, meaning the side played in the study.
            Possible values are: `WHITE` or `BLACK`
            """,
        requiredMode = REQUIRED)
    private Color color;

}
