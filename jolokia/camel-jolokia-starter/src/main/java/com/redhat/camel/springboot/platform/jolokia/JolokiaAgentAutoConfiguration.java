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
package com.redhat.camel.springboot.platform.jolokia;

import org.jolokia.support.spring.SpringJolokiaAgent;
import org.jolokia.support.spring.SpringJolokiaConfigHolder;
import org.jolokia.support.spring.SpringJolokiaLogHandlerHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Configuration
@ConditionalOnMissingBean(type = "org.jolokia.support.spring.SpringJolokiaAgent")
@EnableConfigurationProperties(JolokiaAgentConfig.class)
@ConditionalOnProperty(name = "camel.jolokia.agent.enabled", havingValue = "true", matchIfMissing = true)
public class JolokiaAgentAutoConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(JolokiaAgentAutoConfiguration.class);

	protected static final String DEFAULT_CA_ON_K8S = "/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt";

	@Autowired
	private JolokiaAgentConfig agentConfig;

	@Bean
	public SpringJolokiaAgent jolokia(final SpringJolokiaConfigHolder configHolder, final SpringJolokiaLogHandlerHolder logHandlerHolder) {
		final SpringJolokiaAgent agent = new SpringJolokiaAgent();
		agent.setLookupConfig(agentConfig.lookupConfig());
		agent.setLookupServices(agentConfig.lookupServices());
		agent.setSystemPropertiesMode(agentConfig.systemPropertiesMode());
		agent.setExposeApplicationContext(agentConfig.exposeApplicationContext());
		agent.setConfig(configHolder);
		agent.setLogHandler(logHandlerHolder);
		return agent;
	}

	@Bean
	@ConditionalOnMissingBean(name = "logHandlerHolder")
	public SpringJolokiaLogHandlerHolder logHandlerHolder() {
		final SpringJolokiaLogHandlerHolder logHandlerHolder = new SpringJolokiaLogHandlerHolder();
		logHandlerHolder.setType("slf4j");
		return logHandlerHolder;
	}

	@Bean
	@ConditionalOnMissingBean(name = "configHolder")
	@ConditionalOnProperty(name = "camel.jolokia.agent.kubernetesDiscover", havingValue = "false")
	public SpringJolokiaConfigHolder configHolder() {
		return loadConfigFromProperties();
	}

	@Bean("configHolder")
	@ConditionalOnMissingBean(name = "configHolder")
	@ConditionalOnProperty(name = "camel.jolokia.agent.kubernetesDiscover", havingValue = "true", matchIfMissing = true)
	public SpringJolokiaConfigHolder k8sConfigHolder() {
		final SpringJolokiaConfigHolder springJolokiaConfigHolder = loadConfigFromProperties();
		final String caCert = agentConfig.kubernetesUseDefaultCa() ? DEFAULT_CA_ON_K8S
				: agentConfig.serverConfig().getOrDefault("caCert", DEFAULT_CA_ON_K8S);
		if (Files.exists(Path.of(caCert))) {
			setDefaultConfigValue(springJolokiaConfigHolder.getConfig(), "protocol", "https");
			setDefaultConfigValue(springJolokiaConfigHolder.getConfig(), "useSslClientAuthentication", "true");
			setDefaultConfigValue(springJolokiaConfigHolder.getConfig(), "caCert", caCert);
		} else {
			LOG.debug("kubernetesDiscover is enabled but the file {} does not exist, no additional properties will be set", caCert);
		}
		return springJolokiaConfigHolder;
	}

	private SpringJolokiaConfigHolder loadConfigFromProperties() {
		final SpringJolokiaConfigHolder springJolokiaConfigHolder = new SpringJolokiaConfigHolder();
		setDefaultConfigValue(agentConfig.serverConfig(), "host", "0.0.0.0");
		setDefaultConfigValue(agentConfig.serverConfig(), "autoStart", "true");
		springJolokiaConfigHolder.setConfig(agentConfig.serverConfig());
		return springJolokiaConfigHolder;
	}

	private void setDefaultConfigValue(Map<String, String> config, String key, String defaultValue) {
		final String configValue = config.getOrDefault(key, defaultValue);
		LOG.debug("jolokia config set {} = {}", key, configValue);
		config.put(key, configValue);
	}
}
