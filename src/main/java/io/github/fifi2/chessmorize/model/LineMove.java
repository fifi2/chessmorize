package io.github.fifi2.chessmorize.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Builder
@Data
public class LineMove {

    private UUID moveId;
    private String uci;
    private String comment;

}
