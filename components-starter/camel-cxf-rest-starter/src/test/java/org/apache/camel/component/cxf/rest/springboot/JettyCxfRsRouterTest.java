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

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSClientFactoryBean;
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSServerFactoryBean;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;


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
        JettyCxfRsRouterTest.class,
        JettyCxfRsRouterTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }
)
public class JettyCxfRsRouterTest extends CxfRsRouterTest {
    
    
     
    private Server server;
    
    
    @Autowired
    CamelContext context;
    
    @BeforeEach
    public void setUp() throws Exception {
        JAXRSServerFactoryBean sfb = new SpringJAXRSServerFactoryBean();
        List<Object> serviceBeans = new ArrayList<Object>();
        serviceBeans.add(new org.apache.camel.component.cxf.jaxrs.testbean.CustomerService());
        sfb.setServiceBeans(serviceBeans);
        sfb.setAddress("http://localhost:" + backendPort + "/services/JettyCxfRsRouterTest/rest");
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
        
    
    @Override
    @Disabled("The test from the parent class is not applicable in this scenario")
    public void testEndpointUris() {
        // Don't test anything here
    }

    
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        @Bean
        public AbstractJAXRSFactoryBean rsClient() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            
            afb.setAddress("http://localhost:" + backendPort + "/services/JettyCxfRsRouterTest/rest");
            afb.setServiceClass(org.apache.camel.component.cxf.jaxrs.testbean.CustomerService.class);
            afb.setLoggingFeatureEnabled(true);
            afb.setSkipFaultLogging(true);
            return afb;
        }
        
        
        
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("jetty://http://localhost:" + port + "/services/CxfRsRouterTest/route?matchOnUriPrefix=true")
                        .to("cxfrs:bean:rsClient?ignoreDeleteMethodMessageBody=true&synchronous=true");
                }
            };
        }
    }
    
}
