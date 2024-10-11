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
package org.apache.camel.component.cxf.rest.springboot;



import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxrs.testbean.Customer;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.stream.CachedOutputStream;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfRsStreamCacheTest.class,
        CxfRsStreamCacheTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CxfRsStreamCacheTest {

    private static final String PUT_REQUEST = "<Customer><name>Mary</name><id>123</id></Customer>";
    private static final String CONTEXT = "/CxfRsStreamCacheTest";
    private static final String CXT = "8080/services" + CONTEXT;
    private static final String RESPONSE = "<pong xmlns=\"test/service\"/>";

    private String cxfRsEndpointUri = "cxfrs://" + CONTEXT + "?synchronous=" + isSynchronous()
                                      + "&dataFormat=PAYLOAD&resourceClasses=org.apache.camel.component.cxf.jaxrs.testbean.CustomerService";

    @Autowired
    ProducerTemplate template;
    
    @EndpointInject("mock:result")
    MockEndpoint mock;
    
    @EndpointInject("mock:onComplete")
    MockEndpoint onComplete;
    
    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new UndertowServletWebServerFactory();
    }
    
    
    @Test
    public void testPutConsumer() throws Exception {
        
        mock.expectedMessageCount(1);
        mock.message(0).body().isInstanceOf(Customer.class);

        
        onComplete.expectedMessageCount(1);

        HttpPut put = new HttpPut("http://localhost:" + CXT + "/customerservice/customers");
        StringEntity entity = new StringEntity(PUT_REQUEST, ContentType.parse("text/xml; charset=ISO-8859-1"));
        put.addHeader("test", "header1;header2");
        put.setEntity(entity);
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        
        try (CloseableHttpResponse response = httpclient.execute(put)) {
            assertEquals(200, response.getCode());
            assertEquals(RESPONSE, EntityUtils.toString(response.getEntity()));
        }

        mock.assertIsSatisfied();
        onComplete.assertIsSatisfied();

    }

    protected boolean isSynchronous() {
        return false;
    }

    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {

                    getContext().setStreamCaching(true);
                    getContext().getStreamCachingStrategy().setSpoolThreshold(1L);
                    errorHandler(noErrorHandler());

                    from(cxfRsEndpointUri)
                            // should be able to convert to Customer
                            .convertBodyTo(Customer.class)
                            .to("mock:result")
                            .process(exchange -> {
                                // respond with OK
                                CachedOutputStream cos = new CachedOutputStream(exchange);
                                cos.write(RESPONSE.getBytes("UTF-8"));
                                cos.close();
                                exchange.getMessage().setBody(cos.newStreamCache());

                                exchange.getExchangeExtension().addOnCompletion(new Synchronization() {
                                    @Override
                                    public void onComplete(Exchange exchange) {
                                        template.sendBody("mock:onComplete", "");
                                    }

                                    @Override
                                    public void onFailure(Exchange exchange) {

                                    }
                                });
                            });

                }
            };
        }
    }
    
}
