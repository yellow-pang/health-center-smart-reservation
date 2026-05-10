package egovframework.healthcenter.reservation.mapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import egovframework.healthcenter.reservation.dto.ReservationSlotCreateRequest;

@Repository("reservationSlotMapper")
public class ReservationSlotMapper extends EgovAbstractMapper {

	public List<ReservationSlotVO> selectActiveSlots(Long serviceTypeId, LocalDate date) {
		Map<String, Object> params = new HashMap<>();
		params.put("serviceTypeId", serviceTypeId);
		params.put("date", date);
		return selectList("ReservationSlotMapper.selectActiveSlots", params);
	}

	public ReservationSlotVO selectSlotById(Long slotId) {
		return selectOne("ReservationSlotMapper.selectSlotById", slotId);
	}

	public int increaseReservedCountIfAvailable(Long slotId) {
		return update("ReservationSlotMapper.increaseReservedCountIfAvailable", slotId);
	}

	public int decreaseReservedCount(Long slotId) {
		return update("ReservationSlotMapper.decreaseReservedCount", slotId);
	}

	public void insertSlot(Long healthCenterId, ReservationSlotCreateRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("serviceTypeId", request.serviceTypeId());
		params.put("date", request.date());
		params.put("startTime", request.startTime());
		params.put("endTime", request.endTime());
		params.put("capacity", request.capacity());
		insert("ReservationSlotMapper.insertSlot", params);
	}
}
