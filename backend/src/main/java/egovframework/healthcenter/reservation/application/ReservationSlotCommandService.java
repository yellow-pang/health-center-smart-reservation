package egovframework.healthcenter.reservation.application;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.reservation.dto.ReservationSlotCreateRequest;
import egovframework.healthcenter.reservation.dto.ReservationSlotResponse;
import egovframework.healthcenter.reservation.dto.ReservationSlotUpdateRequest;
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
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_DUPLICATED, ErrorCode.RESERVATION_SLOT_DUPLICATED.message(), e);
		}

		ReservationSlotVO createdSlot = reservationSlotMapper.selectActiveSlots(request.serviceTypeId(), request.date())
			.stream()
			.filter(slot -> request.startTime().equals(slot.getStartTime()))
			.filter(slot -> request.endTime().equals(slot.getEndTime()))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("생성된 예약 슬롯을 조회할 수 없습니다."));
		return ReservationSlotResponse.from(createdSlot);
	}

	@Transactional
	public ReservationSlotResponse updateSlot(Long slotId, ReservationSlotUpdateRequest request) {
		validateSlotId(slotId);
		validateUpdateRequest(request);
		try {
			int updated = reservationSlotMapper.updateSlot(slotId, request);
			if (updated == 0) {
				throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 슬롯을 찾을 수 없거나 정원이 현재 예약 수보다 작습니다.");
			}
		} catch (DuplicateKeyException e) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_DUPLICATED, "이미 등록된 예약 슬롯 시간입니다.", e);
		}

		return ReservationSlotResponse.from(findSlot(slotId));
	}

	@Transactional
	public ReservationSlotResponse deactivateSlot(Long slotId) {
		validateSlotId(slotId);

		int updated = reservationSlotMapper.deactivateSlot(slotId);
		if (updated == 0) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_NOT_FOUND);
		}

		return ReservationSlotResponse.from(findSlot(slotId));
	}

	private void validateCreateRequest(ReservationSlotCreateRequest request) {
		if (request == null || request.serviceTypeId() == null || request.serviceTypeId() < 1) {
			throw new BusinessException(ErrorCode.SERVICE_TYPE_INVALID_REQUEST);
		}
		if (request.date() == null) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 날짜를 입력하세요.");
		}
		if (request.startTime() == null || request.endTime() == null) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 시작 시간과 종료 시간을 입력하세요.");
		}
		if (!request.endTime().isAfter(request.startTime())) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 종료 시간은 시작 시간보다 늦어야 합니다.");
		}
		if (request.capacity() == null || request.capacity() < 1) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 가능 인원은 1명 이상이어야 합니다.");
		}
	}

	private void validateUpdateRequest(ReservationSlotUpdateRequest request) {
		if (request == null || request.serviceTypeId() == null || request.serviceTypeId() < 1) {
			throw new BusinessException(ErrorCode.SERVICE_TYPE_INVALID_REQUEST);
		}
		if (request.date() == null) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 날짜를 입력하세요.");
		}
		if (request.startTime() == null || request.endTime() == null) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 시작 시간과 종료 시간을 입력하세요.");
		}
		if (!request.endTime().isAfter(request.startTime())) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 종료 시간은 시작 시간보다 늦어야 합니다.");
		}
		if (request.capacity() == null || request.capacity() < 1) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 가능 인원은 1명 이상이어야 합니다.");
		}
		if (request.active() == null) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "사용 여부를 입력하세요.");
		}
	}

	private void validateSlotId(Long slotId) {
		if (slotId == null || slotId < 1) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST);
		}
	}

	private ReservationSlotVO findSlot(Long slotId) {
		ReservationSlotVO slot = reservationSlotMapper.selectSlotById(slotId);
		if (slot == null) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_NOT_FOUND);
		}
		return slot;
	}
}
