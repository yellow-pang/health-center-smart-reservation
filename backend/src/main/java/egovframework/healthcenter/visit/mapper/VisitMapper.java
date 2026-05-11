package egovframework.healthcenter.visit.mapper;

import java.util.HashMap;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import egovframework.healthcenter.reservation.mapper.ReservationVO;

@Repository("visitMapper")
public class VisitMapper extends EgovAbstractMapper {

	public Long insertReservedVisit(ReservationVO reservation, Long registeredBy) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", reservation.getHealthCenterId());
		params.put("reservationId", reservation.getId());
		params.put("serviceTypeId", reservation.getServiceTypeId());
		params.put("memberId", reservation.getMemberId());
		params.put("registeredBy", registeredBy);
		params.put("visitorName", reservation.getVisitorName());
		params.put("visitorPhone", reservation.getVisitorPhone());
		insert("VisitMapper.insertReservedVisit", params);
		return (Long) params.get("id");
	}

	public Long insertWalkInVisit(
			Long healthCenterId,
			Long serviceTypeId,
			Long registeredBy,
			String visitorName,
			String visitorPhone) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("serviceTypeId", serviceTypeId);
		params.put("registeredBy", registeredBy);
		params.put("visitorName", visitorName);
		params.put("visitorPhone", visitorPhone);
		insert("VisitMapper.insertWalkInVisit", params);
		return (Long) params.get("id");
	}
}
