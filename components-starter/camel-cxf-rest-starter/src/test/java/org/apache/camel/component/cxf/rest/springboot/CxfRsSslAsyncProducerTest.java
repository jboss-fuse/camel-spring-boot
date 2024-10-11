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


import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.CertificateFileSslStoreProvider;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.Ssl.ClientAuth;
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
import org.apache.cxf.transport.https.httpclient.DefaultHostnameVerifier;



@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfRsSslAsyncProducerTest.class,
        CxfRsSslAsyncProducerTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CxfRsSslAsyncProducerTest {

    static int port = CXFTestSupport.getSslPort();
    
    
    @Autowired
    protected ProducerTemplate template;
    
    private Server server;
    
    @BeforeEach
    public void setUp() throws Exception {
        JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
        List<Object> serviceBeans = new ArrayList<Object>();
        serviceBeans.add(new org.apache.camel.component.cxf.jaxrs.testbean.CustomerService());
        sfb.setServiceBeans(serviceBeans);
        sfb.setAddress("/CxfRsSslAsyncProducerTest/");
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
        // do nothing here
    }
    
    @Test
    public void testCorrectTrustStore() {
        Exchange exchange = template.send("direct://trust", new MyProcessor());

        // get the response message 
        Customer response = (Customer) exchange.getMessage().getBody();

        assertNotNull(response, "The response should not be null");
        assertEquals("123", String.valueOf(response.getId()), "Get a wrong customer id");
        assertEquals("John", response.getName(), "Get a wrong customer name");
        assertEquals(200, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE), "Get a wrong response code");
        assertEquals("value", exchange.getMessage().getHeader("key"), "Get a wrong header value");
    }

    @Test
    public void testNoTrustStore() {
        Exchange exchange = template.send("direct://noTrust", new MyProcessor());
        assertTrue(exchange.isFailed());
        Exception e = exchange.getException();
        assertEquals("javax.net.ssl.SSLHandshakeException", e.getCause().getClass().getCanonicalName());
    }

    @Test
    public void testWrongTrustStore() {
        Exchange exchange = template.send("direct://wrongTrust", new MyProcessor());
        assertTrue(exchange.isFailed());
        Exception e = exchange.getException();
        assertEquals("javax.net.ssl.SSLHandshakeException", e.getCause().getClass().getCanonicalName());
    }

    private class MyProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            exchange.setPattern(ExchangePattern.InOut);
            Message inMessage = exchange.getIn();
            setupDestinationURL(inMessage);
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
        }
    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        
        
        /*
         * This is the way how to configure TLS/SSL with Bean 
         */
        @Bean
        public ServletWebServerFactory servletWebServerFactory() throws UnknownHostException {
            UndertowServletWebServerFactory undertowWebServerFactory 
                = new UndertowServletWebServerFactory();
            Ssl ssl = new Ssl();
            ssl.setClientAuth(ClientAuth.NONE);
            ssl.setKeyPassword("changeit");
            ssl.setKeyStoreType("JKS");
            ssl.setKeyStore("classpath:ssl/keystore-server.jks");
            ssl.setKeyStorePassword("changeit");
            SslBuilderCustomizer sslBuilderCustomizer = 
                new SslBuilderCustomizer(port, InetAddress.getByName("localhost"),
                                         ssl, CertificateFileSslStoreProvider.from(ssl));
            undertowWebServerFactory.addBuilderCustomizers(sslBuilderCustomizer);
            return undertowWebServerFactory;
        }
        
        @Bean
        DefaultHostnameVerifier defaultHostnameVerifier() {
            return new DefaultHostnameVerifier();
        }
        
        @Bean
        SSLContextParameters mySslContext() {
            SSLContextParameters sslContext = new SSLContextParameters();
            TrustManagersParameters trustManager = new TrustManagersParameters();
            KeyStoreParameters keyStore = new KeyStoreParameters();
            keyStore.setType("JKS");
            keyStore.setPassword("changeit");
            keyStore.setResource("/ssl/truststore-client.jks");
            trustManager.setKeyStore(keyStore);
            sslContext.setTrustManagers(trustManager);
            return sslContext;
        }
        
        @Bean
        SSLContextParameters wrongSslContext() {
            SSLContextParameters sslContext = new SSLContextParameters();
            TrustManagersParameters trustManager = new TrustManagersParameters();
            KeyStoreParameters keyStore = new KeyStoreParameters();
            keyStore.setType("JKS");
            keyStore.setPassword("changeit");
            keyStore.setResource("/ssl/truststore-wrong.jks");
            trustManager.setKeyStore(keyStore);
            sslContext.setTrustManagers(trustManager);
            return sslContext;
        }
        
        
        @Bean
        AbstractJAXRSFactoryBean serviceEndpoint(SSLContextParameters mySslContext, DefaultHostnameVerifier defaultHostnameVerifier) {
            AbstractJAXRSFactoryBean afb = new SpringJAXRSClientFactoryBean();
            afb.setAddress("https://localhost:" + port 
                                   + "/services/CxfRsSslAsyncProducerTest/");
            return afb;
        }
        
        /*@Bean
        CxfRsEndpoint serviceEndpointWithWrongTrust(SSLContextParameters wrongSslContext, DefaultHostnameVerifier defaultHostnameVerifier) {
            
            CxfRsEndpoint cxfEndpoint = new CxfRsEndpoint();
            cxfEndpoint.setAddress("https://localhost:" + port 
                                   + "/services/CxfRsSslAsyncProducerTest/");
            cxfEndpoint.setHostnameVerifier(defaultHostnameVerifier);
            cxfEndpoint.setSslContextParameters(wrongSslContext);
            cxfEndpoint.setSynchronous(true);
            return cxfEndpoint;
        }
        
        @Bean
        CxfRsEndpoint serviceEndpointWithNoTrust(DefaultHostnameVerifier defaultHostnameVerifier) {
            
            CxfRsEndpoint cxfEndpoint = new CxfRsEndpoint();
            cxfEndpoint.setAddress("https://localhost:" + port 
                                   + "/services/CxfRsSslAsyncProducerTest/");
            cxfEndpoint.setHostnameVerifier(defaultHostnameVerifier);
            cxfEndpoint.setSynchronous(true);
            return cxfEndpoint;
        }*/
        
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct://trust").to("cxfrs:bean:serviceEndpoint?sslContextParameters=#mySslContext&hostnameVerifier=#defaultHostnameVerifier");
                    from("direct://wrongTrust").to("cxfrs:bean:serviceEndpoint?sslContextParameters=#wrongSslContext&hostnameVerifier=#defaultHostnameVerifier");
                    from("direct://noTrust").to("cxfrs:bean:serviceEndpoint?hostnameVerifier=#defaultHostnameVerifier");
                }
            };
        }
    }
    
}
