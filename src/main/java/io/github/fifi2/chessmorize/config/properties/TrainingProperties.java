package io.github.fifi2.chessmorize.config.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "chessmorize.training")
@Validated
@Data
public class TrainingProperties {

    private boolean shuffled;

    @NotNull
    private List<List<Integer>> calendar;

    public Integer getMaxNumber() {

        return this.calendar
            .stream()
            .flatMap(Collection::stream)
            .max(Integer::compareTo)
            .orElseThrow();
    }

}
