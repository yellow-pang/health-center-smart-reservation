package egovframework.healthcenter.queue.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.dto.QueueTicketResponse;
import egovframework.healthcenter.queue.mapper.QueueTicketMapper;
import egovframework.healthcenter.queue.mapper.QueueTicketVO;
import egovframework.healthcenter.queue.policy.QueueTicketPolicy;

@Service
public class QueueCommandService {

	private final QueueTicketMapper queueTicketMapper;
	private final QueueTicketPolicy queueTicketPolicy;

	public QueueCommandService(QueueTicketMapper queueTicketMapper, QueueTicketPolicy queueTicketPolicy) {
		this.queueTicketMapper = queueTicketMapper;
		this.queueTicketPolicy = queueTicketPolicy;
	}

	@Transactional
	public QueueTicketResponse call(MemberPrincipal principal, Long queueTicketId) {
		QueueTicketVO ticket = loadTicket(principal, queueTicketId);
		queueTicketPolicy.validateCall(ticket);
		if (queueTicketMapper.markCalled(queueTicketId) == 0) {
			throw new IllegalArgumentException("호출할 수 없는 대기 상태입니다.");
		}
		return QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
	}

	@Transactional
	public QueueTicketResponse start(MemberPrincipal principal, Long queueTicketId) {
		QueueTicketVO ticket = loadTicket(principal, queueTicketId);
		queueTicketPolicy.validateStart(ticket);
		if (queueTicketMapper.markInProgress(queueTicketId) == 0) {
			throw new IllegalArgumentException("처리 시작할 수 없는 대기 상태입니다.");
		}
		queueTicketMapper.markVisitInProgress(queueTicketId);
		return QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
	}

	@Transactional
	public QueueTicketResponse complete(MemberPrincipal principal, Long queueTicketId) {
		QueueTicketVO ticket = loadTicket(principal, queueTicketId);
		queueTicketPolicy.validateComplete(ticket);
		if (queueTicketMapper.markCompleted(queueTicketId) == 0) {
			throw new IllegalArgumentException("완료할 수 없는 대기 상태입니다.");
		}
		queueTicketMapper.markVisitCompleted(queueTicketId);
		queueTicketMapper.markReservationCompleted(queueTicketId);
		return QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
	}

	@Transactional
	public QueueTicketResponse hold(MemberPrincipal principal, Long queueTicketId) {
		QueueTicketVO ticket = loadTicket(principal, queueTicketId);
		queueTicketPolicy.validateHold(ticket);
		if (queueTicketMapper.markHold(queueTicketId) == 0) {
			throw new IllegalArgumentException("보류할 수 없는 대기 상태입니다.");
		}
		return QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
	}

	private QueueTicketVO loadTicket(MemberPrincipal principal, Long queueTicketId) {
		queueTicketPolicy.validateStaff(principal);
		if (queueTicketId == null) {
			throw new IllegalArgumentException("대기표 ID를 입력하세요.");
		}
		QueueTicketVO ticket = queueTicketMapper.selectQueueTicketById(queueTicketId);
		queueTicketPolicy.validateAccess(principal, ticket);
		return ticket;
	}
}
