/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.platform.http.springboot;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoConfiguration(exclude = {
        ManagementWebSecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        SecurityAutoConfiguration.class
    }
)
@CamelSpringBootTest
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        CamelAutoConfiguration.class,
        SpringBootPlatformHttpUndertowActuatorTest.class,
        SpringBootPlatformHttpUndertowActuatorTest.TestConfiguration.class,
        PlatformHttpComponentAutoConfiguration.class,
        SpringBootPlatformHttpAutoConfiguration.class
    },
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.metrics.access=unrestricted",
        "server.undertow.threads.worker.core=4",
        "server.undertow.threads.worker.max=8",
        "server.undertow.options.server.ENABLE_STATISTICS=true"
    }
)
public class SpringBootPlatformHttpUndertowActuatorTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Configuration
    public static class TestConfiguration {

        @Bean
        public RouteBuilder testRouteBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("platform-http:/test").setBody().constant("test response");
                }
            };
        }
    }

    @Test
    public void testUndertowWorkerThreadMetricsAvailable() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("undertow.threads.worker");
    }

    @Test
    public void testUndertowWorkerCoreThreadsMetric() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics/undertow.threads.worker.core", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"undertow.threads.worker.core\"");
        assertThat(response.getBody()).contains("\"baseUnit\":\"threads\"");
    }

    @Test
    public void testUndertowWorkerMaxThreadsMetric() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics/undertow.threads.worker.max", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"undertow.threads.worker.max\"");
        assertThat(response.getBody()).contains("\"baseUnit\":\"threads\"");
    }

    @Test
    public void testUndertowWorkerCurrentThreadsMetric() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics/undertow.threads.worker.current", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"undertow.threads.worker.current\"");
        assertThat(response.getBody()).contains("\"baseUnit\":\"threads\"");
    }

    @Test
    public void testUndertowWorkerBusyThreadsMetric() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics/undertow.threads.worker.busy", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"undertow.threads.worker.busy\"");
        assertThat(response.getBody()).contains("\"baseUnit\":\"threads\"");
    }

    @Test
    public void testUndertowWorkerUtilizationMetric() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics/undertow.threads.worker.utilization", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"undertow.threads.worker.utilization\"");
        assertThat(response.getBody()).contains("\"baseUnit\":\"percent\"");
    }

    @Test
    public void testUndertowWorkerQueueSizeMetric() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics/undertow.threads.worker.queue.size", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"undertow.threads.worker.queue.size\"");
    }

    @Test
    public void testUndertowIOThreadsMetric() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics/undertow.threads.io", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"undertow.threads.io\"");
        assertThat(response.getBody()).contains("\"baseUnit\":\"threads\"");
    }

    @Test
    public void testActuatorEndpointAfterHttpRequest() {
        restTemplate.getForEntity("/test", String.class);

        ResponseEntity<String> metricsResponse = restTemplate.getForEntity("/actuator/metrics/undertow.threads.worker.busy", String.class);
        
        assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metricsResponse.getBody()).contains("\"name\":\"undertow.threads.worker.busy\"");
    }
}
