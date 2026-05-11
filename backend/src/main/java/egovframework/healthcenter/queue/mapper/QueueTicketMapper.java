package egovframework.healthcenter.queue.mapper;

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

	public List<QueueTicketVO> selectQueueTickets(Long healthCenterId, Long serviceTypeId, String status) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("serviceTypeId", serviceTypeId);
		params.put("status", status);
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

	public int markVisitInProgress(Long queueTicketId) {
		return update("QueueTicketMapper.markVisitInProgress", queueTicketId);
	}

	public int markVisitCompleted(Long queueTicketId) {
		return update("QueueTicketMapper.markVisitCompleted", queueTicketId);
	}

	public int markReservationCompleted(Long queueTicketId) {
		return update("QueueTicketMapper.markReservationCompleted", queueTicketId);
	}
}
