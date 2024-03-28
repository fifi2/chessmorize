package io.github.fifi2.chessmorize.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractController {

    protected void logError(final ServerHttpRequest request,
                            final Throwable throwable) {

        log.error(
            "Error occurred on {} {}",
            request.getMethod(),
            request.getPath(),
            throwable);
    }

}
