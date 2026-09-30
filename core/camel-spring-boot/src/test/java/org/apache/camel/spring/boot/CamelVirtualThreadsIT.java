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
package org.apache.camel.spring.boot;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.junit6.CamelSpringBootTest;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Camel virtual-thread configuration.
 *
 * This test runs in a clean JVM so that ThreadType observes the required system property during its static initialization.
 */
@Isolated
@EnableAutoConfiguration
@CamelSpringBootTest
@SpringBootTest(classes = { CamelAutoConfiguration.class, CamelVirtualThreadsIT.class },
                properties = {
                    "spring.threads.virtual.enabled=true",
                    "camel.threads.virtual.enabled=true"
                })
public class CamelVirtualThreadsIT {

    @Autowired
    CamelContext context;

    private static boolean isVirtualThread(Thread thread) {
        try {
            Method isVirtual = Thread.class.getMethod("isVirtual");
            return (boolean) isVirtual.invoke(thread);
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    public void testCamelVirtualThreadPropertyIsSet() {
        assertThat(System.getProperty("camel.threads.virtual.enabled"))
                .as("camel.threads.virtual.enabled should be set before Camel starts")
                .isEqualTo("true");
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_21)
    public void testCamelManagedExecutorRunsOnVirtualThread() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Thread> executingThread = new AtomicReference<>();
        ExecutorService executor = context.getExecutorServiceManager().newSingleThreadExecutor(this, "virtualThreadTest");

        try {
            executor.submit(() -> {
                executingThread.set(Thread.currentThread());
                latch.countDown();
            });

            Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> latch.getCount() == 0);

            Thread thread = executingThread.get();
            assertThat(thread).isNotNull();
            assertThat(isVirtualThread(thread))
                    .as("Camel-managed executors should use virtual threads when camel.threads.virtual.enabled=true")
                    .isTrue();
        } finally {
            executor.shutdownNow();
        }
    }
}
