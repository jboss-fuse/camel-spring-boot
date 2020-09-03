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
package org.apache.camel.component.ehcache.springboot;

import org.apache.camel.CamelContext;
import org.apache.camel.component.infinispan.InfinispanComponent;
import org.apache.camel.component.infinispan.springboot.customizer.EmbeddedCacheManagerCustomizer;
import org.apache.camel.component.infinispan.springboot.customizer.RemoteCacheManagerCustomizer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootApplication
@SpringBootTest(
    classes = {
        CacheManagerCustomizerWithoutCacheManagerTest.TestConfiguration.class
    },
    properties = {
        "debug=false",
        "infinispan.embedded.enabled=false",
        "infinispan.remote.enabled=false",
        "camel.component.infinispan.customizer.embedded-cache-manager.enabled=true",
        "camel.component.infinispan.customizer.remote-cache-manager.enabled=true"
    })
public class CacheManagerCustomizerWithoutCacheManagerTest {
    @Autowired
    InfinispanComponent component;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    CamelContext context;

    @Test
    public void testComponentConfiguration() {
        InfinispanComponent component = context.getComponent("infinispan", InfinispanComponent.class);

        Assert.assertNotNull(component);
        Assert.assertNull(component.getConfiguration().getCacheContainer());
        Assert.assertEquals(0, applicationContext.getBeansOfType(EmbeddedCacheManagerCustomizer.class).size());
        Assert.assertEquals(0, applicationContext.getBeansOfType(RemoteCacheManagerCustomizer.class).size());
    }

    @Configuration
    public static class TestConfiguration {
    }
}