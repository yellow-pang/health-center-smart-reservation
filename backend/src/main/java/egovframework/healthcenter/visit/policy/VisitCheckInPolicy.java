package egovframework.healthcenter.visit.policy;

import org.springframework.stereotype.Component;

import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.mapper.ReservationVO;

@Component
public class VisitCheckInPolicy {

	public void validateCheckIn(MemberPrincipal principal, ReservationVO reservation) {
		if (reservation == null) {
			throw new IllegalArgumentException("예약 정보를 찾을 수 없습니다.");
		}
		if (principal.healthCenterId() == null || !principal.healthCenterId().equals(reservation.getHealthCenterId())) {
			throw new IllegalArgumentException("해당 예약을 체크인할 권한이 없습니다.");
		}
		if (principal.role() != MemberRole.STAFF && principal.role() != MemberRole.ADMIN) {
			throw new IllegalArgumentException("체크인 권한이 없습니다.");
		}
		if (!"RESERVED".equals(reservation.getStatus())) {
			throw new IllegalArgumentException("이미 체크인했거나 체크인할 수 없는 예약입니다.");
		}
	}
}
