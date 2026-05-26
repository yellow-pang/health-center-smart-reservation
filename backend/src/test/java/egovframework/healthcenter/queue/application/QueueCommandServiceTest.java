package egovframework.healthcenter.queue.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import egovframework.healthcenter.queue.dto.QueueAutoCloseResult;
import egovframework.healthcenter.queue.mapper.QueueTicketMapper;
import egovframework.healthcenter.queue.policy.QueueTicketPolicy;

class QueueCommandServiceTest {

	private final QueueTicketMapper queueTicketMapper = Mockito.mock(QueueTicketMapper.class);
	private final QueueTicketPolicy queueTicketPolicy = new QueueTicketPolicy();
	private final QueueCommandService service = new QueueCommandService(queueTicketMapper, queueTicketPolicy);

	@DisplayName("자동 마감은 기준일에서 보관 일수를 뺀 날짜까지의 미처리 대기표를 NO_SHOW 처리한다")
	@Test
	void autoCloseOverduePendingTicketsClosesPendingTicketsBeforeCutoff() {
		LocalDate runDate = LocalDate.of(2026, 5, 26);
		LocalDateTime cutoffExclusive = LocalDateTime.of(2026, 5, 25, 0, 0);
		when(queueTicketMapper.countOverduePendingTicketsForAutoClose(cutoffExclusive)).thenReturn(3);

		QueueAutoCloseResult result = service.autoCloseOverduePendingTickets(runDate, 2);

		assertEquals(runDate, result.runDate());
		assertEquals(LocalDate.of(2026, 5, 24), result.cutoffDate());
		assertEquals(3, result.closedCount());
		verify(queueTicketMapper).markOverduePendingVisitsNoShow(cutoffExclusive);
		verify(queueTicketMapper).markOverduePendingReservationsNoShow(cutoffExclusive);
		verify(queueTicketMapper).markOverduePendingTicketsNoShow(cutoffExclusive);
	}

	@DisplayName("자동 마감 대상이 없으면 상태 변경 SQL을 실행하지 않는다")
	@Test
	void autoCloseOverduePendingTicketsSkipsUpdatesWhenNoPendingTickets() {
		LocalDate runDate = LocalDate.of(2026, 5, 26);
		LocalDateTime cutoffExclusive = LocalDateTime.of(2026, 5, 25, 0, 0);
		when(queueTicketMapper.countOverduePendingTicketsForAutoClose(cutoffExclusive)).thenReturn(0);

		QueueAutoCloseResult result = service.autoCloseOverduePendingTickets(runDate, 2);

		assertEquals(0, result.closedCount());
		verify(queueTicketMapper, never()).markOverduePendingVisitsNoShow(cutoffExclusive);
		verify(queueTicketMapper, never()).markOverduePendingReservationsNoShow(cutoffExclusive);
		verify(queueTicketMapper, never()).markOverduePendingTicketsNoShow(cutoffExclusive);
	}
}
