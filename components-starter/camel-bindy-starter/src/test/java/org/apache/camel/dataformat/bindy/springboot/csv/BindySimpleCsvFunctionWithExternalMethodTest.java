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
package org.apache.camel.dataformat.bindy.springboot.csv;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.dataformat.bindy.util.ConverterUtils;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;


@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        BindySimpleCsvFunctionWithExternalMethodTest.class,
        BindySimpleCsvFunctionWithExternalMethodTest.TestConfiguration.class
    }
)
public class BindySimpleCsvFunctionWithExternalMethodTest {

    @Autowired
    ProducerTemplate template;
    
    @EndpointInject("mock:resultMarshal1")
    private MockEndpoint mockEndPointMarshal1;

    @EndpointInject("mock:resultUnMarshal1")
    private MockEndpoint mockEndPointUnMarshal1;

    @EndpointInject("mock:resultMarshal2")
    private MockEndpoint mockEndPointMarshal2;

    @EndpointInject("mock:resultUnMarshal2")
    private MockEndpoint mockEndPointUnMarshal2;

    public static String replaceToBar(String fooString) {
        return fooString.replaceAll("foo", "bar");
    }

    @Test
    public void testUnMarshallMessage() throws Exception {

        mockEndPointMarshal1.expectedMessageCount(1);
        mockEndPointMarshal1
                .expectedBodiesReceived("\"123\",\"\"\"foo\"\"\",\"10\"" + ConverterUtils.getStringCarriageReturn("WINDOWS"));

        BindyCsvRowFormat7621 body = new BindyCsvRowFormat7621();
        body.setFirstField("123");
        body.setSecondField("\"foo\"");
        body.setNumber(new BigDecimal(10));
        template.sendBody("direct:startMarshal1", body);

        mockEndPointMarshal1.assertIsSatisfied();

        BindyCsvRowFormat7621 model
                = mockEndPointUnMarshal1.getReceivedExchanges().get(0).getIn().getBody(BindyCsvRowFormat7621.class);

        assertEquals("123", model.getFirstField());
        assertEquals("\"bar\"", model.getSecondField());
        assertEquals(new BigDecimal(10), model.getNumber());
    }

    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public static class TestConfiguration {

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    BindyCsvDataFormat camelDataFormat1 = new BindyCsvDataFormat(BindyCsvRowFormat7621.class);

                    from("direct:startMarshal1")
                            .marshal(camelDataFormat1)
                            .to("mock:resultMarshal1")
                            .to("direct:middle1");

                    from("direct:middle1")
                            .unmarshal(camelDataFormat1)
                            .to("mock:resultUnMarshal1");
                }
            };
        }
    }
    
    @CsvRecord(separator = ",", quote = "\"", quoting = true, quotingEscaped = false)
    public static class BindyCsvRowFormat7621 implements Serializable {
        private static final long serialVersionUID = 1L;

        @DataField(pos = 1)
        private String firstField;

        @DataField(pos = 2,
                   method = "org.apache.camel.dataformat.bindy.springboot.csv.BindySimpleCsvFunctionWithExternalMethodTest.replaceToBar")
        private String secondField;

        @DataField(pos = 3, pattern = "########.##")
        private BigDecimal number;

        public String getFirstField() {
            return firstField;
        }

        public void setFirstField(String firstField) {
            this.firstField = firstField;
        }

        public String getSecondField() {
            return secondField;
        }

        public void setSecondField(String secondField) {
            this.secondField = secondField;
        }

        public BigDecimal getNumber() {
            return number;
        }

        public void setNumber(BigDecimal number) {
            this.number = number;
        }
    }

}
