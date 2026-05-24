package egovframework.healthcenter.queue.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.dto.QueueTicketResponse;
import egovframework.healthcenter.queue.mapper.QueueTicketMapper;
import egovframework.healthcenter.queue.policy.QueueTicketPolicy;

@Service
public class QueueQueryService {

	private static final int DEFAULT_LIMIT = 200;
	private static final int MAX_LIMIT = 500;

	private final QueueTicketMapper queueTicketMapper;
	private final QueueTicketPolicy queueTicketPolicy;

	public QueueQueryService(QueueTicketMapper queueTicketMapper, QueueTicketPolicy queueTicketPolicy) {
		this.queueTicketMapper = queueTicketMapper;
		this.queueTicketPolicy = queueTicketPolicy;
	}

	@Transactional(readOnly = true)
	public List<QueueTicketResponse> findQueueTickets(
			MemberPrincipal principal,
			Long serviceTypeId,
			String status,
			LocalDate date,
			Integer limit) {
		queueTicketPolicy.validateStaff(principal);
		LocalDate targetDate = date == null ? LocalDate.now() : date;
		return queueTicketMapper.selectQueueTickets(
				principal.healthCenterId(),
				serviceTypeId,
				normalizeStatus(status),
				targetDate.atStartOfDay(),
				targetDate.plusDays(1).atStartOfDay(),
				normalizeLimit(limit))
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

	private int normalizeLimit(Integer limit) {
		if (limit == null) {
			return DEFAULT_LIMIT;
		}
		if (limit < 1) {
			return DEFAULT_LIMIT;
		}
		return Math.min(limit, MAX_LIMIT);
	}
}
