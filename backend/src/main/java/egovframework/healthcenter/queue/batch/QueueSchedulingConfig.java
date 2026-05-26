package egovframework.healthcenter.queue.batch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
	name = "Healthcenter.Queue.AutoClose.Enabled",
	havingValue = "true",
	matchIfMissing = true
)
public class QueueSchedulingConfig {
}
