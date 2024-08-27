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
import org.apache.camel.spring.xml.CamelEndpointFactoryBean;

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
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxrs.AbstractJAXRSFactoryBean;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;



@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfRsProducerAddressOverrideTest.class,
        CxfRsProducerAddressOverrideTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }
)
public class CxfRsProducerAddressOverrideTest {

    static int port1 = CXFTestSupport.getPort1();
    static int port2 = CXFTestSupport.getPort2();
    
    private Server server;

    @Autowired
    private ProducerTemplate template;
    
       
    
    @BeforeEach
    public void setUp() throws Exception {
        JAXRSServerFactoryBean sfb = new SpringJAXRSServerFactoryBean();
        List<Object> serviceBeans = new ArrayList<Object>();
        serviceBeans.add(new org.apache.camel.component.cxf.jaxrs.testbean.CustomerService());
        sfb.setServiceBeans(serviceBeans);
        sfb.setAddress("http://localhost:" + port1 + "/services/CxfRsProducerAddressOverrideTest/");
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
    
    protected void setupDestinationURL(Message inMessage) {
        inMessage.setHeader(Exchange.DESTINATION_OVERRIDE_URL,
                "http://localhost:" + port1 + "/services/CxfRsProducerAddressOverrideTest");
    }

    @Test
    public void testGetCustomerWithSyncProxyAPIByOverrideDest() {
        Exchange exchange = template.send("direct://proxy", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                // set the operation name 
                inMessage.setHeader(CxfConstants.OPERATION_NAME, "getCustomer");
                // using the proxy client API
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.FALSE);
                // set the parameters , if you just have one parameter 
                // camel will put this object into an Object[] itself
                inMessage.setBody("123");
                setupDestinationURL(inMessage);
            }
        });

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();

        assertNotNull(response, "The response should not be null");
        assertEquals(123, response.getId(), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
    }

    @Test
    public void testGetCustomerWithSyncHttpAPIByOverrideDest() {
        Exchange exchange = template.send("direct://http", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                // using the http central client API
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
                // set the Http method
                inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                // set the relative path
                inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/123");
                // Specify the response class , cxfrs will use InputStream as the response object type 
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Customer.class);
                setupDestinationURL(inMessage);
            }
        });

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();

        assertNotNull(response, "The response should not be null");
        assertEquals(123, response.getId(), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
    }

    @Test
    public void testGetCustomerWithAsyncProxyAPIByOverrideDest() {
        Exchange exchange = template.send("cxfrs:bean:rsClientProxy", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                // set the operation name 
                inMessage.setHeader(CxfConstants.OPERATION_NAME, "getCustomer");
                // using the proxy client API
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.FALSE);
                // set the parameters , if you just have one parameter 
                // camel will put this object into an Object[] itself
                inMessage.setBody("123");
                setupDestinationURL(inMessage);
            }
        });

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();

        assertNotNull(response, "The response should not be null");
        assertEquals(123, response.getId(), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
    }

    @Test
    public void testGetCustomerWithAsyncHttpAPIByOverrideDest() {
        Exchange exchange = template.send("cxfrs:bean:rsClientHttp", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                // using the http central client API
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
                // set the Http method
                inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                // set the relative path
                inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/123");
                // Specify the response class , cxfrs will use InputStream as
                // the response object type
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Customer.class);
                setupDestinationURL(inMessage);
            }
        });

        // get the response message
        Customer response = (Customer) exchange.getMessage().getBody();

        assertNotNull(response, "The response should not be null");
        assertEquals(123, response.getId(), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
    }

    @Test
    public void testAddressMultiOverride() {
        // First call with override url
        Exchange exchange = template.send("direct://http",
                new SendProcessor("http://localhost:" + port1 + "/services/CxfRsProducerAddressOverrideTest"));
        // get the response message
        Customer response = exchange.getMessage().getBody(Customer.class);
        assertNotNull(response, "The response should not be null");

        // Second call with override url
        exchange = template.send("direct://http",
                new SendProcessor("http://localhost:" + port1 + "/services/CxfRsProducerNonExistingAddressOverrideTest"));

        // Third call with override url ( we reuse the first url there )
        exchange = template.send("direct://http",
                new SendProcessor("http://localhost:" + port1 + "/services/CxfRsProducerAddressOverrideTest"));
        // get the response message
        response = exchange.getMessage().getBody(Customer.class);
        assertNotNull(response, "The response should not be null");
    }

    class SendProcessor implements Processor {
        private String address;

        public SendProcessor(String address) {
            this.address = address;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            exchange.setPattern(ExchangePattern.InOut);
            Message inMessage = exchange.getIn();

            // using the http central client API
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
            // set the Http method
            inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
            // set the relative path
            inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/123");
            // Specify the response class , cxfrs will use InputStream as the
            // response object type
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Customer.class);
            inMessage.setHeader(Exchange.DESTINATION_OVERRIDE_URL, address);
        }
    }
    
    class TestFeature implements Feature {
        boolean initialized;

        @Override
        public void initialize(InterceptorProvider interceptorProvider, Bus bus) {
            initialized = true;
        }

        @Override
        public void initialize(Client client, Bus bus) {
            //Do nothing
        }

        @Override
        public void initialize(Server server, Bus bus) {
            //Do nothing
        }

        @Override
        public void initialize(Bus bus) {
            //Do nothing
        }
    }
    
    class JettyProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            // check the query
            Message inMessage = exchange.getIn();
            exchange.getMessage().setBody(inMessage.getHeader(Exchange.HTTP_QUERY, String.class));
        }
    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        
        @Bean 
        public CamelEndpointFactoryBean fromEndpoint() {
            CamelEndpointFactoryBean jettyConsumer = new CamelEndpointFactoryBean();
            jettyConsumer.setUri("jetty://http://localhost:" + port2 + "/CxfRsProducerAddressOverrideTest/testQuery");
            return jettyConsumer;
        }
        
        @Bean 
        public Processor myProcessor() {
            return new JettyProcessor();
        }
        
        @Bean
        public AbstractJAXRSFactoryBean rsClientProxy() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            afb.setAddress("http://badAddress");
            afb.setServiceClass(org.apache.camel.component.cxf.jaxrs.testbean.CustomerService.class);
            return afb;
        }
        
        @Bean
        public AbstractJAXRSFactoryBean rsClientHttp() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            afb.setAddress("http://badAddress");
            return afb;
        }
        
        
        
        @Bean
        public Feature testFeature() {
            return new TestFeature();
        }
        
        @Bean
        public List<Feature> myFeatures(Feature testFeature) {
            List<Feature> features = new ArrayList<Feature>();
            features.add(testFeature);
            return features;
        }
        
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct://proxy").to("cxfrs:bean:rsClientProxy?synchronous=true");
                    from("direct://http").to("cxfrs:bean:rsClientHttp?synchronous=true");
                    from("ref:fromEndpoint").process("myProcessor");
                }
            };
        }
    }
    
}
