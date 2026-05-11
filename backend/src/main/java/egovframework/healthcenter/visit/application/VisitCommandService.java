package egovframework.healthcenter.visit.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.mapper.QueueTicketMapper;
import egovframework.healthcenter.queue.mapper.QueueTicketVO;
import egovframework.healthcenter.reservation.mapper.ReservationMapper;
import egovframework.healthcenter.reservation.mapper.ReservationVO;
import egovframework.healthcenter.visit.dto.VisitCheckInRequest;
import egovframework.healthcenter.visit.dto.VisitCheckInResponse;
import egovframework.healthcenter.visit.mapper.VisitMapper;
import egovframework.healthcenter.visit.policy.VisitCheckInPolicy;

@Service
public class VisitCommandService {

	private final ReservationMapper reservationMapper;
	private final VisitMapper visitMapper;
	private final QueueTicketMapper queueTicketMapper;
	private final VisitCheckInPolicy visitCheckInPolicy;

	public VisitCommandService(
			ReservationMapper reservationMapper,
			VisitMapper visitMapper,
			QueueTicketMapper queueTicketMapper,
			VisitCheckInPolicy visitCheckInPolicy) {
		this.reservationMapper = reservationMapper;
		this.visitMapper = visitMapper;
		this.queueTicketMapper = queueTicketMapper;
		this.visitCheckInPolicy = visitCheckInPolicy;
	}

	@Transactional
	public VisitCheckInResponse checkIn(MemberPrincipal principal, VisitCheckInRequest request) {
		validatePrincipal(principal);
		validateRequest(request);

		ReservationVO reservation = reservationMapper.selectReservationByNo(request.reservationNo());
		visitCheckInPolicy.validateCheckIn(principal, reservation);

		int updated = reservationMapper.markCheckedIn(reservation.getId());
		if (updated == 0) {
			throw new IllegalArgumentException("이미 체크인했거나 체크인할 수 없는 예약입니다.");
		}
		Long visitId = visitMapper.insertReservedVisit(reservation, principal.memberId());
		QueueTicketVO ticket = queueTicketMapper.issueWaitingTicket(
			reservation.getHealthCenterId(),
			visitId,
			reservation.getServiceTypeId()
		);

		return new VisitCheckInResponse(
			visitId,
			ticket.getId(),
			ticket.getTicketNumber(),
			ticket.getStatus()
		);
	}

	private void validatePrincipal(MemberPrincipal principal) {
		if (principal == null || principal.memberId() == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
	}

	private void validateRequest(VisitCheckInRequest request) {
		if (request == null || request.reservationNo() == null || request.reservationNo().isBlank()) {
			throw new IllegalArgumentException("예약번호를 입력하세요.");
		}
	}
}
