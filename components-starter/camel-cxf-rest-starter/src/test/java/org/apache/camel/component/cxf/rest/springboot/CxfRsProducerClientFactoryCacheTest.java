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


import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.CXFTestSupport;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.cxf.jaxrs.testbean.Customer;
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSClientFactoryBean;
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSServerFactoryBean;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.apache.cxf.jaxrs.AbstractJAXRSFactoryBean;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;




@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfRsProducerClientFactoryCacheTest.class,
        CxfRsProducerClientFactoryCacheTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CxfRsProducerClientFactoryCacheTest {
    
    private int port = CXFTestSupport.getPort1();
     
    private Server server;
    
    
    @Autowired
    ProducerTemplate template;
    
    @BeforeEach
    public void setUp() throws Exception {
        JAXRSServerFactoryBean sfb = new SpringJAXRSServerFactoryBean();
        List<Object> serviceBeans = new ArrayList<Object>();
        serviceBeans.add(new org.apache.camel.component.cxf.jaxrs.testbean.CustomerService());
        sfb.setServiceBeans(serviceBeans);
        sfb.setAddress("/CxfRsProducerClientFactoryCacheTest/");
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
    public void testGetCostumerWithHttpCentralClientAPI() throws Exception {
        doRunTest(template, port);
    }

    private void doRunTest(ProducerTemplate template, final int clientPort) {
        Exchange exchange = template.send("direct://http", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
                inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/123");
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Customer.class);
                inMessage.setHeader("clientPort", clientPort);
                inMessage.setBody(null);
            }
        });

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();

        assertNotNull(response, "The response should not be null");
        assertEquals("123", String.valueOf(response.getId()), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
        assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
    }


    
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        
        @Bean
        public ServletWebServerFactory servletWebServerFactory() {
            return new UndertowServletWebServerFactory(port);
        }
        
        
        @Bean
        public AbstractJAXRSFactoryBean rsClient() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            
            afb.setAddress("http://localhost:" + port + "/services/CxfRsProducerClientFactoryCacheTest/");
            return afb;
        }
        
        
        
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct://http")
                        .to("cxfrs:bean:rsClient");
                }
            };
        }
    }
    
}
