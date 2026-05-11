package egovframework.healthcenter.visit.policy;

import org.springframework.stereotype.Component;

import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.office.mapper.ServiceTypeVO;

@Component
public class VisitWalkInPolicy {

	public void validateWalkIn(MemberPrincipal principal, ServiceTypeVO serviceType) {
		if (principal == null || principal.memberId() == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		if (principal.role() != MemberRole.STAFF && principal.role() != MemberRole.ADMIN) {
			throw new IllegalArgumentException("현장 접수 권한이 없습니다.");
		}
		if (principal.healthCenterId() == null) {
			throw new IllegalArgumentException("현장 접수할 보건소 정보가 없습니다.");
		}
		if (serviceType == null || !serviceType.isActive()) {
			throw new IllegalArgumentException("업무 유형을 찾을 수 없습니다.");
		}
		if (!principal.healthCenterId().equals(serviceType.getHealthCenterId())) {
			throw new IllegalArgumentException("해당 업무 유형으로 현장 접수할 권한이 없습니다.");
		}
	}
}
