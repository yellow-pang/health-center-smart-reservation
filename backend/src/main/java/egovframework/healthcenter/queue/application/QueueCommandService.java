package egovframework.healthcenter.queue.application;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.common.logging.AuditLogSupport;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.dto.ClosePendingQueueTicketsResponse;
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
			throw new BusinessException(ErrorCode.QUEUE_INVALID_STATUS, "호출할 수 없는 대기 상태입니다.");
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
			throw new BusinessException(ErrorCode.QUEUE_INVALID_STATUS, "처리 시작할 수 없는 대기 상태입니다.");
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
			throw new BusinessException(ErrorCode.QUEUE_INVALID_STATUS, "완료할 수 없는 대기 상태입니다.");
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
			throw new BusinessException(ErrorCode.QUEUE_INVALID_STATUS, "보류할 수 없는 대기 상태입니다.");
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
			throw new BusinessException(ErrorCode.QUEUE_INVALID_STATUS, "최종 미응답 처리할 수 없는 대기 상태입니다.");
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
			throw new BusinessException(ErrorCode.QUEUE_INVALID_STATUS, "취소할 수 없는 대기 상태입니다.");
		}
		queueTicketMapper.markVisitCanceled(queueTicketId);
		queueTicketMapper.markReservationCanceled(queueTicketId);
		QueueTicketResponse response = QueueTicketResponse.from(queueTicketMapper.selectQueueTicketById(queueTicketId));
		logTransition(principal, "queue.canceled", ticket, response);
		return response;
	}

	@Transactional
	public ClosePendingQueueTicketsResponse closePendingTickets(MemberPrincipal principal, LocalDate date) {
		validateAdmin(principal);
		LocalDate targetDate = date == null ? LocalDate.now() : date;
		int pendingCount = queueTicketMapper.countPendingTicketsForClose(principal.healthCenterId(), targetDate);
		if (pendingCount > 0) {
			queueTicketMapper.markPendingVisitsNoShow(principal.healthCenterId(), targetDate);
			queueTicketMapper.markPendingReservationsNoShow(principal.healthCenterId(), targetDate);
			queueTicketMapper.markPendingTicketsNoShow(principal.healthCenterId(), targetDate);
		}
		log.info(
			"event=queue.pending_closed traceId={} memberId={} role={} healthCenterId={} targetDate={} closedCount={}",
			AuditLogSupport.traceId(),
			AuditLogSupport.memberId(principal),
			AuditLogSupport.role(principal),
			AuditLogSupport.healthCenterId(principal),
			targetDate,
			pendingCount
		);
		return new ClosePendingQueueTicketsResponse(targetDate, pendingCount);
	}

	private QueueTicketVO loadTicket(MemberPrincipal principal, Long queueTicketId) {
		queueTicketPolicy.validateStaff(principal);
		if (queueTicketId == null) {
			throw new BusinessException(ErrorCode.QUEUE_INVALID_REQUEST, "대기표 ID를 입력하세요.");
		}
		QueueTicketVO ticket = queueTicketMapper.selectQueueTicketById(queueTicketId);
		queueTicketPolicy.validateAccess(principal, ticket);
		return ticket;
	}

	private void validateAdmin(MemberPrincipal principal) {
		if (principal == null || principal.memberId() == null) {
			throw new BusinessException(ErrorCode.AUTH_REQUIRED);
		}
		if (principal.healthCenterId() == null) {
			throw new BusinessException(ErrorCode.QUEUE_INVALID_REQUEST, "대기열을 마감할 보건소 정보가 없습니다.");
		}
		if (principal.role() != MemberRole.ADMIN) {
			throw new BusinessException(ErrorCode.QUEUE_FORBIDDEN, "대기열 마감은 관리자만 처리할 수 있습니다.");
		}
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
