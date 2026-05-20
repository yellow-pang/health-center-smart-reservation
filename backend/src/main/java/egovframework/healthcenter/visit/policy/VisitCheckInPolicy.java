package egovframework.healthcenter.visit.policy;

import org.springframework.stereotype.Component;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.mapper.ReservationVO;

@Component
public class VisitCheckInPolicy {

	public void validateCheckIn(MemberPrincipal principal, ReservationVO reservation) {
		if (reservation == null) {
			throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
		}
		if (principal.healthCenterId() == null || !principal.healthCenterId().equals(reservation.getHealthCenterId())) {
			throw new BusinessException(ErrorCode.VISIT_FORBIDDEN, "해당 예약을 체크인할 권한이 없습니다.");
		}
		if (principal.role() != MemberRole.STAFF && principal.role() != MemberRole.ADMIN) {
			throw new BusinessException(ErrorCode.VISIT_FORBIDDEN, "체크인 권한이 없습니다.");
		}
		if (!"RESERVED".equals(reservation.getStatus())) {
			throw new BusinessException(ErrorCode.VISIT_ALREADY_CHECKED_IN);
		}
	}
}
