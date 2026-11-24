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
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSClientFactoryBean;
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSServerFactoryBean;
import org.apache.camel.http.base.cookie.BaseCookieHandler;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.xml.CamelEndpointFactoryBean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        CxfRsAsyncProducerSessionTest.class,
        CxfRsAsyncProducerSessionTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }//, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CxfRsAsyncProducerSessionTest {

    static int port1 = CXFTestSupport.getPort1();
    static int port2 = CXFTestSupport.getPort2();
    
    private Server server;

    @Autowired
    private ProducerTemplate template;
    
   
    
    @BeforeEach
    public void setUp() throws Exception {
        JAXRSServerFactoryBean sfb = new SpringJAXRSServerFactoryBean();
        List<Object> serviceBeans = new ArrayList<Object>();
        serviceBeans.add(new org.apache.camel.component.cxf.jaxrs.testbean.EchoService());
        sfb.setServiceBeans(serviceBeans);
        sfb.setAddress("http://localhost:" + port1 + "/services/CxfRsProducerSessionTest/");
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
    public void testNoSessionProxy() {
        String response = sendMessage("direct://proxy", "World", Boolean.FALSE).getMessage().getBody(String.class);
        assertEquals("New New World", response);
        response = sendMessage("direct://proxy", "World", Boolean.FALSE).getMessage().getBody(String.class);
        assertEquals("New New World", response);
    }

    @Test
    public void testExchangeSessionProxy() {
        String response = sendMessage("direct://proxyexchange", "World", Boolean.FALSE).getMessage().getBody(String.class);
        assertEquals("Old New World", response);
        response = sendMessage("direct://proxyexchange", "World", Boolean.FALSE).getMessage().getBody(String.class);
        assertEquals("Old New World", response);
    }

    @Test
    public void testInstanceSession() {
        String response = sendMessage("direct://proxyinstance", "World", Boolean.FALSE).getMessage().getBody(String.class);
        assertEquals("Old New World", response);
        response = sendMessage("direct://proxyinstance", "World", Boolean.FALSE).getMessage().getBody(String.class);
        assertEquals("Old Old World", response);
        // we do the instance tests for proxy and http in one test because order
        // matters here
        response = sendMessage("direct://httpinstance", "World", Boolean.TRUE).getMessage().getBody(String.class);
        assertEquals("Old Old World", response);
    }

    @Test
    public void testNoSessionHttp() {
        String response = sendMessage("direct://http", "World", Boolean.TRUE).getMessage().getBody(String.class);
        assertEquals("New New World", response);
        response = sendMessage("direct://http", "World", Boolean.TRUE).getMessage().getBody(String.class);
        assertEquals("New New World", response);
    }

    @Test
    public void testExchangeSessionHttp() {
        String response = sendMessage("direct://httpexchange", "World", Boolean.TRUE).getMessage().getBody(String.class);
        assertEquals("Old New World", response);
        response = sendMessage("direct://httpexchange", "World", Boolean.TRUE).getMessage().getBody(String.class);
        assertEquals("Old New World", response);
    }

    private Exchange sendMessage(String endpoint, String body, Boolean httpApi) {
        Exchange exchange = template.send(endpoint, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                inMessage.setHeader(CxfConstants.OPERATION_NAME, "echo");
                inMessage.setHeader(Exchange.HTTP_METHOD, "POST");
                inMessage.setHeader(Exchange.HTTP_PATH, "/echoservice/echo");
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, httpApi);
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, String.class);
                inMessage.setBody(body);
            }
        });
        return exchange;
    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

       
        
        @Bean 
        public CamelEndpointFactoryBean fromEndpoint() {
            CamelEndpointFactoryBean jettyConsumer = new CamelEndpointFactoryBean();
            jettyConsumer.setUri("jetty://http://localhost:" + port2 + "/CxfRsProducerSessionTest/echoservice");
            return jettyConsumer;
        }
        
                
        @Bean
        public AbstractJAXRSFactoryBean rsClientProxy() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
           
            afb.setAddress("http://localhost:" + port1
                                   + "/services/CxfRsProducerSessionTest/");
            //afb.setServiceClass somehow cause conflict with other test, should be bus conflict
            afb.setServiceClass(org.apache.camel.component.cxf.jaxrs.testbean.EchoService.class);
            afb.setLoggingFeatureEnabled(true);
            
            return afb;
        }
        
        @Bean
        public AbstractJAXRSFactoryBean rsClientHttp() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            afb.setAddress("http://localhost:" + port1
                                   + "/services/CxfRsProducerSessionTest/");
            return afb;
        }
        
        @Bean
        public BaseCookieHandler instanceCookieHandler() {
            return new org.apache.camel.http.base.cookie.InstanceCookieHandler();
        }
        
        @Bean
        public BaseCookieHandler exchangeCookieHandler() {
            return new org.apache.camel.http.base.cookie.ExchangeCookieHandler();
        }
        
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct://proxy").to("cxfrs:bean:rsClientProxy")
                        .convertBodyTo(String.class).to("cxfrs:bean:rsClientProxy");
                    from("direct://proxyinstance").to("cxfrs:bean:rsClientProxy?cookieHandler=#instanceCookieHandler")
                        .convertBodyTo(String.class).to("cxfrs:bean:rsClientProxy?cookieHandler=#instanceCookieHandler");
                    from("direct://proxyexchange").to("cxfrs:bean:rsClientProxy?cookieHandler=#exchangeCookieHandler")
                        .convertBodyTo(String.class).to("cxfrs:bean:rsClientProxy?cookieHandler=#exchangeCookieHandler");
                    from("direct://http").to("cxfrs:bean:rsClientHttp")
                        .convertBodyTo(String.class).to("cxfrs:bean:rsClientHttp");
                    from("direct://httpinstance").to("cxfrs:bean:rsClientHttp?cookieHandler=#instanceCookieHandler")
                        .convertBodyTo(String.class).to("cxfrs:bean:rsClientHttp?cookieHandler=#instanceCookieHandler");
                    from("direct://httpexchange").to("cxfrs:bean:rsClientHttp?cookieHandler=#exchangeCookieHandler")
                        .convertBodyTo(String.class).to("cxfrs:bean:rsClientHttp?cookieHandler=#exchangeCookieHandler");
                    
                }
            };
        }
    }
    
}
