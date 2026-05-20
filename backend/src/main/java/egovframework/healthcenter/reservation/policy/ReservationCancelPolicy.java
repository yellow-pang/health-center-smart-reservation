package egovframework.healthcenter.reservation.policy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.mapper.ReservationVO;

@Component
public class ReservationCancelPolicy {

	private static final String RESERVED = "RESERVED";

	public void validateCancelable(MemberPrincipal principal, ReservationVO reservation, LocalDateTime now) {
		if (principal == null || principal.memberId() == null) {
			throw new BusinessException(ErrorCode.AUTH_REQUIRED);
		}
		if (reservation == null) {
			throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
		}
		if (!canAccess(principal, reservation)) {
			throw new BusinessException(ErrorCode.RESERVATION_FORBIDDEN);
		}
		if (!RESERVED.equals(reservation.getStatus())) {
			throw new BusinessException(ErrorCode.RESERVATION_CANCEL_INVALID_STATUS);
		}
		LocalDateTime visitAt = LocalDateTime.of(reservation.getSlotDate(), reservation.getStartTime());
		if (now.isAfter(visitAt.minusHours(1))) {
			throw new BusinessException(ErrorCode.RESERVATION_CANCEL_TIME_EXPIRED);
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
