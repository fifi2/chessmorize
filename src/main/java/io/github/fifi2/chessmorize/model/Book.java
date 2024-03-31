package io.github.fifi2.chessmorize.model;

import com.fasterxml.jackson.annotation.JsonView;
import io.github.fifi2.chessmorize.controller.api.Views;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Book {

    @JsonView(Views.BookList.class)
    private final UUID id;

    private final String studyId;

    @JsonView(Views.BookList.class)
    private final String name;

    private final List<Chapter> chapters;

    private List<Line> lines;

    // position in the training calendar, default to zero
    private int calendarSlot;

}
