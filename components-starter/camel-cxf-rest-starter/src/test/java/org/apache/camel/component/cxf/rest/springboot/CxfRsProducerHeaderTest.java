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


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.Response;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.CXFTestSupport;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.cxf.util.CxfUtils;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.xml.CamelEndpointFactoryBean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;



@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfRsProducerHeaderTest.class,
        CxfRsProducerHeaderTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CxfRsProducerHeaderTest {

    static int port2 = CXFTestSupport.getPort2();
    static int port3 = CXFTestSupport.getPort("CxfRsProducerHeaderTest.1");

    private static final Object RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                                           + "<Customer><id>123</id><name>John</name></Customer>";

    @Autowired
    protected CamelContext camelContext;
    
    private Server server;
    
    @BeforeEach
    public void setUp() throws Exception {
        JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
        List<Object> serviceBeans = new ArrayList<Object>();
        serviceBeans.add(new org.apache.camel.component.cxf.jaxrs.testbean.CustomerService());
        sfb.setServiceBeans(serviceBeans);
        sfb.setAddress("/CxfRsProducerHeaderTest/");
        sfb.setStaticSubresourceResolution(true);
        server = sfb.create();
        server.start();
    }

    @AfterEach
    public void shutdown() throws Exception {
        if (server != null) {
            server.stop();
            server.destroy();
        }
    }

    @Test
    public void testInvokeThatDoesNotInvolveHeaders() throws Exception {
        Exchange exchange = camelContext.createProducerTemplate().send("direct://http", new Processor() {

            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
                inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/123");
                inMessage.setBody(null);
            }

        });

        // verify the out message is a Response object by default
        Response response = (Response) exchange.getMessage().getBody();
        assertNotNull(response, "The response should not be null");
        assertEquals(200, response.getStatus());

        // test converter (from Response to InputStream)
        assertEquals(RESPONSE, CxfUtils.getStringFromInputStream(exchange.getMessage().getBody(InputStream.class)));
    }

    @Test
    public void testHeaderFilteringAndPropagation() throws Exception {
        Exchange exchange = camelContext.createProducerTemplate().send("direct://http2", new Processor() {

            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
                inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                inMessage.setHeader(Exchange.HTTP_PATH, "/CxfRsProducerHeaderTest/customerservice/customers/123");
                inMessage.setHeader("Accept", "application/json");
                inMessage.setHeader("my-user-defined-header", "my-value");
                inMessage.setBody(null);
            }

        });

        // get the response message 
        Response response = (Response) exchange.getMessage().getBody();

        // check the response code on the Response object as set by the "HttpProcess"
        assertEquals(200, response.getStatus());

        // get out message
        Message outMessage = exchange.getMessage();

        // verify the content-type header sent by the "HttpProcess"
        assertEquals("text/xml", outMessage.getHeader(Exchange.CONTENT_TYPE));

        // check the user defined header echoed by the "HttpProcess"
        assertEquals("my-value", outMessage.getHeader("echo-my-user-defined-header"));

        // check the Accept header echoed by the "HttpProcess"
        assertEquals("application/json", outMessage.getHeader("echo-accept"));

        // make sure the HttpProcess have not seen CxfConstants.CAMEL_CXF_RS_USING_HTTP_API
        assertNull(outMessage.getHeader("failed-header-using-http-api"));

        // make sure response code has been set in out header
        assertEquals(Integer.valueOf(200), outMessage.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
    }

    public static class HttpProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            Message in = exchange.getIn();
            Message out = exchange.getMessage();

            if (in.getHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API) != null) {
                // this should have been filtered
                out.setHeader("failed-header-using-http-api", CxfConstants.CAMEL_CXF_RS_USING_HTTP_API);
            }

            out.setHeader("echo-accept", in.getHeader("Accept"));
            out.setHeader("echo-my-user-defined-header", in.getHeader("my-user-defined-header"));

            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "text/xml");

        }

    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        @Bean
        public HttpProcessor httpProcessor() {
            return new HttpProcessor();
        }
        
        @Bean
        public ServletWebServerFactory servletWebServerFactory() {
            return new UndertowServletWebServerFactory(port2);
        }
        
        @Bean 
        public CamelEndpointFactoryBean to1() {
            CamelEndpointFactoryBean cxfRsProducer = new CamelEndpointFactoryBean();
            cxfRsProducer.setUri("cxfrs://http://localhost:" + port2 + "/services/CxfRsProducerHeaderTest/");
            return cxfRsProducer;
        }
        
        @Bean 
        public CamelEndpointFactoryBean to2() {
            CamelEndpointFactoryBean cxfRsProducer = new CamelEndpointFactoryBean();
            cxfRsProducer.setUri("cxfrs://http://localhost:" + port3 + "/CxfRsProducerHeaderTest");
            return cxfRsProducer;
        }
        
        @Bean 
        public CamelEndpointFactoryBean from1() {
            CamelEndpointFactoryBean jettyConsumer = new CamelEndpointFactoryBean();
            jettyConsumer.setUri("jetty://http://localhost:" + port3 + "/CxfRsProducerHeaderTest?matchOnUriPrefix=true");
            return jettyConsumer;
        }
        
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct://http").to("ref:to1");
                    from("direct://http2").to("ref:to2");
                    from("ref:from1").process("httpProcessor");
                }
            };
        }
    }
    
}
