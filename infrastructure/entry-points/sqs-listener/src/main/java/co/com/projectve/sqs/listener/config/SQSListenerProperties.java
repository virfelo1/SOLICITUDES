package co.com.projectve.sqs.listener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "entrypoint.sqs")
public record SQSListenerProperties(
    String region,
    String queueUrl,
    String endpoint
) {
}
