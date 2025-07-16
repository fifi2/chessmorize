package io.github.fifi2.chessmorize.helper;

import org.junit.jupiter.api.extension.*;

import java.io.IOException;

public class LichessMockExtension
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final LichessMock lichessMock = new LichessMock();

    @Override
    public void beforeEach(ExtensionContext context) throws IOException {

        this.lichessMock.start();
    }

    @Override
    public void afterEach(ExtensionContext context) throws IOException {

        this.lichessMock.stop();
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) {

        return parameterContext.getParameter().getType().equals(LichessMock.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) {

        return this.lichessMock;
    }

}
