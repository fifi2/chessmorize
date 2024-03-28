package io.github.fifi2.chessmorize.config;

import io.github.fifi2.chessmorize.config.properties.LichessProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LichessWebClientConfig {

    private final LichessProperties lichessProperties;

    @Bean
    public WebClient lichessWebClient() {

        return WebClient.builder()
            .baseUrl(lichessProperties.getUrl())
            .build();
    }

}
