package egovframework.healthcenter.queue.policy;

import org.springframework.stereotype.Component;

import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.mapper.QueueTicketVO;

@Component
public class QueueTicketPolicy {

	public void validateStaff(MemberPrincipal principal) {
		if (principal == null || principal.memberId() == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		if (principal.healthCenterId() == null) {
			throw new IllegalArgumentException("대기열을 처리할 보건소 정보가 없습니다.");
		}
		if (principal.role() != MemberRole.STAFF && principal.role() != MemberRole.ADMIN) {
			throw new IllegalArgumentException("대기열 처리 권한이 없습니다.");
		}
	}

	public void validateAccess(MemberPrincipal principal, QueueTicketVO ticket) {
		if (ticket == null) {
			throw new IllegalArgumentException("대기표 정보를 찾을 수 없습니다.");
		}
		if (!principal.healthCenterId().equals(ticket.getHealthCenterId())) {
			throw new IllegalArgumentException("해당 대기표를 처리할 권한이 없습니다.");
		}
	}

	public void validateCall(QueueTicketVO ticket) {
		if (!"WAITING".equals(ticket.getStatus()) && !"HOLD".equals(ticket.getStatus())) {
			throw new IllegalArgumentException("호출할 수 없는 대기 상태입니다.");
		}
	}

	public void validateStart(QueueTicketVO ticket) {
		if (!"CALLED".equals(ticket.getStatus())) {
			throw new IllegalArgumentException("처리 시작할 수 없는 대기 상태입니다.");
		}
	}

	public void validateComplete(QueueTicketVO ticket) {
		if (!"IN_PROGRESS".equals(ticket.getStatus())) {
			throw new IllegalArgumentException("완료할 수 없는 대기 상태입니다.");
		}
	}

	public void validateHold(QueueTicketVO ticket) {
		if (!"CALLED".equals(ticket.getStatus())) {
			throw new IllegalArgumentException("보류할 수 없는 대기 상태입니다.");
		}
	}

	public void validateNoShow(QueueTicketVO ticket) {
		if (!"HOLD".equals(ticket.getStatus())) {
			throw new IllegalArgumentException("최종 미응답 처리할 수 없는 대기 상태입니다.");
		}
	}

	public void validateCancel(QueueTicketVO ticket) {
		if (!"WAITING".equals(ticket.getStatus())
				&& !"CALLED".equals(ticket.getStatus())
				&& !"HOLD".equals(ticket.getStatus())) {
			throw new IllegalArgumentException("취소할 수 없는 대기 상태입니다.");
		}
	}
}
