package io.github.fifi2.chessmorize.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Schema(description = "A book containing chapters and lines for chess training")
public class Book {

    @Schema(
        description = "The book id",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d")
    private UUID id;

    @Schema(
        description = "The lichess study id",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d")
    private String studyId;

    @Schema(
        description = "The book name",
        example = "White")
    private String name;

    @Schema(description = "The imported chapters")
    private List<Chapter> chapters;

    @Schema(description = "The generated lines")
    private List<Line> lines;

    // position in the training calendar, default to zero
    @Schema(
        description = "The position in the training calendar",
        example = "1",
        defaultValue = "0")
    private int calendarSlot;

}
