package io.github.fifi2.chessmorize.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "chessmorize.lichess")
@Validated
@Data
public class LichessProperties {

    @NotBlank
    private String url;

}
