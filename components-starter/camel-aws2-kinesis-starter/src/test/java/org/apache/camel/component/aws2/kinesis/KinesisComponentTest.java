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
package org.apache.camel.component.aws2.kinesis;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.infra.aws2.clients.AWSSDKClientUtils;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;

//Based on CwComponentIT
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                KinesisComponentTest.class,
                KinesisComponentTest.TestConfiguration.class
        }
)
@Disabled("API rate limit exceeded")
public class KinesisComponentTest extends BaseKinesis {

    @EndpointInject("mock:result")
    private MockEndpoint result;

    private static KinesisClient client;

    @BeforeAll
    static void setUp() {
        client = AWSSDKClientUtils.newKinesisClient();
        client.createStream(
                CreateStreamRequest.builder()
                        .shardCount(1)
                        .streamName("kinesis1")
                        .build());
    }

    @AfterAll
    static void clean() {
        if(client != null) {
            client.close();
        }
    }


    @Test
    public void send() throws Exception {
        result.expectedMessageCount(2);

        template.send("direct:start", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) {
                exchange.getIn().setHeader(Kinesis2Constants.PARTITION_KEY, "partition-1");
                exchange.getIn().setBody("Kinesis Event 1.");
            }
        });

        template.send("direct:start", ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) {
                exchange.getIn().setHeader(Kinesis2Constants.PARTITION_KEY, "partition-1");
                exchange.getIn().setBody("Kinesis Event 2.");
            }
        });

        assertMockEndpointsSatisfied();
    }

    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration extends BaseKinesis.TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    String kinesisEndpointUri = "aws2-kinesis://kinesis1";

                    from("direct:start").to(kinesisEndpointUri);
                    from(kinesisEndpointUri).to("mock:result");
                }
            };
        }
    }
}
