package egovframework.healthcenter.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.logging.AuditLogSupport;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.dto.ReservationCreateRequest;
import egovframework.healthcenter.reservation.dto.ReservationCreateResponse;
import egovframework.healthcenter.reservation.mapper.ReservationMapper;
import egovframework.healthcenter.reservation.mapper.ReservationSlotMapper;
import egovframework.healthcenter.reservation.mapper.ReservationSlotVO;
import egovframework.healthcenter.reservation.mapper.ReservationVO;
import egovframework.healthcenter.reservation.policy.ReservationCancelPolicy;

@Service
public class ReservationCommandService {

	private static final Logger log = LoggerFactory.getLogger(ReservationCommandService.class);
	private static final DateTimeFormatter RESERVATION_NO_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

	private final ReservationMapper reservationMapper;
	private final ReservationSlotMapper reservationSlotMapper;
	private final ReservationCancelPolicy reservationCancelPolicy;

	public ReservationCommandService(
			ReservationMapper reservationMapper,
			ReservationSlotMapper reservationSlotMapper,
			ReservationCancelPolicy reservationCancelPolicy) {
		this.reservationMapper = reservationMapper;
		this.reservationSlotMapper = reservationSlotMapper;
		this.reservationCancelPolicy = reservationCancelPolicy;
	}

	@Transactional
	public ReservationCreateResponse createReservation(MemberPrincipal principal, ReservationCreateRequest request) {
		validatePrincipal(principal);
		validateRequest(request);

		ReservationSlotVO slot = reservationSlotMapper.selectSlotById(request.reservationSlotId());
		validateReservableSlot(principal, slot, request);
		validateDuplicatedReservation(principal.memberId(), request.reservationSlotId());

		int increased = reservationSlotMapper.increaseReservedCountIfAvailable(request.reservationSlotId());
		if (increased == 0) {
			throw new IllegalArgumentException("선택한 시간대의 예약이 마감되었습니다.");
		}

		String reservationNo = generateReservationNo(slot);
		try {
			reservationMapper.insertReservation(reservationNo, slot.getHealthCenterId(), principal.memberId(), request);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("동일 시간대에 이미 예약이 존재합니다.", e);
		}

		ReservationVO reservation = reservationMapper.selectReservationByNo(reservationNo);
		log.info(
			"event=reservation.created traceId={} memberId={} role={} healthCenterId={} reservationId={} reservationSlotId={} serviceTypeId={} status={}",
			AuditLogSupport.traceId(),
			AuditLogSupport.memberId(principal),
			AuditLogSupport.role(principal),
			AuditLogSupport.healthCenterId(principal),
			reservation.getId(),
			reservation.getReservationSlotId(),
			reservation.getServiceTypeId(),
			reservation.getStatus()
		);
		return ReservationCreateResponse.from(reservation);
	}

	@Transactional
	public void cancelReservation(MemberPrincipal principal, Long reservationId) {
		validatePrincipal(principal);
		if (reservationId == null || reservationId < 1) {
			throw new IllegalArgumentException("예약 ID가 올바르지 않습니다.");
		}

		ReservationVO reservation = reservationMapper.selectReservationById(reservationId);
		reservationCancelPolicy.validateCancelable(principal, reservation, LocalDateTime.now());

		int canceled = reservationMapper.cancelReservation(reservationId);
		if (canceled == 0) {
			throw new IllegalArgumentException("현재 상태에서는 예약을 취소할 수 없습니다.");
		}
		int decreased = reservationSlotMapper.decreaseReservedCount(reservation.getReservationSlotId());
		if (decreased == 0) {
			throw new IllegalArgumentException("예약 슬롯 예약 수를 복구할 수 없습니다.");
		}
		log.info(
			"event=reservation.canceled traceId={} memberId={} role={} healthCenterId={} reservationId={} reservationSlotId={} previousStatus={} status=CANCELED",
			AuditLogSupport.traceId(),
			AuditLogSupport.memberId(principal),
			AuditLogSupport.role(principal),
			AuditLogSupport.healthCenterId(principal),
			reservationId,
			reservation.getReservationSlotId(),
			reservation.getStatus()
		);
	}

	private void validatePrincipal(MemberPrincipal principal) {
		if (principal == null || principal.memberId() == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
	}

	private void validateRequest(ReservationCreateRequest request) {
		if (request == null || request.serviceTypeId() == null || request.serviceTypeId() < 1) {
			throw new IllegalArgumentException("업무 유형 ID가 올바르지 않습니다.");
		}
		if (request.reservationSlotId() == null || request.reservationSlotId() < 1) {
			throw new IllegalArgumentException("예약 슬롯 ID가 올바르지 않습니다.");
		}
		if (isBlank(request.visitorName()) || isBlank(request.visitorPhone())) {
			throw new IllegalArgumentException("방문자 이름과 연락처를 입력하세요.");
		}
	}

	private void validateReservableSlot(MemberPrincipal principal, ReservationSlotVO slot, ReservationCreateRequest request) {
		if (slot == null || !slot.isActive()) {
			throw new IllegalArgumentException("예약 슬롯을 찾을 수 없습니다.");
		}
		if (hasStaffOrAdminRole(principal)
				&& (principal.healthCenterId() == null || !principal.healthCenterId().equals(slot.getHealthCenterId()))) {
			throw new IllegalArgumentException("해당 보건소 예약 슬롯으로 예약할 권한이 없습니다.");
		}
		if (!request.serviceTypeId().equals(slot.getServiceTypeId())) {
			throw new IllegalArgumentException("업무 유형과 예약 슬롯이 일치하지 않습니다.");
		}
		LocalDate today = LocalDate.now();
		if (slot.getDate().isBefore(today) || slot.getDate().isAfter(today.plusDays(14))) {
			throw new IllegalArgumentException("예약 가능 날짜는 오늘부터 14일 이내입니다.");
		}
		if (slot.getReservedCount() >= slot.getCapacity()) {
			throw new IllegalArgumentException("선택한 시간대의 예약이 마감되었습니다.");
		}
	}

	private void validateDuplicatedReservation(Long memberId, Long reservationSlotId) {
		int count = reservationMapper.countActiveReservationByMemberAndSlot(memberId, reservationSlotId);
		if (count > 0) {
			throw new IllegalArgumentException("동일 시간대에 이미 예약이 존재합니다.");
		}
	}

	private String generateReservationNo(ReservationSlotVO slot) {
		String datePart = slot.getDate().format(RESERVATION_NO_DATE_FORMAT);
		String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		return "RSV-" + datePart + "-" + randomPart;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private boolean hasStaffOrAdminRole(MemberPrincipal principal) {
		return principal.role() == MemberRole.STAFF || principal.role() == MemberRole.ADMIN;
	}
}
