package io.github.fifi2.chessmorize.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@Schema(description = "A line in a book, representing a sequence of moves for training")
public class Line {

    @Schema(
        description = "Line unique identifier",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d")
    private UUID id;

    @Schema(
        description = "ID of the chapter this line belongs to",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d")
    private UUID chapterId;

    @Schema(description = "The line content as a list of LineMove")
    private final List<LineMove> moves;

    @Schema(
        description = "Box ID for spaced repetition",
        example = "1",
        defaultValue = "0")
    private int boxId;

    @Schema(
        description = "Last training timestamp (ISO 8601)",
        example = "2024-07-20T12:34:56Z")
    private Instant lastTraining;

    @Schema(
        description = "Last calendar slot when the line was studied",
        example = "3")
    private Integer lastCalendarSlot; // null if the Line has not been studied

    /**
     * Check if the Line has not been trained today.
     *
     * @return a boolean (true = has not been trained today).
     */
    @JsonIgnore
    public boolean hasNotBeenTrainedToday() {

        return Optional
            .ofNullable(this.lastTraining)
            .map(last -> last
                .atOffset(ZoneOffset.UTC)
                .toLocalDate())
            .map(last -> Instant.now()
                .atOffset(ZoneOffset.UTC)
                .toLocalDate()
                .isAfter(last))
            .orElse(true);
    }

}
