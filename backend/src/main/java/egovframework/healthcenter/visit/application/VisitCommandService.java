package egovframework.healthcenter.visit.application;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.common.logging.AuditLogSupport;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.office.mapper.OfficeMapper;
import egovframework.healthcenter.office.mapper.ServiceTypeVO;
import egovframework.healthcenter.queue.mapper.QueueTicketMapper;
import egovframework.healthcenter.queue.mapper.QueueTicketVO;
import egovframework.healthcenter.reservation.mapper.ReservationMapper;
import egovframework.healthcenter.reservation.mapper.ReservationVO;
import egovframework.healthcenter.visit.dto.VisitCheckInRequest;
import egovframework.healthcenter.visit.dto.VisitCheckInResponse;
import egovframework.healthcenter.visit.dto.VisitWalkInRequest;
import egovframework.healthcenter.visit.dto.VisitWalkInResponse;
import egovframework.healthcenter.visit.mapper.VisitMapper;
import egovframework.healthcenter.visit.policy.VisitCheckInPolicy;
import egovframework.healthcenter.visit.policy.VisitWalkInPolicy;

@Service
public class VisitCommandService {

	private static final Logger log = LoggerFactory.getLogger(VisitCommandService.class);

	private final ReservationMapper reservationMapper;
	private final OfficeMapper officeMapper;
	private final VisitMapper visitMapper;
	private final QueueTicketMapper queueTicketMapper;
	private final VisitCheckInPolicy visitCheckInPolicy;
	private final VisitWalkInPolicy visitWalkInPolicy;

	public VisitCommandService(
			ReservationMapper reservationMapper,
			OfficeMapper officeMapper,
			VisitMapper visitMapper,
			QueueTicketMapper queueTicketMapper,
			VisitCheckInPolicy visitCheckInPolicy,
			VisitWalkInPolicy visitWalkInPolicy) {
		this.reservationMapper = reservationMapper;
		this.officeMapper = officeMapper;
		this.visitMapper = visitMapper;
		this.queueTicketMapper = queueTicketMapper;
		this.visitCheckInPolicy = visitCheckInPolicy;
		this.visitWalkInPolicy = visitWalkInPolicy;
	}

	@Transactional
	public VisitCheckInResponse checkIn(MemberPrincipal principal, VisitCheckInRequest request) {
		validatePrincipal(principal);
		validateRequest(request);

		ReservationVO reservation = reservationMapper.selectReservationByNo(request.reservationNo());
		visitCheckInPolicy.validateCheckIn(principal, reservation);

		int updated = reservationMapper.markCheckedIn(reservation.getId());
		if (updated == 0) {
			throw new BusinessException(ErrorCode.VISIT_ALREADY_CHECKED_IN);
		}
		Long visitId = visitMapper.insertReservedVisit(reservation, principal.memberId());
		QueueTicketVO ticket = queueTicketMapper.issueWaitingTicket(
			reservation.getHealthCenterId(),
			visitId,
			reservation.getServiceTypeId()
		);
		log.info(
			"event=visit.checked_in traceId={} memberId={} role={} healthCenterId={} reservationId={} visitId={} queueTicketId={} serviceTypeId={} ticketNumber={} queueStatus={}",
			AuditLogSupport.traceId(),
			AuditLogSupport.memberId(principal),
			AuditLogSupport.role(principal),
			AuditLogSupport.healthCenterId(principal),
			reservation.getId(),
			visitId,
			ticket.getId(),
			reservation.getServiceTypeId(),
			ticket.getTicketNumber(),
			ticket.getStatus()
		);

		return new VisitCheckInResponse(
			visitId,
			ticket.getId(),
			ticket.getTicketNumber(),
			ticket.getStatus()
		);
	}

	@Transactional
	public VisitWalkInResponse walkIn(MemberPrincipal principal, VisitWalkInRequest request) {
		validatePrincipal(principal);
		validateWalkInRequest(request);

		ServiceTypeVO serviceType = officeMapper.selectServiceTypeById(request.serviceTypeId());
		visitWalkInPolicy.validateWalkIn(principal, serviceType);

		Long visitId = visitMapper.insertWalkInVisit(
			principal.healthCenterId(),
			serviceType.getId(),
			principal.memberId(),
			request.visitorName().trim(),
			request.visitorPhone().trim()
		);
		QueueTicketVO ticket = queueTicketMapper.issueWaitingTicket(
			principal.healthCenterId(),
			visitId,
			serviceType.getId()
		);
		log.info(
			"event=visit.walk_in_created traceId={} memberId={} role={} healthCenterId={} visitId={} queueTicketId={} serviceTypeId={} ticketNumber={} queueStatus={}",
			AuditLogSupport.traceId(),
			AuditLogSupport.memberId(principal),
			AuditLogSupport.role(principal),
			AuditLogSupport.healthCenterId(principal),
			visitId,
			ticket.getId(),
			serviceType.getId(),
			ticket.getTicketNumber(),
			ticket.getStatus()
		);

		return new VisitWalkInResponse(
			visitId,
			ticket.getId(),
			ticket.getTicketNumber(),
			ticket.getStatus()
		);
	}

	private void validatePrincipal(MemberPrincipal principal) {
		if (principal == null || principal.memberId() == null) {
			throw new BusinessException(ErrorCode.AUTH_REQUIRED);
		}
	}

	private void validateRequest(VisitCheckInRequest request) {
		if (request == null || request.reservationNo() == null || request.reservationNo().isBlank()) {
			throw new BusinessException(ErrorCode.VISIT_INVALID_REQUEST, "예약번호를 입력하세요.");
		}
	}

	private void validateWalkInRequest(VisitWalkInRequest request) {
		if (request == null || request.serviceTypeId() == null) {
			throw new BusinessException(ErrorCode.SERVICE_TYPE_INVALID_REQUEST, "업무 유형을 선택하세요.");
		}
		if (request.visitorName() == null || request.visitorName().isBlank()) {
			throw new BusinessException(ErrorCode.VISIT_INVALID_REQUEST, "방문자 이름을 입력하세요.");
		}
		if (request.visitorPhone() == null || request.visitorPhone().isBlank()) {
			throw new BusinessException(ErrorCode.VISIT_INVALID_REQUEST, "방문자 전화번호를 입력하세요.");
		}
	}
}
