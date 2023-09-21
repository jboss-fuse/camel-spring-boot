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
package org.apache.camel.component.aws2.sns.springboot;

import org.apache.camel.component.aws2.sns.Sns2Component;
import org.apache.camel.component.aws2.sns.Sns2Configuration;
import org.apache.camel.spring.boot.ComponentConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.core.Protocol;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * Send messages to AWS Simple Notification Topic.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@ConfigurationProperties(prefix = "camel.component.aws2-sns")
public class Sns2ComponentConfiguration
        extends
            ComponentConfigurationPropertiesCommon {

    /**
     * Whether to enable auto configuration of the aws2-sns component. This is
     * enabled by default.
     */
    private Boolean enabled;
    /**
     * Setting the autocreation of the topic
     */
    private Boolean autoCreateTopic = false;
    /**
     * Component configuration. The option is a
     * org.apache.camel.component.aws2.sns.Sns2Configuration type.
     */
    private Sns2Configuration configuration;
    /**
     * The ID of an AWS-managed customer master key (CMK) for Amazon SNS or a
     * custom CMK.
     */
    private String kmsMasterKeyId;
    /**
     * Whether the producer should be started lazy (on the first message). By
     * starting lazy you can use this to allow CamelContext and routes to
     * startup in situations where a producer may otherwise fail during starting
     * and cause the route to fail being started. By deferring this startup to
     * be lazy then the startup failure can be handled during routing messages
     * via Camel's routing error handlers. Beware that when the first message is
     * processed then creating and starting the producer may take a little time
     * and prolong the total processing time of the processing.
     */
    private Boolean lazyStartProducer = false;
    /**
     * Only for FIFO Topic. Strategy for setting the messageDeduplicationId on
     * the message. Can be one of the following options: useExchangeId,
     * useContentBasedDeduplication. For the useContentBasedDeduplication
     * option, no messageDeduplicationId will be set on the message.
     */
    private String messageDeduplicationIdStrategy = "useExchangeId";
    /**
     * Only for FIFO Topic. Strategy for setting the messageGroupId on the
     * message. Can be one of the following options: useConstant, useExchangeId,
     * usePropertyValue. For the usePropertyValue option, the value of property
     * CamelAwsMessageGroupId will be used.
     */
    private String messageGroupIdStrategy;
    /**
     * The message structure to use such as json
     */
    private String messageStructure;
    /**
     * Set the need for overidding the endpoint. This option needs to be used in
     * combination with uriEndpointOverride option
     */
    private Boolean overrideEndpoint = false;
    /**
     * The policy for this topic. Is loaded by default from classpath, but you
     * can prefix with classpath:, file:, or http: to load the resource from
     * different systems.
     */
    private String policy;
    /**
     * The ARN endpoint to subscribe to
     */
    private String queueArn;
    /**
     * The region in which SNS client needs to work. When using this parameter,
     * the configuration will expect the lowercase name of the region (for
     * example ap-east-1) You'll need to use the name Region.EU_WEST_1.id()
     */
    private String region;
    /**
     * Define if Server Side Encryption is enabled or not on the topic
     */
    private Boolean serverSideEncryptionEnabled = false;
    /**
     * The subject which is used if the message header 'CamelAwsSnsSubject' is
     * not present.
     */
    private String subject;
    /**
     * Define if the subscription between SNS Topic and SQS must be done or not
     */
    private Boolean subscribeSNStoSQS = false;
    /**
     * Set the overriding uri endpoint. This option needs to be used in
     * combination with overrideEndpoint option
     */
    private String uriEndpointOverride;
    /**
     * To use the AmazonSNS as the client. The option is a
     * software.amazon.awssdk.services.sns.SnsClient type.
     */
    private SnsClient amazonSNSClient;
    /**
     * Whether autowiring is enabled. This is used for automatic autowiring
     * options (the option must be marked as autowired) by looking up in the
     * registry to find if there is a single instance of matching type, which
     * then gets configured on the component. This can be used for automatic
     * configuring JDBC data sources, JMS connection factories, AWS Clients,
     * etc.
     */
    private Boolean autowiredEnabled = true;
    /**
     * Used for enabling or disabling all consumer based health checks from this
     * component
     */
    private Boolean healthCheckConsumerEnabled = true;
    /**
     * Used for enabling or disabling all producer based health checks from this
     * component. Notice: Camel has by default disabled all producer based
     * health-checks. You can turn on producer checks globally by setting
     * camel.health.producersEnabled=true.
     */
    private Boolean healthCheckProducerEnabled = true;
    /**
     * To define a proxy host when instantiating the SNS client
     */
    private String proxyHost;
    /**
     * To define a proxy port when instantiating the SNS client
     */
    private Integer proxyPort;
    /**
     * To define a proxy protocol when instantiating the SNS client
     */
    private Protocol proxyProtocol = Protocol.HTTPS;
    /**
     * Amazon AWS Access Key
     */
    private String accessKey;
    /**
     * If using a profile credentials provider this parameter will set the
     * profile name
     */
    private String profileCredentialsName;
    /**
     * Amazon AWS Secret Key
     */
    private String secretKey;
    /**
     * If we want to trust all certificates in case of overriding the endpoint
     */
    private Boolean trustAllCertificates = false;
    /**
     * Set whether the SNS client should expect to load credentials on an AWS
     * infra instance or to expect static credentials to be passed in.
     */
    private Boolean useDefaultCredentialsProvider = false;
    /**
     * Set whether the SNS client should expect to load credentials through a
     * profile credentials provider.
     */
    private Boolean useProfileCredentialsProvider = false;

    public Boolean getAutoCreateTopic() {
        return autoCreateTopic;
    }

    public void setAutoCreateTopic(Boolean autoCreateTopic) {
        this.autoCreateTopic = autoCreateTopic;
    }

    public Sns2Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Sns2Configuration configuration) {
        this.configuration = configuration;
    }

    public String getKmsMasterKeyId() {
        return kmsMasterKeyId;
    }

    public void setKmsMasterKeyId(String kmsMasterKeyId) {
        this.kmsMasterKeyId = kmsMasterKeyId;
    }

    public Boolean getLazyStartProducer() {
        return lazyStartProducer;
    }

    public void setLazyStartProducer(Boolean lazyStartProducer) {
        this.lazyStartProducer = lazyStartProducer;
    }

    public String getMessageDeduplicationIdStrategy() {
        return messageDeduplicationIdStrategy;
    }

    public void setMessageDeduplicationIdStrategy(
            String messageDeduplicationIdStrategy) {
        this.messageDeduplicationIdStrategy = messageDeduplicationIdStrategy;
    }

    public String getMessageGroupIdStrategy() {
        return messageGroupIdStrategy;
    }

    public void setMessageGroupIdStrategy(String messageGroupIdStrategy) {
        this.messageGroupIdStrategy = messageGroupIdStrategy;
    }

    public String getMessageStructure() {
        return messageStructure;
    }

    public void setMessageStructure(String messageStructure) {
        this.messageStructure = messageStructure;
    }

    public Boolean getOverrideEndpoint() {
        return overrideEndpoint;
    }

    public void setOverrideEndpoint(Boolean overrideEndpoint) {
        this.overrideEndpoint = overrideEndpoint;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getQueueArn() {
        return queueArn;
    }

    public void setQueueArn(String queueArn) {
        this.queueArn = queueArn;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Boolean getServerSideEncryptionEnabled() {
        return serverSideEncryptionEnabled;
    }

    public void setServerSideEncryptionEnabled(
            Boolean serverSideEncryptionEnabled) {
        this.serverSideEncryptionEnabled = serverSideEncryptionEnabled;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Boolean getSubscribeSNStoSQS() {
        return subscribeSNStoSQS;
    }

    public void setSubscribeSNStoSQS(Boolean subscribeSNStoSQS) {
        this.subscribeSNStoSQS = subscribeSNStoSQS;
    }

    public String getUriEndpointOverride() {
        return uriEndpointOverride;
    }

    public void setUriEndpointOverride(String uriEndpointOverride) {
        this.uriEndpointOverride = uriEndpointOverride;
    }

    public SnsClient getAmazonSNSClient() {
        return amazonSNSClient;
    }

    public void setAmazonSNSClient(SnsClient amazonSNSClient) {
        this.amazonSNSClient = amazonSNSClient;
    }

    public Boolean getAutowiredEnabled() {
        return autowiredEnabled;
    }

    public void setAutowiredEnabled(Boolean autowiredEnabled) {
        this.autowiredEnabled = autowiredEnabled;
    }

    public Boolean getHealthCheckConsumerEnabled() {
        return healthCheckConsumerEnabled;
    }

    public void setHealthCheckConsumerEnabled(Boolean healthCheckConsumerEnabled) {
        this.healthCheckConsumerEnabled = healthCheckConsumerEnabled;
    }

    public Boolean getHealthCheckProducerEnabled() {
        return healthCheckProducerEnabled;
    }

    public void setHealthCheckProducerEnabled(Boolean healthCheckProducerEnabled) {
        this.healthCheckProducerEnabled = healthCheckProducerEnabled;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public Protocol getProxyProtocol() {
        return proxyProtocol;
    }

    public void setProxyProtocol(Protocol proxyProtocol) {
        this.proxyProtocol = proxyProtocol;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getProfileCredentialsName() {
        return profileCredentialsName;
    }

    public void setProfileCredentialsName(String profileCredentialsName) {
        this.profileCredentialsName = profileCredentialsName;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Boolean getTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(Boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    public Boolean getUseDefaultCredentialsProvider() {
        return useDefaultCredentialsProvider;
    }

    public void setUseDefaultCredentialsProvider(
            Boolean useDefaultCredentialsProvider) {
        this.useDefaultCredentialsProvider = useDefaultCredentialsProvider;
    }

    public Boolean getUseProfileCredentialsProvider() {
        return useProfileCredentialsProvider;
    }

    public void setUseProfileCredentialsProvider(
            Boolean useProfileCredentialsProvider) {
        this.useProfileCredentialsProvider = useProfileCredentialsProvider;
    }
}