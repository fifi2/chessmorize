package io.github.fifi2.chessmorize.controller.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
public class TrainingResultRequest {

    @NotNull
    private UUID bookId;

    @NotNull
    private UUID lineId;

    @NotNull
    private Boolean result;

}
