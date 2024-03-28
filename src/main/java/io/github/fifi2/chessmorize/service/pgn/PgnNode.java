package io.github.fifi2.chessmorize.service.pgn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class PgnNode {

    private String san;
    private PgnNag nag;
    private String comment;
    private Map<String, List<String>> annotations;
    private List<PgnNode> variations;
    private String fen;
    private String uci;

}
