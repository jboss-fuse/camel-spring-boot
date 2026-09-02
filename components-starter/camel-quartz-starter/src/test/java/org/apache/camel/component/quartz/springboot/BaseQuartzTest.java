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
package org.apache.camel.component.quartz.springboot;

import org.apache.camel.CamelContext;
import org.apache.camel.api.management.JmxSystemPropertyKeys;
import org.apache.camel.component.quartz.QuartzComponent;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.quartz.Calendar;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.annotation.Bean;

public class BaseQuartzTest {

    static {
        System.setProperty(JmxSystemPropertyKeys.DISABLED, "false");
    }

    protected boolean useJmx() {
        return true;
    }

    protected Calendar getCustomCalendar(CamelContext context) throws SchedulerException {
        QuartzComponent component = context.getComponent("quartz", QuartzComponent.class);
        Scheduler scheduler = component.getScheduler();

        for (var triggerKey : scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup())) {
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (trigger.getCalendarName() != null) {
                return scheduler.getCalendar(trigger.getCalendarName());
            }
        }

        throw new IllegalStateException("No Quartz trigger with a custom calendar");
    }

    @Bean
    CamelContextConfiguration contextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                QuartzComponent quartz = context.getComponent("quartz", QuartzComponent.class);
                quartz.setEnableJmx(useJmx());

            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // do nothing here
            }
        };
    }

}
