package io.github.fifi2.chessmorize.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Color of a side, piece or square in chess, e.g. WHITE or BLACK")
public enum Color {

    WHITE,
    BLACK;

}
