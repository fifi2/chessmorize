package io.github.fifi2.chessmorize.service.pgn;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class PgnGame {

    private Map<String, String> tags;
    private String comment;
    private Map<String, List<String>> annotations;
    private List<PgnNode> nodes;

}
