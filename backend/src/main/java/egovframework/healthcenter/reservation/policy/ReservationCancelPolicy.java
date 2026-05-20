package egovframework.healthcenter.reservation.policy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.mapper.ReservationVO;

@Component
public class ReservationCancelPolicy {

	private static final String RESERVED = "RESERVED";

	public void validateCancelable(MemberPrincipal principal, ReservationVO reservation, LocalDateTime now) {
		if (principal == null || principal.memberId() == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		if (reservation == null) {
			throw new IllegalArgumentException("예약 정보를 찾을 수 없습니다.");
		}
		if (!canAccess(principal, reservation)) {
			throw new IllegalArgumentException("예약 취소 권한이 없습니다.");
		}
		if (!RESERVED.equals(reservation.getStatus())) {
			throw new IllegalArgumentException("현재 상태에서는 예약을 취소할 수 없습니다.");
		}
		LocalDateTime visitAt = LocalDateTime.of(reservation.getSlotDate(), reservation.getStartTime());
		if (now.isAfter(visitAt.minusHours(1))) {
			throw new IllegalArgumentException("예약 취소는 예약 시간 1시간 전까지만 가능합니다.");
		}
	}

	private boolean canAccess(MemberPrincipal principal, ReservationVO reservation) {
		if (principal.memberId().equals(reservation.getMemberId())) {
			return true;
		}
		return isStaffOrAdmin(principal) && principal.healthCenterId() != null
			&& principal.healthCenterId().equals(reservation.getHealthCenterId());
	}

	private boolean isStaffOrAdmin(MemberPrincipal principal) {
		return principal.role() == MemberRole.STAFF || principal.role() == MemberRole.ADMIN;
	}
}
