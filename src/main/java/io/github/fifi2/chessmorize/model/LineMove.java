package io.github.fifi2.chessmorize.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Builder
@Data
public class LineMove {

    private final UUID moveId;
    private final String uci;
    private final String comment;

}
