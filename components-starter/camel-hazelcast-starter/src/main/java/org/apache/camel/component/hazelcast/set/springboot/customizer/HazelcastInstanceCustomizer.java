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
package org.apache.camel.component.hazelcast.set.springboot.customizer;

import org.apache.camel.component.hazelcast.set.HazelcastSetComponent;
import org.apache.camel.component.hazelcast.springboot.customizer.AbstractHazelcastInstanceCustomizer;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(HazelcastInstanceCustomizer.NestedConditions.class)
@AutoConfigureAfter(CamelAutoConfiguration.class)
@EnableConfigurationProperties(HazelcastInstanceCustomizerConfiguration.class)
public class HazelcastInstanceCustomizer extends AbstractHazelcastInstanceCustomizer<HazelcastSetComponent, HazelcastInstanceCustomizerConfiguration> {
    @Override
    public String getId() {
        return "camel.component.hazelcast-set.customizer.hazelcast-instance";
    }
}
