package com.github.hanleyt;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import java.util.function.Function;

public class JerseyExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final Function<ExtensionContext, Application> applicationProvider;
    private JerseyTest jerseyTest;

    public JerseyExtension(Function<ExtensionContext, Application> applicationProvider) {
        this.applicationProvider = applicationProvider;
    }

    private void initJerseyTest(ExtensionContext context) {
        jerseyTest = new JerseyTest() {
            @Override
            protected Application configure() {
                return applicationProvider.apply(context);
            }
        };
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        jerseyTest.tearDown();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        initJerseyTest(context);
        jerseyTest.setUp();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(Client.class) || parameterContext.getParameter().getType().equals(WebTarget.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> parameterType = parameterContext.getParameter().getType();

        if (parameterType.equals(Client.class)) {
            return jerseyTest.client();
        }
        if (parameterType.equals(WebTarget.class)) {
            return jerseyTest.target();
        }
        throw new IllegalStateException("Unrecognised parameter type: " + parameterType);
    }
}