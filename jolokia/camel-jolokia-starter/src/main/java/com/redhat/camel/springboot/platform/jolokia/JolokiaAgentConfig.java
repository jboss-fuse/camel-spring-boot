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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "camel.jolokia.agent")
public class JolokiaAgentConfig {

	private boolean lookupConfig = false;
	private boolean lookupServices = false;
	private String systemPropertiesMode = "never";
	private boolean exposeApplicationContext = false;
	private Map<String, String> serverConfig = new HashMap<>();
	private boolean kubernetesDiscover = true;
	private boolean kubernetesUseDefaultCa = true;

	public boolean lookupConfig() {
		return lookupConfig;
	}

	public void setLookupConfig(final boolean lookupConfig) {
		this.lookupConfig = lookupConfig;
	}

	public boolean lookupServices() {
		return lookupServices;
	}

	public void setLookupServices(final boolean lookupServices) {
		this.lookupServices = lookupServices;
	}

	public String systemPropertiesMode() {
		return systemPropertiesMode;
	}

	public void setSystemPropertiesMode(final String systemPropertiesMode) {
		this.systemPropertiesMode = systemPropertiesMode;
	}

	public boolean exposeApplicationContext() {
		return exposeApplicationContext;
	}

	public void setExposeApplicationContext(final boolean exposeApplicationContext) {
		this.exposeApplicationContext = exposeApplicationContext;
	}

	public Map<String, String> serverConfig() {
		return serverConfig;
	}

	public void setServerConfig(final Map<String, String> serverConfig) {
		this.serverConfig = serverConfig;
	}

	public boolean kubernetesDiscover() {
		return kubernetesDiscover;
	}

	public void setKubernetesDiscover(final boolean kubernetesDiscover) {
		this.kubernetesDiscover = kubernetesDiscover;
	}

	public boolean kubernetesUseDefaultCa() {
		return kubernetesUseDefaultCa;
	}

	public void setKubernetesUseDefaultCa(final boolean kubernetesUseDefaultCa) {
		this.kubernetesUseDefaultCa = kubernetesUseDefaultCa;
	}
}
