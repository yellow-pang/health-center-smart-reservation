package egovframework.healthcenter.queue.batch;

import java.time.LocalDate;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import egovframework.healthcenter.queue.application.QueueCommandService;
import egovframework.healthcenter.queue.dto.QueueAutoCloseResult;

@Component
@ConditionalOnProperty(
	name = "Healthcenter.Queue.AutoClose.Enabled",
	havingValue = "true",
	matchIfMissing = true
)
public class QueueAutoCloseScheduler {

	private static final Logger log = LoggerFactory.getLogger(QueueAutoCloseScheduler.class);

	private final QueueCommandService queueCommandService;
	private final ZoneId zoneId;
	private final int retentionDays;

	public QueueAutoCloseScheduler(
			QueueCommandService queueCommandService,
			@Value("${Globals.TimeZone:Asia/Seoul}") String timeZone,
			@Value("${Healthcenter.Queue.AutoClose.RetentionDays:2}") int retentionDays) {
		this.queueCommandService = queueCommandService;
		this.zoneId = ZoneId.of(timeZone);
		this.retentionDays = retentionDays;
	}

	@Scheduled(
		cron = "${Healthcenter.Queue.AutoClose.Cron:0 10 18 * * *}",
		zone = "${Globals.TimeZone:Asia/Seoul}"
	)
	public void closeOverduePendingQueueTickets() {
		QueueAutoCloseResult result = queueCommandService.autoCloseOverduePendingTickets(LocalDate.now(zoneId), retentionDays);
		log.info(
			"event=queue.pending_auto_close_scheduler_completed runDate={} cutoffDate={} closedCount={}",
			result.runDate(),
			result.cutoffDate(),
			result.closedCount()
		);
	}
}
