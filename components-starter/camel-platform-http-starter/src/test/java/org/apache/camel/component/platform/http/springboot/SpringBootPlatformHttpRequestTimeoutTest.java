package org.apache.camel.component.platform.http.springboot;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;

@EnableAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class, SecurityAutoConfiguration.class})
@CamelSpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { CamelAutoConfiguration.class,
        SpringBootPlatformHttpRequestTimeoutTest.TestConfiguration.class,
        PlatformHttpComponentAutoConfiguration.class, SpringBootPlatformHttpAutoConfiguration.class, },
        properties = {"camel.component.platform-http.request-timeout=10"})
@AutoConfigureMockMvc
public class SpringBootPlatformHttpRequestTimeoutTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void testGetAsync() throws Exception {
        Assertions.assertThat(restTemplate.getForEntity("/slow-get", String.class).getStatusCode())
                .isEqualTo(HttpStatusCode.valueOf(503));
    }

    @Configuration
    public static class TestConfiguration {

        @Bean
        public RouteBuilder platformHttpRouteBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("platform-http:/slow-get").id("slow-route")
                            .process(exchange -> Thread.sleep(1000))
                            .setBody().constant("get");
                }
            };
        }
    }


}
