package egovframework.healthcenter.queue.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.mapper.QueueTicketVO;

class QueueTicketPolicyTest {

	private final QueueTicketPolicy policy = new QueueTicketPolicy();

	@DisplayName("로그인 정보가 없으면 대기열 처리를 거부한다")
	@Test
	void validateStaffRequiresAuthentication() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateStaff(null)
		);

		assertEquals(ErrorCode.AUTH_REQUIRED, exception.errorCode());
	}

	@DisplayName("직원 또는 관리자가 아니면 대기열 처리를 거부한다")
	@Test
	void validateStaffRejectsCitizen() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateStaff(citizen())
		);

		assertEquals(ErrorCode.QUEUE_FORBIDDEN, exception.errorCode());
	}

	@DisplayName("다른 보건소 대기표 접근을 거부한다")
	@Test
	void validateAccessRejectsOtherHealthCenterTicket() {
		QueueTicketVO ticket = ticket("WAITING");
		ticket.setHealthCenterId(2L);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateAccess(staff(1L), ticket)
		);

		assertEquals(ErrorCode.QUEUE_FORBIDDEN, exception.errorCode());
	}

	@DisplayName("WAITING 또는 HOLD가 아니면 호출할 수 없다")
	@Test
	void validateCallRejectsInvalidStatus() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCall(ticket("COMPLETED"))
		);

		assertEquals(ErrorCode.QUEUE_INVALID_STATUS, exception.errorCode());
	}

	@DisplayName("CALLED가 아니면 처리 시작할 수 없다")
	@Test
	void validateStartRejectsInvalidStatus() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateStart(ticket("WAITING"))
		);

		assertEquals(ErrorCode.QUEUE_INVALID_STATUS, exception.errorCode());
	}

	@DisplayName("IN_PROGRESS가 아니면 완료할 수 없다")
	@Test
	void validateCompleteRejectsInvalidStatus() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateComplete(ticket("CALLED"))
		);

		assertEquals(ErrorCode.QUEUE_INVALID_STATUS, exception.errorCode());
	}

	@DisplayName("정상 상태의 대기표 상태 전이는 허용한다")
	@Test
	void validateQueueTransitionsAllowExpectedStatuses() {
		assertDoesNotThrow(() -> policy.validateCall(ticket("WAITING")));
		assertDoesNotThrow(() -> policy.validateCall(ticket("HOLD")));
		assertDoesNotThrow(() -> policy.validateStart(ticket("CALLED")));
		assertDoesNotThrow(() -> policy.validateComplete(ticket("IN_PROGRESS")));
		assertDoesNotThrow(() -> policy.validateHold(ticket("CALLED")));
		assertDoesNotThrow(() -> policy.validateNoShow(ticket("HOLD")));
		assertDoesNotThrow(() -> policy.validateCancel(ticket("WAITING")));
	}

	private QueueTicketVO ticket(String status) {
		QueueTicketVO ticket = new QueueTicketVO();
		ticket.setHealthCenterId(1L);
		ticket.setStatus(status);
		return ticket;
	}

	private MemberPrincipal staff(Long healthCenterId) {
		return new MemberPrincipal(10L, healthCenterId, "staff@test.com", "직원", MemberRole.STAFF);
	}

	private MemberPrincipal citizen() {
		return new MemberPrincipal(20L, 1L, "citizen@test.com", "시민", MemberRole.CITIZEN);
	}
}
