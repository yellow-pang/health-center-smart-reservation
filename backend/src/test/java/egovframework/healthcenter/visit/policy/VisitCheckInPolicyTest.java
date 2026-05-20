package egovframework.healthcenter.visit.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.mapper.ReservationVO;

class VisitCheckInPolicyTest {

	private final VisitCheckInPolicy policy = new VisitCheckInPolicy();

	@DisplayName("예약이 없으면 체크인을 거부한다")
	@Test
	void validateCheckInRejectsMissingReservation() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCheckIn(staff(1L), null)
		);

		assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.errorCode());
	}

	@DisplayName("다른 보건소 예약은 체크인할 수 없다")
	@Test
	void validateCheckInRejectsOtherHealthCenterReservation() {
		ReservationVO reservation = reservedReservation();
		reservation.setHealthCenterId(2L);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCheckIn(staff(1L), reservation)
		);

		assertEquals(ErrorCode.VISIT_FORBIDDEN, exception.errorCode());
	}

	@DisplayName("직원 또는 관리자가 아니면 체크인할 수 없다")
	@Test
	void validateCheckInRejectsCitizen() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCheckIn(citizen(), reservedReservation())
		);

		assertEquals(ErrorCode.VISIT_FORBIDDEN, exception.errorCode());
	}

	@DisplayName("예약 상태가 RESERVED가 아니면 중복 체크인으로 거부한다")
	@Test
	void validateCheckInRejectsAlreadyCheckedInReservation() {
		ReservationVO reservation = reservedReservation();
		reservation.setStatus("CHECKED_IN");

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCheckIn(staff(1L), reservation)
		);

		assertEquals(ErrorCode.VISIT_ALREADY_CHECKED_IN, exception.errorCode());
	}

	@DisplayName("같은 보건소 직원은 RESERVED 예약을 체크인할 수 있다")
	@Test
	void validateCheckInAllowsSameHealthCenterStaff() {
		assertDoesNotThrow(() -> policy.validateCheckIn(staff(1L), reservedReservation()));
	}

	private ReservationVO reservedReservation() {
		ReservationVO reservation = new ReservationVO();
		reservation.setHealthCenterId(1L);
		reservation.setStatus("RESERVED");
		return reservation;
	}

	private MemberPrincipal staff(Long healthCenterId) {
		return new MemberPrincipal(10L, healthCenterId, "staff@test.com", "직원", MemberRole.STAFF);
	}

	private MemberPrincipal citizen() {
		return new MemberPrincipal(20L, 1L, "citizen@test.com", "시민", MemberRole.CITIZEN);
	}
}
