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
package org.apache.camel.component.grok.springboot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.grok.GrokDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.DataFormatCustomizer;
import org.apache.camel.spi.DataFormatFactory;
import org.apache.camel.spi.HasId;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.DataFormatConfigurationProperties;
import org.apache.camel.spring.boot.util.CamelPropertiesHelper;
import org.apache.camel.spring.boot.util.ConditionalOnCamelContextAndAutoConfigurationBeans;
import org.apache.camel.spring.boot.util.GroupCondition;
import org.apache.camel.spring.boot.util.HierarchicalPropertiesEvaluator;
import org.apache.camel.support.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.springboot.maven.SpringBootAutoConfigurationMojo")
@Configuration(proxyBeanMethods = false)
@Conditional({ConditionalOnCamelContextAndAutoConfigurationBeans.class,
        GrokDataFormatAutoConfiguration.GroupConditions.class})
@AutoConfigureAfter(name = "org.apache.camel.spring.boot.CamelAutoConfiguration")
@EnableConfigurationProperties({DataFormatConfigurationProperties.class,
        GrokDataFormatConfiguration.class})
public class GrokDataFormatAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GrokDataFormatAutoConfiguration.class);
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private GrokDataFormatConfiguration configuration;
    @Autowired(required = false)
    private List<DataFormatCustomizer<GrokDataFormat>> customizers;

    static class GroupConditions extends GroupCondition {
        public GroupConditions() {
            super("camel.dataformat", "camel.dataformat.grok");
        }
    }

    @Bean(name = "grok-dataformat-factory")
    @ConditionalOnMissingBean(GrokDataFormat.class)
    public DataFormatFactory configureGrokDataFormatFactory() throws Exception {
        return new DataFormatFactory() {
            @Override
            public DataFormat newInstance() {
                GrokDataFormat dataformat = new GrokDataFormat();
                if (CamelContextAware.class
                        .isAssignableFrom(GrokDataFormat.class)) {
                    CamelContextAware contextAware = CamelContextAware.class
                            .cast(dataformat);
                    if (contextAware != null) {
                        contextAware.setCamelContext(camelContext);
                    }
                }
                try {
                    Map<String, Object> parameters = new HashMap<>();
                    IntrospectionSupport.getProperties(configuration,
                            parameters, null, false);
                    CamelPropertiesHelper.setCamelProperties(camelContext,
                            dataformat, parameters, false);
                } catch (Exception e) {
                    throw new RuntimeCamelException(e);
                }
                if (ObjectHelper.isNotEmpty(customizers)) {
                    for (DataFormatCustomizer<GrokDataFormat> customizer : customizers) {
                        boolean useCustomizer = (customizer instanceof HasId)
                                ? HierarchicalPropertiesEvaluator.evaluate(
                                        applicationContext.getEnvironment(),
                                        "camel.dataformat.customizer",
                                        "camel.dataformat.grok.customizer",
                                        ((HasId) customizer).getId())
                                : HierarchicalPropertiesEvaluator.evaluate(
                                        applicationContext.getEnvironment(),
                                        "camel.dataformat.customizer",
                                        "camel.dataformat.grok.customizer");
                        if (useCustomizer) {
                            LOGGER.debug(
                                    "Configure dataformat {}, with customizer {}",
                                    dataformat, customizer);
                            customizer.customize(dataformat);
                        }
                    }
                }
                return dataformat;
            }
        };
    }
}