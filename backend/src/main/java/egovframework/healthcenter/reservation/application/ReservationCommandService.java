package egovframework.healthcenter.reservation.application;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.dto.ReservationCreateRequest;
import egovframework.healthcenter.reservation.dto.ReservationCreateResponse;
import egovframework.healthcenter.reservation.mapper.ReservationMapper;
import egovframework.healthcenter.reservation.mapper.ReservationSlotMapper;
import egovframework.healthcenter.reservation.mapper.ReservationSlotVO;
import egovframework.healthcenter.reservation.mapper.ReservationVO;

@Service
public class ReservationCommandService {

	private static final DateTimeFormatter RESERVATION_NO_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

	private final ReservationMapper reservationMapper;
	private final ReservationSlotMapper reservationSlotMapper;

	public ReservationCommandService(
			ReservationMapper reservationMapper,
			ReservationSlotMapper reservationSlotMapper) {
		this.reservationMapper = reservationMapper;
		this.reservationSlotMapper = reservationSlotMapper;
	}

	@Transactional
	public ReservationCreateResponse createReservation(MemberPrincipal principal, ReservationCreateRequest request) {
		validatePrincipal(principal);
		validateRequest(request);

		ReservationSlotVO slot = reservationSlotMapper.selectSlotById(request.reservationSlotId());
		validateReservableSlot(slot, request);
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
		return ReservationCreateResponse.from(reservation);
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

	private void validateReservableSlot(ReservationSlotVO slot, ReservationCreateRequest request) {
		if (slot == null || !slot.isActive()) {
			throw new IllegalArgumentException("예약 슬롯을 찾을 수 없습니다.");
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
}
