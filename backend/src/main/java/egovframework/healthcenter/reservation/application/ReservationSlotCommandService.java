package egovframework.healthcenter.reservation.application;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.reservation.dto.ReservationSlotCreateRequest;
import egovframework.healthcenter.reservation.dto.ReservationSlotResponse;
import egovframework.healthcenter.reservation.mapper.ReservationSlotMapper;
import egovframework.healthcenter.reservation.mapper.ReservationSlotVO;

@Service
public class ReservationSlotCommandService {

	private static final long DEFAULT_HEALTH_CENTER_ID = 1L;

	private final ReservationSlotMapper reservationSlotMapper;

	public ReservationSlotCommandService(ReservationSlotMapper reservationSlotMapper) {
		this.reservationSlotMapper = reservationSlotMapper;
	}

	@Transactional
	public ReservationSlotResponse createSlot(ReservationSlotCreateRequest request) {
		validateCreateRequest(request);
		try {
			reservationSlotMapper.insertSlot(DEFAULT_HEALTH_CENTER_ID, request);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("이미 등록된 예약 슬롯입니다.", e);
		}

		ReservationSlotVO createdSlot = reservationSlotMapper.selectActiveSlots(request.serviceTypeId(), request.date())
			.stream()
			.filter(slot -> request.startTime().equals(slot.getStartTime()))
			.filter(slot -> request.endTime().equals(slot.getEndTime()))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("생성된 예약 슬롯을 조회할 수 없습니다."));
		return ReservationSlotResponse.from(createdSlot);
	}

	private void validateCreateRequest(ReservationSlotCreateRequest request) {
		if (request == null || request.serviceTypeId() == null || request.serviceTypeId() < 1) {
			throw new IllegalArgumentException("업무 유형 ID가 올바르지 않습니다.");
		}
		if (request.date() == null) {
			throw new IllegalArgumentException("예약 날짜를 입력하세요.");
		}
		if (request.startTime() == null || request.endTime() == null) {
			throw new IllegalArgumentException("예약 시작 시간과 종료 시간을 입력하세요.");
		}
		if (!request.endTime().isAfter(request.startTime())) {
			throw new IllegalArgumentException("예약 종료 시간은 시작 시간보다 늦어야 합니다.");
		}
		if (request.capacity() == null || request.capacity() < 1) {
			throw new IllegalArgumentException("예약 가능 인원은 1명 이상이어야 합니다.");
		}
	}
}
