package egovframework.healthcenter.queue.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.dto.QueueTicketResponse;
import egovframework.healthcenter.queue.mapper.QueueTicketMapper;
import egovframework.healthcenter.queue.policy.QueueTicketPolicy;

@Service
public class QueueQueryService {

	private final QueueTicketMapper queueTicketMapper;
	private final QueueTicketPolicy queueTicketPolicy;

	public QueueQueryService(QueueTicketMapper queueTicketMapper, QueueTicketPolicy queueTicketPolicy) {
		this.queueTicketMapper = queueTicketMapper;
		this.queueTicketPolicy = queueTicketPolicy;
	}

	@Transactional(readOnly = true)
	public List<QueueTicketResponse> findQueueTickets(MemberPrincipal principal, Long serviceTypeId, String status) {
		queueTicketPolicy.validateStaff(principal);
		return queueTicketMapper.selectQueueTickets(principal.healthCenterId(), serviceTypeId, normalizeStatus(status))
			.stream()
			.map(QueueTicketResponse::from)
			.toList();
	}

	private String normalizeStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		return status.trim().toUpperCase();
	}
}
