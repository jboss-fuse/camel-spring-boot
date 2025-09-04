package org.apache.camel.component.platform.http.springboot;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = ServletWebServerFactoryAutoConfiguration.class)
public class UndertowWebServerFactory {

    @Bean
    public ServletWebServerFactory undertowWebServerFactory() {
        return new UndertowServletWebServerFactory();
    }

}
