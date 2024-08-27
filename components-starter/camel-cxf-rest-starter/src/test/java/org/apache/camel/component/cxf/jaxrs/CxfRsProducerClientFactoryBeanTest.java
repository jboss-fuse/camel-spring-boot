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
package org.apache.camel.component.cxf.jaxrs;


import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.CXFTestSupport;
import org.apache.camel.component.cxf.jaxrs.testbean.ServiceUtil;
import org.apache.camel.component.cxf.spring.jaxrs.SpringJAXRSClientFactoryBean;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.AbstractJAXRSFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;



@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfRsProducerClientFactoryBeanTest.class,
        CxfRsProducerClientFactoryBeanTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CxfRsProducerClientFactoryBeanTest {

    private int port = CXFTestSupport.getPort1();
    
    
    @Autowired
    CamelContext context;
        
    
    @Test
    public void testProducerInOutInterceptors() throws Exception {
        CxfRsEndpoint e = context.getEndpoint(
                "cxfrs://bean://rsClientHttpInterceptors", CxfRsEndpoint.class);
        CxfRsProducer p = new CxfRsProducer(e);
        CxfRsProducer.ClientFactoryBeanCache cache = p.getClientFactoryBeanCache();
        JAXRSClientFactoryBean bean = cache.get("http://localhost:" + port + "/services/CxfRsProducerClientFactoryBeanInterceptors/");
        List<Interceptor<?>> ins = bean.getInInterceptors();
        assertEquals(1, ins.size());
        assertTrue(ins.get(0) instanceof LoggingInInterceptor);
        List<Interceptor<?>> outs = bean.getOutInterceptors();
        assertEquals(1, outs.size());
        assertTrue(outs.get(0) instanceof LoggingOutInterceptor);
    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        @Bean("service")
        public ServiceUtil bindToRegistry() {
            return new ServiceUtil();
        }
        
        @Bean
        public ServletWebServerFactory servletWebServerFactory() {
            return new UndertowServletWebServerFactory(port);
        }
        
        @Bean
        public AbstractJAXRSFactoryBean rsClientHttpInterceptors() {
            SpringJAXRSClientFactoryBean afb = new SpringJAXRSClientFactoryBean();
            
            afb.setAddress("/CxfRsProducerClientFactoryBeanInterceptors/");
            afb.getInInterceptors().add(new org.apache.cxf.ext.logging.LoggingInInterceptor());
            afb.getOutInterceptors().add(new org.apache.cxf.ext.logging.LoggingOutInterceptor());
            return afb;
        }
        
        
        
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct://http").to("cxfrs:bean:rsClientHttpInterceptors");
                }
            };
        }
    }
    
}
