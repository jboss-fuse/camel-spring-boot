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
package org.apache.camel.component.ai.tool.springboot;

import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.camel.Component;
import org.apache.camel.Ordered;
import org.apache.camel.component.ai.tool.AiToolComponent;
import org.apache.camel.component.ai.tool.AiToolConfiguration;
import org.apache.camel.spi.ComponentCustomizer;
import org.apache.camel.spring.boot.util.ConditionalOnCamelContextAndAutoConfigurationBeans;
import org.apache.camel.spring.boot.util.ConditionalOnHierarchicalProperties;
import org.apache.camel.spring.boot.util.HierarchicalPropertiesEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;

/**
 * Clears catalog defaults for ai-tool Boolean options that were not explicitly configured.
 */
@AutoConfiguration(after = AiToolComponentAutoConfiguration.class)
@Conditional(ConditionalOnCamelContextAndAutoConfigurationBeans.class)
@ConditionalOnHierarchicalProperties({ "camel.component", "camel.component.ai-tool" })
public class AiToolUnsetHintsAutoConfiguration {

    private static final String PREFIX = "camel.component.ai-tool.";

    private static final Map<String, BiConsumer<AiToolConfiguration, Boolean>> OPTIONS = Map.of(
            "read-only-hint", AiToolConfiguration::setReadOnlyHint,
            "destructive-hint", AiToolConfiguration::setDestructiveHint,
            "idempotent-hint", AiToolConfiguration::setIdempotentHint,
            "open-world-hint", AiToolConfiguration::setOpenWorldHint,
            "return-direct", AiToolConfiguration::setReturnDirect);

    @Lazy
    @Bean
    public ComponentCustomizer resetUnsetAiToolHints(ApplicationContext applicationContext) {
        Binder binder = Binder.get(applicationContext.getEnvironment());
        return new ComponentCustomizer() {
            @Override
            public void configure(String name, Component target) {
                AiToolConfiguration configuration = ((AiToolComponent) target).getConfiguration();
                OPTIONS.forEach((option, setter) -> {
                    if (!binder.bind(PREFIX + option, Boolean.class).isBound()) {
                        // The generated configuration class initializes these values to false.
                        setter.accept(configuration, null);
                    }
                });
            }

            @Override
            public boolean isEnabled(String name, Component target) {
                return target instanceof AiToolComponent
                        && HierarchicalPropertiesEvaluator.evaluate(applicationContext,
                                "camel.component.customizer", "camel.component.ai-tool.customizer");
            }

            @Override
            public int getOrder() {
                return Ordered.LOWEST;
            }
        };
    }
}
