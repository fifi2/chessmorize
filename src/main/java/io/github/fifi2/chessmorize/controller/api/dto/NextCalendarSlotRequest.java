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
@Schema(description = "Request to move to the next calendar slot in training")
public class NextCalendarSlotRequest {

    @NotNull
    @Schema(
        description = "The book id",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d",
        requiredMode = REQUIRED)
    private UUID bookId;

}
