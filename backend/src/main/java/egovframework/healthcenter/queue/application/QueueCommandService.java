package egovframework.healthcenter.queue.application;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.logging.AuditLogSupport;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.dto.QueueTicketResponse;
import egovframework.healthcenter.queue.mapper.QueueTicketMapper;
import egovframework.healthcenter.queue.mapper.QueueTicketVO;
import egovframework.healthcenter.queue.policy.QueueTicketPolicy;

@Service
public class QueueCommandService {

	private static final Logger log = LoggerFactory.getLogger(QueueCommandService.class);

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
		QueueTicketResponse response = QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
		logTransition(principal, "queue.called", ticket, response);
		return response;
	}

	@Transactional
	public QueueTicketResponse start(MemberPrincipal principal, Long queueTicketId) {
		QueueTicketVO ticket = loadTicket(principal, queueTicketId);
		queueTicketPolicy.validateStart(ticket);
		if (queueTicketMapper.markInProgress(queueTicketId) == 0) {
			throw new IllegalArgumentException("처리 시작할 수 없는 대기 상태입니다.");
		}
		queueTicketMapper.markVisitInProgress(queueTicketId);
		QueueTicketResponse response = QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
		logTransition(principal, "queue.started", ticket, response);
		return response;
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
		QueueTicketResponse response = QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
		logTransition(principal, "queue.completed", ticket, response);
		return response;
	}

	@Transactional
	public QueueTicketResponse hold(MemberPrincipal principal, Long queueTicketId) {
		QueueTicketVO ticket = loadTicket(principal, queueTicketId);
		queueTicketPolicy.validateHold(ticket);
		if (queueTicketMapper.markHold(queueTicketId) == 0) {
			throw new IllegalArgumentException("보류할 수 없는 대기 상태입니다.");
		}
		QueueTicketResponse response = QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
		logTransition(principal, "queue.held", ticket, response);
		return response;
	}

	@Transactional
	public QueueTicketResponse noShow(MemberPrincipal principal, Long queueTicketId) {
		QueueTicketVO ticket = loadTicket(principal, queueTicketId);
		queueTicketPolicy.validateNoShow(ticket);
		if (queueTicketMapper.markNoShow(queueTicketId) == 0) {
			throw new IllegalArgumentException("최종 미응답 처리할 수 없는 대기 상태입니다.");
		}
		queueTicketMapper.markVisitNoShow(queueTicketId);
		queueTicketMapper.markReservationNoShow(queueTicketId);
		QueueTicketResponse response = QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
		logTransition(principal, "queue.no_show", ticket, response);
		return response;
	}

	@Transactional
	public QueueTicketResponse cancel(MemberPrincipal principal, Long queueTicketId) {
		QueueTicketVO ticket = loadTicket(principal, queueTicketId);
		queueTicketPolicy.validateCancel(ticket);
		if (queueTicketMapper.markCanceled(queueTicketId) == 0) {
			throw new IllegalArgumentException("취소할 수 없는 대기 상태입니다.");
		}
		queueTicketMapper.markVisitCanceled(queueTicketId);
		queueTicketMapper.markReservationCanceled(queueTicketId);
		QueueTicketResponse response = QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
		logTransition(principal, "queue.canceled", ticket, response);
		return response;
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

	private void logTransition(MemberPrincipal principal, String event, QueueTicketVO before, QueueTicketResponse after) {
		log.info(
			"event={} traceId={} memberId={} role={} healthCenterId={} queueTicketId={} visitId={} serviceTypeId={} ticketNumber={} previousStatus={} status={}",
			event,
			AuditLogSupport.traceId(),
			AuditLogSupport.memberId(principal),
			AuditLogSupport.role(principal),
			AuditLogSupport.healthCenterId(principal),
			after.queueTicketId(),
			after.visitId(),
			after.serviceTypeId(),
			after.ticketNumber(),
			before.getStatus(),
			after.status()
		);
	}
}
