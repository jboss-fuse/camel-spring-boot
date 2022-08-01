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
package org.apache.camel.component.debezium.springboot;

import java.util.Map;
import javax.annotation.Generated;
import org.apache.camel.component.debezium.configuration.Db2ConnectorEmbeddedDebeziumConfiguration;
import org.apache.camel.spring.boot.ComponentConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Capture changes from a Oracle database.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.springboot.maven.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.component.debezium-db2")
public class DebeziumDb2ComponentConfiguration
        extends
            ComponentConfigurationPropertiesCommon {

    /**
     * Whether to enable auto configuration of the debezium-db2 component. This
     * is enabled by default.
     */
    private Boolean enabled;
    /**
     * Additional properties for debezium components in case they can't be set
     * directly on the camel configurations (e.g: setting Kafka Connect
     * properties needed by Debezium engine, for example setting
     * KafkaOffsetBackingStore), the properties have to be prefixed with
     * additionalProperties.. E.g:
     * additionalProperties.transactional.id=12345&additionalProperties.schema.registry.url=http://localhost:8811/avro
     */
    private Map<String, Object> additionalProperties;
    /**
     * Allows for bridging the consumer to the Camel routing Error Handler,
     * which mean any exceptions occurred while the consumer is trying to pickup
     * incoming messages, or the likes, will now be processed as a message and
     * handled by the routing Error Handler. By default the consumer will use
     * the org.apache.camel.spi.ExceptionHandler to deal with exceptions, that
     * will be logged at WARN or ERROR level and ignored.
     */
    private Boolean bridgeErrorHandler = false;
    /**
     * Allow pre-configured Configurations to be set. The option is a
     * org.apache.camel.component.debezium.configuration.Db2ConnectorEmbeddedDebeziumConfiguration type.
     */
    private Db2ConnectorEmbeddedDebeziumConfiguration configuration;
    /**
     * The Converter class that should be used to serialize and deserialize key
     * data for offsets. The default is JSON converter.
     */
    private String internalKeyConverter = "org.apache.kafka.connect.json.JsonConverter";
    /**
     * The Converter class that should be used to serialize and deserialize
     * value data for offsets. The default is JSON converter.
     */
    private String internalValueConverter = "org.apache.kafka.connect.json.JsonConverter";
    /**
     * The name of the Java class of the commit policy. It defines when offsets
     * commit has to be triggered based on the number of events processed and
     * the time elapsed since the last commit. This class must implement the
     * interface 'OffsetCommitPolicy'. The default is a periodic commit policy
     * based upon time intervals.
     */
    private String offsetCommitPolicy = "io.debezium.embedded.spi.OffsetCommitPolicy.PeriodicCommitOffsetPolicy";
    /**
     * Maximum number of milliseconds to wait for records to flush and partition
     * offset data to be committed to offset storage before cancelling the
     * process and restoring the offset data to be committed in a future
     * attempt. The default is 5 seconds. The option is a long type.
     */
    private Long offsetCommitTimeoutMs = 5000L;
    /**
     * Interval at which to try committing offsets. The default is 1 minute. The
     * option is a long type.
     */
    private Long offsetFlushIntervalMs = 60000L;
    /**
     * The name of the Java class that is responsible for persistence of
     * connector offsets.
     */
    private String offsetStorage = "org.apache.kafka.connect.storage.FileOffsetBackingStore";
    /**
     * Path to file where offsets are to be stored. Required when offset.storage
     * is set to the FileOffsetBackingStore.
     */
    private String offsetStorageFileName;
    /**
     * The number of partitions used when creating the offset storage topic.
     * Required when offset.storage is set to the 'KafkaOffsetBackingStore'.
     */
    private Integer offsetStoragePartitions;
    /**
     * Replication factor used when creating the offset storage topic. Required
     * when offset.storage is set to the KafkaOffsetBackingStore
     */
    private Integer offsetStorageReplicationFactor;
    /**
     * The name of the Kafka topic where offsets are to be stored. Required when
     * offset.storage is set to the KafkaOffsetBackingStore.
     */
    private String offsetStorageTopic;
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
     * Regular expressions matching columns to exclude from change events
     * (deprecated, use column.exclude.list instead)
     */
    private String columnBlacklist;
    /**
     * Regular expressions matching columns to exclude from change events
     */
    private String columnExcludeList;
    /**
     * The name of the database from which the connector should capture changes
     */
    private String databaseDbname;
    /**
     * The name of the DatabaseHistory class that should be used to store and
     * recover database schema changes. The configuration properties for the
     * history are prefixed with the 'database.history.' string.
     */
    private String databaseHistory = "class io.debezium.relational.history.KafkaDatabaseHistory";
    /**
     * The path to the file that will be used to record the database history
     */
    private String databaseHistoryFileFilename;
    /**
     * A list of host/port pairs that the connector will use for establishing
     * the initial connection to the Kafka cluster for retrieving database
     * schema history previously stored by the connector. This should point to
     * the same Kafka cluster used by the Kafka Connect process.
     */
    private String databaseHistoryKafkaBootstrapServers;
    /**
     * The number of attempts in a row that no data are returned from Kafka
     * before recover completes. The maximum amount of time to wait after
     * receiving no data is (recovery.attempts) x (recovery.poll.interval.ms).
     */
    private Integer databaseHistoryKafkaRecoveryAttempts = 100;
    /**
     * The number of milliseconds to wait while polling for persisted data
     * during recovery. The option is a int type.
     */
    private Integer databaseHistoryKafkaRecoveryPollIntervalMs = 100;
    /**
     * The name of the topic for the database schema history
     */
    private String databaseHistoryKafkaTopic;
    /**
     * Resolvable hostname or IP address of the database server.
     */
    private String databaseHostname;
    /**
     * Password of the database user to be used when connecting to the database.
     */
    private String databasePassword;
    /**
     * Port of the database server.
     */
    private Integer databasePort = 50000;
    /**
     * Unique name that identifies the database server and all recorded offsets,
     * and that is used as a prefix for all schemas and topics. Each distinct
     * installation should have a separate namespace and be monitored by at most
     * one Debezium connector.
     */
    private String databaseServerName;
    /**
     * Name of the database user to be used when connecting to the database.
     */
    private String databaseUser;
    /**
     * Specify how DECIMAL and NUMERIC columns should be represented in change
     * events, including:'precise' (the default) uses java.math.BigDecimal to
     * represent values, which are encoded in the change events using a binary
     * representation and Kafka Connect's
     * 'org.apache.kafka.connect.data.Decimal' type; 'string' uses string to
     * represent values; 'double' represents values using Java's 'double', which
     * may not offer the precision but will be far easier to use in consumers.
     */
    private String decimalHandlingMode = "precise";
    /**
     * Specify how failures during processing of events (i.e. when encountering
     * a corrupted event) should be handled, including:'fail' (the default) an
     * exception indicating the problematic event and its position is raised,
     * causing the connector to be stopped; 'warn' the problematic event and its
     * position will be logged and the event will be skipped;'ignore' the
     * problematic event will be skipped.
     */
    private String eventProcessingFailureHandlingMode = "fail";
    /**
     * Length of an interval in milli-seconds in in which the connector
     * periodically sends heartbeat messages to a heartbeat topic. Use 0 to
     * disable heartbeat messages. Disabled by default. The option is a int
     * type.
     */
    private Integer heartbeatIntervalMs = 0;
    /**
     * The prefix that is used to name heartbeat topics.Defaults to
     * __debezium-heartbeat.
     */
    private String heartbeatTopicsPrefix = "__debezium-heartbeat";
    /**
     * Maximum size of each batch of source records. Defaults to 2048.
     */
    private Integer maxBatchSize = 2048;
    /**
     * Maximum size of the queue for change events read from the database log
     * but not yet recorded or forwarded. Defaults to 8192, and should always be
     * larger than the maximum batch size.
     */
    private Integer maxQueueSize = 8192;
    /**
     * Time to wait for new change events to appear after receiving no events,
     * given in milliseconds. Defaults to 500 ms. The option is a long type.
     */
    private Long pollIntervalMs = 500L;
    /**
     * A delay period before a snapshot will begin, given in milliseconds.
     * Defaults to 0 ms. The option is a long type.
     */
    private Long snapshotDelayMs = 0L;
    /**
     * The maximum number of records that should be loaded into memory while
     * performing a snapshot
     */
    private Integer snapshotFetchSize;
    /**
     * The criteria for running a snapshot upon startup of the connector.
     * Options include: 'initial' (the default) to specify the connector should
     * run a snapshot only when no offsets are available for the logical server
     * name; 'schema_only' to specify the connector should run a snapshot of the
     * schema when no offsets are available for the logical server name.
     */
    private String snapshotMode = "initial";
    /**
     * This property contains a comma-separated list of fully-qualified tables
     * (DB_NAME.TABLE_NAME) or (SCHEMA_NAME.TABLE_NAME), depending on
     * thespecific connectors. Select statements for the individual tables are
     * specified in further configuration properties, one for each table,
     * identified by the id
     * 'snapshot.select.statement.overrides.DB_NAME.TABLE_NAME' or
     * 'snapshot.select.statement.overrides.SCHEMA_NAME.TABLE_NAME',
     * respectively. The value of those properties is the select statement to
     * use when retrieving data from the specific table during snapshotting. A
     * possible use case for large append-only tables is setting a specific
     * point where to start (resume) snapshotting, in case a previous
     * snapshotting was interrupted.
     */
    private String snapshotSelectStatementOverrides;
    /**
     * A version of the format of the publicly visible source part in the
     * message
     */
    private String sourceStructVersion = "v2";
    /**
     * A comma-separated list of regular expressions that match the
     * fully-qualified names of tables to be excluded from monitoring
     * (deprecated, use table.exclude.list instead)
     */
    private String tableBlacklist;
    /**
     * A comma-separated list of regular expressions that match the
     * fully-qualified names of tables to be excluded from monitoring
     */
    private String tableExcludeList;
    /**
     * Flag specifying whether built-in tables should be ignored.
     */
    private Boolean tableIgnoreBuiltin = true;
    /**
     * The tables for which changes are to be captured
     */
    private String tableIncludeList;
    /**
     * The tables for which changes are to be captured (deprecated, use
     * table.include.list instead)
     */
    private String tableWhitelist;
    /**
     * Time, date, and timestamps can be represented with different kinds of
     * precisions, including:'adaptive' (the default) bases the precision of
     * time, date, and timestamp values on the database column's precision;
     * 'adaptive_time_microseconds' like 'adaptive' mode, but TIME fields always
     * use microseconds precision;'connect' always represents time, date, and
     * timestamp values using Kafka Connect's built-in representations for Time,
     * Date, and Timestamp, which uses millisecond precision regardless of the
     * database columns' precision .
     */
    private String timePrecisionMode = "adaptive";

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Boolean getBridgeErrorHandler() {
        return bridgeErrorHandler;
    }

    public void setBridgeErrorHandler(Boolean bridgeErrorHandler) {
        this.bridgeErrorHandler = bridgeErrorHandler;
    }

    public Db2ConnectorEmbeddedDebeziumConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(
            Db2ConnectorEmbeddedDebeziumConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getInternalKeyConverter() {
        return internalKeyConverter;
    }

    public void setInternalKeyConverter(String internalKeyConverter) {
        this.internalKeyConverter = internalKeyConverter;
    }

    public String getInternalValueConverter() {
        return internalValueConverter;
    }

    public void setInternalValueConverter(String internalValueConverter) {
        this.internalValueConverter = internalValueConverter;
    }

    public String getOffsetCommitPolicy() {
        return offsetCommitPolicy;
    }

    public void setOffsetCommitPolicy(String offsetCommitPolicy) {
        this.offsetCommitPolicy = offsetCommitPolicy;
    }

    public Long getOffsetCommitTimeoutMs() {
        return offsetCommitTimeoutMs;
    }

    public void setOffsetCommitTimeoutMs(Long offsetCommitTimeoutMs) {
        this.offsetCommitTimeoutMs = offsetCommitTimeoutMs;
    }

    public Long getOffsetFlushIntervalMs() {
        return offsetFlushIntervalMs;
    }

    public void setOffsetFlushIntervalMs(Long offsetFlushIntervalMs) {
        this.offsetFlushIntervalMs = offsetFlushIntervalMs;
    }

    public String getOffsetStorage() {
        return offsetStorage;
    }

    public void setOffsetStorage(String offsetStorage) {
        this.offsetStorage = offsetStorage;
    }

    public String getOffsetStorageFileName() {
        return offsetStorageFileName;
    }

    public void setOffsetStorageFileName(String offsetStorageFileName) {
        this.offsetStorageFileName = offsetStorageFileName;
    }

    public Integer getOffsetStoragePartitions() {
        return offsetStoragePartitions;
    }

    public void setOffsetStoragePartitions(Integer offsetStoragePartitions) {
        this.offsetStoragePartitions = offsetStoragePartitions;
    }

    public Integer getOffsetStorageReplicationFactor() {
        return offsetStorageReplicationFactor;
    }

    public void setOffsetStorageReplicationFactor(
            Integer offsetStorageReplicationFactor) {
        this.offsetStorageReplicationFactor = offsetStorageReplicationFactor;
    }

    public String getOffsetStorageTopic() {
        return offsetStorageTopic;
    }

    public void setOffsetStorageTopic(String offsetStorageTopic) {
        this.offsetStorageTopic = offsetStorageTopic;
    }

    public Boolean getAutowiredEnabled() {
        return autowiredEnabled;
    }

    public void setAutowiredEnabled(Boolean autowiredEnabled) {
        this.autowiredEnabled = autowiredEnabled;
    }

    public String getColumnBlacklist() {
        return columnBlacklist;
    }

    public void setColumnBlacklist(String columnBlacklist) {
        this.columnBlacklist = columnBlacklist;
    }

    public String getColumnExcludeList() {
        return columnExcludeList;
    }

    public void setColumnExcludeList(String columnExcludeList) {
        this.columnExcludeList = columnExcludeList;
    }

    public String getDatabaseDbname() {
        return databaseDbname;
    }

    public void setDatabaseDbname(String databaseDbname) {
        this.databaseDbname = databaseDbname;
    }

    public String getDatabaseHistory() {
        return databaseHistory;
    }

    public void setDatabaseHistory(String databaseHistory) {
        this.databaseHistory = databaseHistory;
    }

    public String getDatabaseHistoryFileFilename() {
        return databaseHistoryFileFilename;
    }

    public void setDatabaseHistoryFileFilename(
            String databaseHistoryFileFilename) {
        this.databaseHistoryFileFilename = databaseHistoryFileFilename;
    }

    public String getDatabaseHistoryKafkaBootstrapServers() {
        return databaseHistoryKafkaBootstrapServers;
    }

    public void setDatabaseHistoryKafkaBootstrapServers(
            String databaseHistoryKafkaBootstrapServers) {
        this.databaseHistoryKafkaBootstrapServers = databaseHistoryKafkaBootstrapServers;
    }

    public Integer getDatabaseHistoryKafkaRecoveryAttempts() {
        return databaseHistoryKafkaRecoveryAttempts;
    }

    public void setDatabaseHistoryKafkaRecoveryAttempts(
            Integer databaseHistoryKafkaRecoveryAttempts) {
        this.databaseHistoryKafkaRecoveryAttempts = databaseHistoryKafkaRecoveryAttempts;
    }

    public Integer getDatabaseHistoryKafkaRecoveryPollIntervalMs() {
        return databaseHistoryKafkaRecoveryPollIntervalMs;
    }

    public void setDatabaseHistoryKafkaRecoveryPollIntervalMs(
            Integer databaseHistoryKafkaRecoveryPollIntervalMs) {
        this.databaseHistoryKafkaRecoveryPollIntervalMs = databaseHistoryKafkaRecoveryPollIntervalMs;
    }

    public String getDatabaseHistoryKafkaTopic() {
        return databaseHistoryKafkaTopic;
    }

    public void setDatabaseHistoryKafkaTopic(String databaseHistoryKafkaTopic) {
        this.databaseHistoryKafkaTopic = databaseHistoryKafkaTopic;
    }

    public String getDatabaseHostname() {
        return databaseHostname;
    }

    public void setDatabaseHostname(String databaseHostname) {
        this.databaseHostname = databaseHostname;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public Integer getDatabasePort() {
        return databasePort;
    }

    public void setDatabasePort(Integer databasePort) {
        this.databasePort = databasePort;
    }

    public String getDatabaseServerName() {
        return databaseServerName;
    }

    public void setDatabaseServerName(String databaseServerName) {
        this.databaseServerName = databaseServerName;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDecimalHandlingMode() {
        return decimalHandlingMode;
    }

    public void setDecimalHandlingMode(String decimalHandlingMode) {
        this.decimalHandlingMode = decimalHandlingMode;
    }

    public String getEventProcessingFailureHandlingMode() {
        return eventProcessingFailureHandlingMode;
    }

    public void setEventProcessingFailureHandlingMode(
            String eventProcessingFailureHandlingMode) {
        this.eventProcessingFailureHandlingMode = eventProcessingFailureHandlingMode;
    }

    public Integer getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    public void setHeartbeatIntervalMs(Integer heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }

    public String getHeartbeatTopicsPrefix() {
        return heartbeatTopicsPrefix;
    }

    public void setHeartbeatTopicsPrefix(String heartbeatTopicsPrefix) {
        this.heartbeatTopicsPrefix = heartbeatTopicsPrefix;
    }

    public Integer getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(Integer maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public Integer getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(Integer maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public Long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(Long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public Long getSnapshotDelayMs() {
        return snapshotDelayMs;
    }

    public void setSnapshotDelayMs(Long snapshotDelayMs) {
        this.snapshotDelayMs = snapshotDelayMs;
    }

    public Integer getSnapshotFetchSize() {
        return snapshotFetchSize;
    }

    public void setSnapshotFetchSize(Integer snapshotFetchSize) {
        this.snapshotFetchSize = snapshotFetchSize;
    }

    public String getSnapshotMode() {
        return snapshotMode;
    }

    public void setSnapshotMode(String snapshotMode) {
        this.snapshotMode = snapshotMode;
    }

    public String getSnapshotSelectStatementOverrides() {
        return snapshotSelectStatementOverrides;
    }

    public void setSnapshotSelectStatementOverrides(
            String snapshotSelectStatementOverrides) {
        this.snapshotSelectStatementOverrides = snapshotSelectStatementOverrides;
    }

    public String getSourceStructVersion() {
        return sourceStructVersion;
    }

    public void setSourceStructVersion(String sourceStructVersion) {
        this.sourceStructVersion = sourceStructVersion;
    }

    public String getTableBlacklist() {
        return tableBlacklist;
    }

    public void setTableBlacklist(String tableBlacklist) {
        this.tableBlacklist = tableBlacklist;
    }

    public String getTableExcludeList() {
        return tableExcludeList;
    }

    public void setTableExcludeList(String tableExcludeList) {
        this.tableExcludeList = tableExcludeList;
    }

    public Boolean getTableIgnoreBuiltin() {
        return tableIgnoreBuiltin;
    }

    public void setTableIgnoreBuiltin(Boolean tableIgnoreBuiltin) {
        this.tableIgnoreBuiltin = tableIgnoreBuiltin;
    }

    public String getTableIncludeList() {
        return tableIncludeList;
    }

    public void setTableIncludeList(String tableIncludeList) {
        this.tableIncludeList = tableIncludeList;
    }

    public String getTableWhitelist() {
        return tableWhitelist;
    }

    public void setTableWhitelist(String tableWhitelist) {
        this.tableWhitelist = tableWhitelist;
    }

    public String getTimePrecisionMode() {
        return timePrecisionMode;
    }

    public void setTimePrecisionMode(String timePrecisionMode) {
        this.timePrecisionMode = timePrecisionMode;
    }
}