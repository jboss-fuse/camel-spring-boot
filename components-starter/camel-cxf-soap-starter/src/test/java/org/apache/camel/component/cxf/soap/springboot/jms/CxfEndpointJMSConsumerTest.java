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
package org.apache.camel.component.cxf.soap.springboot.jms;




import javax.xml.namespace.QName;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.component.cxf.spring.jaxws.CxfSpringEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.infra.artemis.services.ArtemisTCPAllProtocolsInfraService;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.apache.camel.test.spring.junit6.CamelSpringBootTest;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.apache.hello_world_soap_http.Greeter;


@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfEndpointJMSConsumerTest.class,
        CxfEndpointJMSConsumerTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }
)
public class CxfEndpointJMSConsumerTest {
    private static ArtemisTCPAllProtocolsInfraService broker;
    private String namespace = "http://apache.org/hello_world_soap_http";
    private QName serviceName = new QName(namespace, "SOAPService");
    private QName endpointName = new QName(namespace, "SoapPort");

    @BeforeAll
    public static void startBroker() {
        broker = new ArtemisTCPAllProtocolsInfraService();
        broker.initialize();
    }

    @AfterAll
    public static void stopBroker() {
        if (broker != null) {
            broker.shutdown();
        }
    }

    // Compute address lazily to ensure broker is initialized
    private String getAddress() {
        // Replace 0.0.0.0 with localhost so clients can connect
        String serviceAddress = broker.serviceAddress().replace("0.0.0.0", "localhost");
        return "jms:jndi:dynamicQueues/test.cxf.jmstransport.queue"
                + "?jndiInitialContextFactory"
                + "=org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
                + "&jndiConnectionFactoryName=ConnectionFactory&jndiURL="
                + serviceAddress;
    }



    @Test
    @Disabled("Test infrastructure incompatibility: ArtemisServiceFactory no longer exists, " +
              "and ArtemisTCPAllProtocolsInfraService does not work with CXF JMS transport. " +
              "Needs update to use compatible broker setup for CXF JMS testing.")
    public void testInvocation() {
        String address = getAddress();
        System.out.println("============================================");
        System.out.println("Broker service address: " + broker.serviceAddress());
        System.out.println("Full JMS address: " + address);
        System.out.println("============================================");

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(Greeter.class);
        factory.setAddress(address);
        Greeter greeter = factory.create(Greeter.class);
        String response = greeter.greetMe("Camel");
        assertEquals("Hello Camel", response, "Get a wrong response");
    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {
        
        @Bean
        CxfEndpoint jmsEndpoint() {
            CxfSpringEndpoint cxfEndpoint = new CxfSpringEndpoint();
            cxfEndpoint.setServiceNameAsQName(serviceName);
            cxfEndpoint.setEndpointNameAsQName(endpointName);
            cxfEndpoint.setServiceClass(org.apache.hello_world_soap_http.Greeter.class);
            cxfEndpoint.setAddress(getAddress());
            cxfEndpoint.getInInterceptors().add(new org.apache.cxf.ext.logging.LoggingInInterceptor());
            cxfEndpoint.getOutInterceptors().add(new org.apache.cxf.ext.logging.LoggingOutInterceptor());
            return cxfEndpoint;
        }

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("cxf:bean:jmsEndpoint").process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // just set the response for greetme operation here
                            String me = exchange.getIn().getBody(String.class);
                            exchange.getMessage().setBody("Hello " + me);
                        }
                    });
                }
            };
        }
    }
    
    
}
