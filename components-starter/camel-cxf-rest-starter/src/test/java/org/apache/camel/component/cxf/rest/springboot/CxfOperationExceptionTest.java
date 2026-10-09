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

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.CXFTestSupport;
import org.apache.camel.component.cxf.jaxrs.testbean.Customer;
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSClientFactoryBean;
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSServerFactoryBean;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        CxfOperationExceptionTest.class,
        CxfOperationExceptionTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }
)
public class CxfOperationExceptionTest {
    
    


    private int port = CXFTestSupport.getPort1();
    private int backendPort = CXFTestSupport.getPort2();
    
     
    private Server server;
    
    
    @Autowired
    ProducerTemplate template;
    
    @BeforeEach
    public void setUp() throws Exception {
        JAXRSServerFactoryBean sfb = new SpringJAXRSServerFactoryBean();
        List<Object> serviceBeans = new ArrayList<Object>();
        serviceBeans.add(new org.apache.camel.component.cxf.jaxrs.testbean.CustomerService());
        sfb.setServiceBeans(serviceBeans);
        sfb.setAddress("http://localhost:" + backendPort + "/services/CxfOperationExceptionTest/rest");
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
    public void testRestServerDirectlyAddCustomer() {
        Customer input = new Customer();
        input.setName("Donald Duck");

        // we cannot convert directly to Customer as we need camel-jaxb
        assertThrows(CamelExecutionException.class, () -> template
                .requestBodyAndHeader("cxfrs:http://localhost:" + port
                                      + "/CxfOperationExceptionTest/route/customerservice/customers?throwExceptionOnFailure=true",
                        input,
                        Exchange.HTTP_METHOD, "POST", String.class));
    }

    @Test
    public void testRestServerDirectlyAddCustomerWithExceptionsTurnedOff() {
        Customer input = new Customer();
        input.setName("Donald Duck");

        // we cannot convert directly to Customer as we need camel-jaxb
        String response = template.requestBodyAndHeader("cxfrs:bean:rsClient?throwExceptionOnFailure=false", input,
                Exchange.HTTP_METHOD, "POST", String.class);

        assertNotNull(response);
        assertTrue(response.contains("CxfOperationExceptionTest/rest"));
    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        
                
        @Bean
        public AbstractJAXRSFactoryBean rsServer() {
            SpringJAXRSServerFactoryBean afb = new SpringJAXRSServerFactoryBean();
            
            afb.setAddress("http://localhost:" + port + "/services/CxfOperationExceptionTest/route");
            afb.setServiceClass(org.apache.camel.component.cxf.jaxrs.testbean.CustomerService.class);
            afb.setLoggingFeatureEnabled(true);
            return afb;
        }
        
        @Bean
        public AbstractJAXRSFactoryBean rsClient() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            
            afb.setAddress("http://localhost:" + port + "/services/CxfOperationExceptionTest/rest");
            afb.setServiceClass(org.apache.camel.component.cxf.jaxrs.testbean.CustomerService.class);
            afb.setLoggingFeatureEnabled(true);
            return afb;
        }
        
        
        
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("cxfrs:bean:rsServer").setHeader("CamelCxfRsUsingHttpAPI", constant(true))
                        .to("cxfrs:bean:rsClient");
                }
            };
        }
    }
    
}
