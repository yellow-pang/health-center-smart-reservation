package egovframework.healthcenter.queue.mapper;

import java.util.HashMap;
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
}
