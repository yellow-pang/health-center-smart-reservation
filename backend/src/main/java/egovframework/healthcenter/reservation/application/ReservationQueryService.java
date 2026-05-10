package egovframework.healthcenter.reservation.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
			throw new IllegalArgumentException("예약 ID가 올바르지 않습니다.");
		}

		ReservationVO reservation = reservationMapper.selectReservationById(reservationId);
		if (reservation == null) {
			throw new IllegalArgumentException("예약 정보를 찾을 수 없습니다.");
		}
		validateReadable(principal, reservation);
		return ReservationResponse.from(reservation);
	}

	private void validatePrincipal(MemberPrincipal principal) {
		if (principal == null || principal.memberId() == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
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
		throw new IllegalArgumentException("예약 조회 권한이 없습니다.");
	}

	private boolean isStaffOrAdmin(MemberPrincipal principal) {
		return principal.role() == MemberRole.STAFF || principal.role() == MemberRole.ADMIN;
	}
}
