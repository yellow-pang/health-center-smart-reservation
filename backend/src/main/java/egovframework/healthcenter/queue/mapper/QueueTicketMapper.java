package egovframework.healthcenter.queue.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

@Repository("queueTicketMapper")
public class QueueTicketMapper extends EgovAbstractMapper {

	public QueueTicketVO issueWaitingTicket(Long healthCenterId, Long visitId, Long serviceTypeId) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("visitId", visitId);
		params.put("serviceTypeId", serviceTypeId);
		return selectOne("QueueTicketMapper.issueWaitingTicket", params);
	}

	public List<QueueTicketVO> selectQueueTickets(
			Long healthCenterId,
			Long serviceTypeId,
			String status,
			LocalDateTime fromIssuedAt,
			LocalDateTime toIssuedAt,
			Integer limit) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("serviceTypeId", serviceTypeId);
		params.put("status", status);
		params.put("fromIssuedAt", fromIssuedAt);
		params.put("toIssuedAt", toIssuedAt);
		params.put("limit", limit);
		return selectList("QueueTicketMapper.selectQueueTickets", params);
	}

	public QueueTicketVO selectQueueTicketById(Long queueTicketId) {
		return selectOne("QueueTicketMapper.selectQueueTicketById", queueTicketId);
	}

	public int markCalled(Long queueTicketId) {
		return update("QueueTicketMapper.markCalled", queueTicketId);
	}

	public int markInProgress(Long queueTicketId) {
		return update("QueueTicketMapper.markInProgress", queueTicketId);
	}

	public int markCompleted(Long queueTicketId) {
		return update("QueueTicketMapper.markCompleted", queueTicketId);
	}

	public int markHold(Long queueTicketId) {
		return update("QueueTicketMapper.markHold", queueTicketId);
	}

	public int markNoShow(Long queueTicketId) {
		return update("QueueTicketMapper.markNoShow", queueTicketId);
	}

	public int markCanceled(Long queueTicketId) {
		return update("QueueTicketMapper.markCanceled", queueTicketId);
	}

	public int markVisitInProgress(Long queueTicketId) {
		return update("QueueTicketMapper.markVisitInProgress", queueTicketId);
	}

	public int markVisitCompleted(Long queueTicketId) {
		return update("QueueTicketMapper.markVisitCompleted", queueTicketId);
	}

	public int markVisitNoShow(Long queueTicketId) {
		return update("QueueTicketMapper.markVisitNoShow", queueTicketId);
	}

	public int markVisitCanceled(Long queueTicketId) {
		return update("QueueTicketMapper.markVisitCanceled", queueTicketId);
	}

	public int markReservationCompleted(Long queueTicketId) {
		return update("QueueTicketMapper.markReservationCompleted", queueTicketId);
	}

	public int markReservationNoShow(Long queueTicketId) {
		return update("QueueTicketMapper.markReservationNoShow", queueTicketId);
	}

	public int markReservationCanceled(Long queueTicketId) {
		return update("QueueTicketMapper.markReservationCanceled", queueTicketId);
	}

	public int countPendingTicketsForClose(Long healthCenterId, LocalDate targetDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("targetDate", targetDate);
		return selectOne("QueueTicketMapper.countPendingTicketsForClose", params);
	}

	public int markPendingVisitsNoShow(Long healthCenterId, LocalDate targetDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("targetDate", targetDate);
		return update("QueueTicketMapper.markPendingVisitsNoShow", params);
	}

	public int markPendingReservationsNoShow(Long healthCenterId, LocalDate targetDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("targetDate", targetDate);
		return update("QueueTicketMapper.markPendingReservationsNoShow", params);
	}

	public int markPendingTicketsNoShow(Long healthCenterId, LocalDate targetDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("targetDate", targetDate);
		return update("QueueTicketMapper.markPendingTicketsNoShow", params);
	}

	public int countOverduePendingTicketsForAutoClose(LocalDateTime cutoffExclusive) {
		return selectOne("QueueTicketMapper.countOverduePendingTicketsForAutoClose", cutoffParams(cutoffExclusive));
	}

	public int markOverduePendingVisitsNoShow(LocalDateTime cutoffExclusive) {
		return update("QueueTicketMapper.markOverduePendingVisitsNoShow", cutoffParams(cutoffExclusive));
	}

	public int markOverduePendingReservationsNoShow(LocalDateTime cutoffExclusive) {
		return update("QueueTicketMapper.markOverduePendingReservationsNoShow", cutoffParams(cutoffExclusive));
	}

	public int markOverduePendingTicketsNoShow(LocalDateTime cutoffExclusive) {
		return update("QueueTicketMapper.markOverduePendingTicketsNoShow", cutoffParams(cutoffExclusive));
	}

	private Map<String, Object> cutoffParams(LocalDateTime cutoffExclusive) {
		Map<String, Object> params = new HashMap<>();
		params.put("cutoffExclusive", cutoffExclusive);
		return params;
	}
}
