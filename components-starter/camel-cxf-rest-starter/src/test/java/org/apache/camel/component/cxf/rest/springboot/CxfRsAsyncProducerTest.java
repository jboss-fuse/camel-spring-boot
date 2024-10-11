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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.Response;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.CXFTestSupport;
import org.apache.camel.component.cxf.common.CxfOperationException;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.util.CastUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxrs.AbstractJAXRSFactoryBean;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.utils.ParameterizedCollectionType;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;



@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfRsAsyncProducerTest.class,
        CxfRsAsyncProducerTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }
)
public class CxfRsAsyncProducerTest {

    static int port1 = CXFTestSupport.getPort1();
    static int port2 = CXFTestSupport.getPort2();
    
    private Server server;

    @Autowired
    private ProducerTemplate template;
    
    @Autowired
    private CamelContext context;
    
    
    
    @BeforeEach
    public void setUp() throws Exception {
        JAXRSServerFactoryBean sfb = new SpringJAXRSServerFactoryBean();
        List<Object> serviceBeans = new ArrayList<Object>();
        serviceBeans.add(new org.apache.camel.component.cxf.jaxrs.testbean.CustomerService());
        sfb.setServiceBeans(serviceBeans);
        sfb.setAddress("http://localhost:" + port1 + "/services/CxfRsAsyncProducerTest/");
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
    public void testGetCustomerWithClientProxyAPI() {
        // START SNIPPET: ProxyExample
        Exchange exchange = template.send("direct://proxy", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                // set the operation name 
                inMessage.setHeader(CxfConstants.OPERATION_NAME, "getCustomer");
                // using the proxy client API
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.FALSE);
                // set a customer header
                inMessage.setHeader("key", "value");
                // set the parameters , if you just have one parameter 
                // camel will put this object into an Object[] itself
                inMessage.setBody("123");
            }
        });

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();

        assertNotNull(response, "The response should not be null");
        assertEquals(123, response.getId(), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
        assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
        assertEquals("value", exchange.getMessage().getHeader("key"), "Get a wrong header value");
        // END SNIPPET: ProxyExample     
    }

    @Test
    public void testGetCustomersWithClientProxyAPI() {
        Exchange exchange = template.send("direct://proxy", newExchange -> {
            newExchange.setPattern(ExchangePattern.InOut);
            Message inMessage = newExchange.getIn();
            // set the operation name 
            inMessage.setHeader(CxfConstants.OPERATION_NAME, "getCustomers");
            // using the proxy client API
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.FALSE);
            // camel will put this object into an Object[] itself
            inMessage.setBody(null);
        });

        // get the response message 
        List<Customer> response = CastUtils.cast((List<?>) exchange.getMessage().getBody());

        assertNotNull(response, "The response should not be null");
        assertTrue(response.contains(new Customer(113, "Dan")), "Dan is missing!");
        assertTrue(response.contains(new Customer(123, "John")), "John is missing!");
        assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
    }

    //@Test
    public void testGetCustomersWithHttpCentralClientAPI() {
        Exchange exchange = template.send("direct://proxy", newExchange -> {
            newExchange.setPattern(ExchangePattern.InOut);
            Message inMessage = newExchange.getIn();
            // set the Http method
            inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
            // set the relative path 
            inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/");
            // using the proxy client API
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
            // set the headers 
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, List.class);
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_GENERIC_TYPE,
                    new ParameterizedCollectionType(Customer.class));
            // camel will put this object into an Object[] itself
            inMessage.setBody(null);
        });

        // get the response message 
        List<Customer> response = CastUtils.cast((List<?>) exchange.getMessage().getBody());

        assertNotNull(response, "The response should not be null");
        assertTrue(response.contains(new Customer(113, "Dan")), "Dan is missing!");
        assertTrue(response.contains(new Customer(123, "John")), "John is missing!");
        assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
    }

    @Test
    public void testGetCustomerWithHttpCentralClientAPI() {
        Exchange exchange = template.send("direct://http", newExchange -> {
            newExchange.setPattern(ExchangePattern.InOut);
            Message inMessage = newExchange.getIn();
            // using the http central client API
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
            // set the Http method
            inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
            // set the relative path
            inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/123");
            // Specify the response class , cxfrs will use InputStream as the response object type 
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Customer.class);
            // set a customer header
            inMessage.setHeader("key", "value");
            // since we use the Get method, so we don't need to set the message body
            inMessage.setBody(null);
        });

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();

        assertNotNull(response, "The response should not be null");
        assertEquals(123, response.getId(), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
        assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
        assertEquals("value", exchange.getMessage().getHeader("key"), "Get a wrong header value");
    }

    @Test
    public void testSuppressGetCustomerExceptionWithCxfRsEndpoint() {
        Exchange exchange
                = template.send("cxfrs://http://localhost:" + port1 + "/services/" + getClass().getSimpleName()
                                + "/?httpClientAPI=true&throwExceptionOnFailure=false",
                        newExchange -> {
                            newExchange.setPattern(ExchangePattern.InOut);
                            Message message = newExchange.getIn();
                            // set the Http method
                            message.setHeader(Exchange.HTTP_METHOD, "PUT");
                            // set the relative path
                            message.setHeader(Exchange.HTTP_PATH, "/customerservice/customers");
                            // we just setup the customer with a wrong id
                            Customer customer = new Customer();
                            customer.setId(222);
                            customer.setName("user");
                            message.setBody(customer);
                        });

        // we should get the exception here 
        assertNull(exchange.getException(), "Don't expect the exception here");
        Message result = exchange.getMessage();
        assertEquals(406, result.getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong http status code.");

    }

    @Test
    public void testGetCustomerExceptionWithCxfRsEndpoint() {
        Exchange exchange
                = template.send(
                        "cxfrs://http://localhost:" + port1 + "/services/" + getClass().getSimpleName() + "/?httpClientAPI=true",
                        newExchange -> {
                            newExchange.setPattern(ExchangePattern.InOut);
                            Message message = newExchange.getIn();
                            // set the Http method
                            message.setHeader(Exchange.HTTP_METHOD, "PUT");
                            // set the relative path
                            message.setHeader(Exchange.HTTP_PATH, "/customerservice/customers");
                            // we just setup the customer with a wrong id
                            Customer customer = new Customer();
                            customer.setId(222);
                            customer.setName("user");
                            message.setBody(customer);
                        });

        // we should get the exception here 
        assertNotNull(exchange.getException(), "Expect the exception here");
        CxfOperationException exception = (CxfOperationException) exchange.getException();

        assertEquals("Cannot find the customer!", exception.getResponseBody(), "Get a wrong response body");

    }

    @Test
    public void testGetCustomerWithCxfRsEndpoint() {
        Exchange exchange
                = template.send(
                        "cxfrs://http://localhost:" + port1 + "/services/" + getClass().getSimpleName() + "/?httpClientAPI=true",
                        newExchange -> {
                            newExchange.setPattern(ExchangePattern.InOut);
                            Message inMessage = newExchange.getIn();
                            // set the Http method
                            inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                            // set the relative path
                            inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/123");
                            // Specify the response class , cxfrs will use InputStream as the response object type 
                            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Customer.class);
                            // since we use the Get method, so we don't need to set the message body
                            inMessage.setBody(null);
                        });

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();
        assertNotNull(response, "The response should not be null");
        assertEquals(123, response.getId(), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
        assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
    }

    @Test
    public void testGetCustomerWithVariableReplacementAndCxfRsEndpoint() {
        Exchange exchange = template.send(
                "cxfrs://http://localhost:" + port1 + "/services/" + getClass().getSimpleName() + "/?httpClientAPI=true",
                newExchange -> {
                    newExchange.setPattern(ExchangePattern.InOut);
                    Message inMessage = newExchange.getIn();
                    // set the Http method
                    inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                    // set the relative path
                    inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customers/{customerId}");
                    // Set variables for replacement
                    inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_VAR_VALUES, new String[] { "123" });
                    // Specify the response class , cxfrs will use InputStream as the response object type
                    inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Customer.class);
                    // since we use the Get method, so we don't need to set the message body
                    inMessage.setBody(null);
                });

        // get the response message
        Customer response = (Customer) exchange.getMessage().getBody();
        assertNotNull(response, "The response should not be null");
        assertEquals(123, response.getId(), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
        assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
    }

    @Test
    public void testAddCustomerUniqueResponseCodeWithHttpClientAPI() {
        Exchange exchange
                = template.send(
                        "cxfrs://http://localhost:" + port1 + "/services/" + getClass().getSimpleName() + "?httpClientAPI=true",
                        newExchange -> {
                            newExchange.setPattern(ExchangePattern.InOut);
                            Message inMessage = newExchange.getIn();
                            // set the Http method
                            inMessage.setHeader(Exchange.HTTP_METHOD, "POST");
                            // set the relative path
                            inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customersUniqueResponseCode");
                            // create a new customer object
                            Customer customer = new Customer();
                            customer.setId(9999);
                            customer.setName("HttpClient");
                            inMessage.setBody(customer);
                        });

        // get the response message 
        Response response = (Response) exchange.getMessage().getBody();
        assertNotNull(response, "The response should not be null");
        assertNotNull(response.getEntity(), "The response entity should not be null");
        // check the response code
        assertEquals(201, response.getStatus(), "Get a wrong response code");
        // check the response code from message header
        assertEquals(201, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
    }

    //@Test
    public void testAddCustomerUniqueResponseCodeWithProxyAPI() {
        Exchange exchange = template.send("direct://proxy", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                Message inMessage = exchange.getIn();
                // set the operation name 
                inMessage.setHeader(CxfConstants.OPERATION_NAME, "addCustomerUniqueResponseCode");
                // using the proxy client API
                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.FALSE);
                // set the parameters , if you just have one parameter 
                // camel will put this object into an Object[] itself
                Customer customer = new Customer();
                customer.setId(8888);
                customer.setName("ProxyAPI");
                inMessage.setBody(customer);
            }
        });

        // get the response message 
        Response response = (Response) exchange.getMessage().getBody();
        assertNotNull(response, "The response should not be null");
        assertNotNull(response.getEntity(), "The response entity should not be null");
        // check the response code
        assertEquals(201, response.getStatus(), "Get a wrong response code");
        // check the response code from message header
        assertEquals(201, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
    }

    @Test
    public void testAddCustomerUniqueResponseCode() {
        Exchange exchange
                = template.send(
                        "cxfrs://http://localhost:" + port1 + "/services/" + getClass().getSimpleName() + "?httpClientAPI=true",
                        new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                exchange.setPattern(ExchangePattern.InOut);
                                Message inMessage = exchange.getIn();
                                // set the Http method
                                inMessage.setHeader(Exchange.HTTP_METHOD, "POST");
                                // set the relative path
                                inMessage.setHeader(Exchange.HTTP_PATH, "/customerservice/customersUniqueResponseCode");
                                // put the response's entity into out message body
                                inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Customer.class);
                                // create a new customer object
                                Customer customer = new Customer();
                                customer.setId(8888);
                                customer.setName("Willem");
                                inMessage.setBody(customer);
                            }
                        });

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();
        assertNotNull(response, "The response should not be null");
        assertNotEquals(8888, response.getId(), "Get a wrong customer id");
        assertEquals("Willem", response.getName(), "Get a wrong customer name");
        assertEquals(201, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
    }

    @Test
    public void testProducerWithQueryParameters() {
        Exchange exchange = template.send("cxfrs://http://localhost:" + port2 + "/" + getClass().getSimpleName()
                                          + "/testQuery?httpClientAPI=true&q1=12&q2=13",
                newExchange -> {
                    newExchange.setPattern(ExchangePattern.InOut);
                    Message inMessage = newExchange.getIn();
                    // set the Http method
                    inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                    inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, InputStream.class);
                    inMessage.setBody(null);
                });

        // get the response message 
        String response = exchange.getMessage().getBody(String.class);
        assertNotNull(response, "The response should not be null");
        assertEquals("q1=12&q2=13", response, "The response value is wrong");
    }

    @Test
    public void testProducerWithQueryParametersHeader() {
        Exchange exchange = template.send("cxfrs://http://localhost:" + port2 + "/" + getClass().getSimpleName()
                                          + "/testQuery?httpClientAPI=true&q1=12&q2=13",
                newExchange -> {
                    newExchange.setPattern(ExchangePattern.InOut);
                    Message inMessage = newExchange.getIn();
                    // set the Http method
                    inMessage.setHeader(Exchange.HTTP_METHOD, "GET");
                    inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, InputStream.class);
                    // override the parameter setting from URI
                    // START SNIPPET: QueryMapExample
                    Map<String, String> queryMap = new LinkedHashMap<>();
                    queryMap.put("q1", "new");
                    queryMap.put("q2", "world");
                    inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_QUERY_MAP, queryMap);
                    // END SNIPPET: QueryMapExample 
                    inMessage.setBody(null);
                });

        // get the response message 
        String response = exchange.getMessage().getBody(String.class);
        assertNotNull(response, "The response should not be null");
        assertEquals("q1=new&q2=world", response, "The response value is wrong");
    }

    @Test
    public void testRestServerDirectlyGetCustomer() {
        // we cannot convert directly to Customer as we need camel-jaxb
        String response = template.requestBodyAndHeader(
                "cxfrs:http://localhost:" + port1 + "/services/" + getClass().getSimpleName() + "/customerservice/customers/123",
                null, Exchange.HTTP_METHOD, "GET", String.class);

        assertNotNull(response, "The response should not be null");
    }

    @Test
    public void testRestServerDirectlyAddCustomer() {
        Customer input = new Customer();
        input.setName("Donald Duck");

        // we cannot convert directly to Customer as we need camel-jaxb
        String response = template.requestBodyAndHeader(
                "cxfrs:http://localhost:" + port1 + "/services/" + getClass().getSimpleName() + "/customerservice/customers",
                input, Exchange.HTTP_METHOD, "POST", String.class);

        assertNotNull(response);
        assertTrue(response.endsWith("<name>Donald Duck</name></Customer>"));
    }

    
    
    
    @Test
    public void testProducerWithFeature() {
        TestFeature feature = context.getRegistry().lookupByNameAndType("testFeature", TestFeature.class);

        template.requestBodyAndHeader("cxfrs:http://localhost:" + port1 + "/services/" + getClass().getSimpleName()
                                      + "/customerservice/customers/123?features=#myFeatures",
                null, Exchange.HTTP_METHOD, "GET", String.class);

        assertTrue(feature.initialized, "The feature should be initialized");
    }

    @Test
    public void testProducer422Response() {
        Exchange exchange = template.send(
                "cxfrs://http://localhost:" + port1 + "/services/" + getClass().getSimpleName() + "/?httpClientAPI=true",
                newExchange -> {
                    newExchange.setPattern(ExchangePattern.InOut);
                    Message message = newExchange.getIn();
                    // Try to create a new Customer with an invalid name
                    message.setHeader(Exchange.HTTP_METHOD, "POST");
                    message.setHeader(Exchange.HTTP_PATH, "/customerservice/customers");
                    Customer customer = new Customer();
                    customer.setId(8888);
                    customer.setName("");  // will trigger a 422 response (a common REST server validation response code)
                    message.setBody(customer);
                });

        assertNotNull(exchange.getException(), "Expect the exception here");
        assertTrue(exchange.getException() instanceof CxfOperationException, "Exception should be a CxfOperationException");

        CxfOperationException cxfOperationException = CxfOperationException.class.cast(exchange.getException());

        assertEquals(422, cxfOperationException.getStatusCode(), "CXF operation exception has correct response code");
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
            jettyConsumer.setUri("jetty://http://localhost:" + port2 + "/CxfRsAsyncProducerTest/testQuery");
            return jettyConsumer;
        }
        
        @Bean 
        public Processor myProcessor() {
            return new JettyProcessor();
        }
        
        @Bean
        public AbstractJAXRSFactoryBean rsClientProxy() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            //afb.setBus(BusFactory.getDefaultBus());
            afb.setAddress("http://localhost:" + port1
                                   + "/services/CxfRsAsyncProducerTest/");
            //afb.setServiceClass somehow cause conflict with other test, should be bus conflict
            afb.setServiceClass(org.apache.camel.component.cxf.jaxrs.testbean.CustomerService.class);
            afb.setLoggingFeatureEnabled(true);
            
            return afb;
        }
        
        @Bean
        public AbstractJAXRSFactoryBean rsClientHttp() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            afb.setAddress("http://localhost:" + port1
                                   + "/services/CxfRsAsyncProducerTest/");
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
                    from("direct://proxy").to("cxfrs:bean:rsClientProxy");
                    from("direct://http").to("cxfrs:bean:rsClientHttp");
                    from("ref:fromEndpoint").process("myProcessor");
                }
            };
        }
    }
    
}
