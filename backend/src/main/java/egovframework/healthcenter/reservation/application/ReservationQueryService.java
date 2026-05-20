package egovframework.healthcenter.reservation.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.dto.ReservationResponse;
import egovframework.healthcenter.reservation.mapper.ReservationMapper;
import egovframework.healthcenter.reservation.mapper.ReservationVO;

@Service
public class ReservationQueryService {

	private final ReservationMapper reservationMapper;

	public ReservationQueryService(ReservationMapper reservationMapper) {
		this.reservationMapper = reservationMapper;
	}

	@Transactional(readOnly = true)
	public List<ReservationResponse> findMyReservations(MemberPrincipal principal) {
		validatePrincipal(principal);
		return reservationMapper.selectReservationsByMemberId(principal.memberId())
			.stream()
			.map(ReservationResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public ReservationResponse findReservationDetail(MemberPrincipal principal, Long reservationId) {
		validatePrincipal(principal);
		if (reservationId == null || reservationId < 1) {
			throw new BusinessException(ErrorCode.RESERVATION_INVALID_REQUEST);
		}

		ReservationVO reservation = reservationMapper.selectReservationById(reservationId);
		if (reservation == null) {
			throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
		}
		validateReadable(principal, reservation);
		return ReservationResponse.from(reservation);
	}

	private void validatePrincipal(MemberPrincipal principal) {
		if (principal == null || principal.memberId() == null) {
			throw new BusinessException(ErrorCode.AUTH_REQUIRED);
		}
	}

	private void validateReadable(MemberPrincipal principal, ReservationVO reservation) {
		if (principal.memberId().equals(reservation.getMemberId())) {
			return;
		}
		if (isStaffOrAdmin(principal) && principal.healthCenterId() != null
				&& principal.healthCenterId().equals(reservation.getHealthCenterId())) {
			return;
		}
		throw new BusinessException(ErrorCode.RESERVATION_FORBIDDEN);
	}

	private boolean isStaffOrAdmin(MemberPrincipal principal) {
		return principal.role() == MemberRole.STAFF || principal.role() == MemberRole.ADMIN;
	}
}
