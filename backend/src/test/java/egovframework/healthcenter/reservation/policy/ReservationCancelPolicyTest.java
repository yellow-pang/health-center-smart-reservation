package egovframework.healthcenter.reservation.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.mapper.ReservationVO;

class ReservationCancelPolicyTest {

	private final ReservationCancelPolicy policy = new ReservationCancelPolicy();

	@DisplayName("로그인 정보가 없으면 예약 취소를 거부한다")
	@Test
	void validateCancelableRequiresAuthentication() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCancelable(null, reservedReservation(), LocalDateTime.now())
		);

		assertEquals(ErrorCode.AUTH_REQUIRED, exception.errorCode());
	}

	@DisplayName("다른 사용자의 예약은 취소할 수 없다")
	@Test
	void validateCancelableRejectsOtherMemberReservation() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCancelable(citizen(99L), reservedReservation(), LocalDateTime.now())
		);

		assertEquals(ErrorCode.RESERVATION_FORBIDDEN, exception.errorCode());
	}

	@DisplayName("예약 상태가 RESERVED가 아니면 취소할 수 없다")
	@Test
	void validateCancelableRejectsInvalidStatus() {
		ReservationVO reservation = reservedReservation();
		reservation.setStatus("CHECKED_IN");

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCancelable(citizen(1L), reservation, LocalDateTime.now())
		);

		assertEquals(ErrorCode.RESERVATION_CANCEL_INVALID_STATUS, exception.errorCode());
	}

	@DisplayName("방문 1시간 전 이후에는 예약을 취소할 수 없다")
	@Test
	void validateCancelableRejectsExpiredCancelTime() {
		ReservationVO reservation = reservedReservation();
		LocalDateTime now = LocalDateTime.of(reservation.getSlotDate(), reservation.getStartTime()).minusMinutes(30);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> policy.validateCancelable(citizen(1L), reservation, now)
		);

		assertEquals(ErrorCode.RESERVATION_CANCEL_TIME_EXPIRED, exception.errorCode());
	}

	@DisplayName("같은 보건소 직원은 예약을 취소할 수 있다")
	@Test
	void validateCancelableAllowsSameHealthCenterStaff() {
		ReservationVO reservation = reservedReservation();
		LocalDateTime now = LocalDateTime.of(reservation.getSlotDate(), reservation.getStartTime()).minusHours(2);

		assertDoesNotThrow(() -> policy.validateCancelable(staff(2L, 1L), reservation, now));
	}

	private ReservationVO reservedReservation() {
		ReservationVO reservation = new ReservationVO();
		reservation.setId(1L);
		reservation.setHealthCenterId(1L);
		reservation.setMemberId(1L);
		reservation.setSlotDate(LocalDate.now().plusDays(1));
		reservation.setStartTime(LocalTime.of(10, 0));
		reservation.setStatus("RESERVED");
		return reservation;
	}

	private MemberPrincipal citizen(Long memberId) {
		return new MemberPrincipal(memberId, 1L, "citizen@test.com", "시민", MemberRole.CITIZEN);
	}

	private MemberPrincipal staff(Long memberId, Long healthCenterId) {
		return new MemberPrincipal(memberId, healthCenterId, "staff@test.com", "직원", MemberRole.STAFF);
	}
}
