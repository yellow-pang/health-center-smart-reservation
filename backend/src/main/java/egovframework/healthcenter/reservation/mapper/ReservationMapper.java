package egovframework.healthcenter.reservation.mapper;

import java.util.HashMap;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import egovframework.healthcenter.reservation.dto.ReservationCreateRequest;

@Repository("reservationMapper")
public class ReservationMapper extends EgovAbstractMapper {

	public int countActiveReservationByMemberAndSlot(Long memberId, Long reservationSlotId) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("reservationSlotId", reservationSlotId);
		return selectOne("ReservationMapper.countActiveReservationByMemberAndSlot", params);
	}

	public void insertReservation(
			String reservationNo,
			Long healthCenterId,
			Long memberId,
			ReservationCreateRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("reservationNo", reservationNo);
		params.put("healthCenterId", healthCenterId);
		params.put("memberId", memberId);
		params.put("serviceTypeId", request.serviceTypeId());
		params.put("reservationSlotId", request.reservationSlotId());
		params.put("visitorName", request.visitorName());
		params.put("visitorPhone", request.visitorPhone());
		insert("ReservationMapper.insertReservation", params);
	}

	public ReservationVO selectReservationByNo(String reservationNo) {
		return selectOne("ReservationMapper.selectReservationByNo", reservationNo);
	}
}
