package egovframework.healthcenter.reservation.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.reservation.dto.ReservationSlotResponse;
import egovframework.healthcenter.reservation.mapper.ReservationSlotMapper;

@Service
@Transactional(readOnly = true)
public class ReservationSlotQueryService {

	private final ReservationSlotMapper reservationSlotMapper;

	public ReservationSlotQueryService(ReservationSlotMapper reservationSlotMapper) {
		this.reservationSlotMapper = reservationSlotMapper;
	}

	public List<ReservationSlotResponse> findAvailableSlots(Long serviceTypeId, LocalDate date) {
		validateSearchCondition(serviceTypeId, date);
		return reservationSlotMapper.selectActiveSlots(serviceTypeId, date)
			.stream()
			.map(ReservationSlotResponse::from)
			.toList();
	}

	private void validateSearchCondition(Long serviceTypeId, LocalDate date) {
		if (serviceTypeId == null || serviceTypeId < 1) {
			throw new BusinessException(ErrorCode.SERVICE_TYPE_INVALID_REQUEST);
		}
		if (date == null) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 날짜를 입력하세요.");
		}
		LocalDate today = LocalDate.now();
		if (date.isBefore(today) || date.isAfter(today.plusDays(14))) {
			throw new BusinessException(ErrorCode.RESERVATION_SLOT_INVALID_REQUEST, "예약 가능 날짜는 오늘부터 14일 이내입니다.");
		}
	}
}
