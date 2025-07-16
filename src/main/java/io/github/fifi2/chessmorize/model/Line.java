package io.github.fifi2.chessmorize.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
public class Line {

    private UUID id;
    private UUID chapterId;
    private final List<LineMove> moves;
    private int boxId;
    private Instant lastTraining;
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
