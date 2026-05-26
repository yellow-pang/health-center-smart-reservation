package egovframework.healthcenter.queue.dto;

import java.time.LocalDate;

public record QueueAutoCloseResult(
	LocalDate runDate,
	LocalDate cutoffDate,
	int closedCount
) {
}
