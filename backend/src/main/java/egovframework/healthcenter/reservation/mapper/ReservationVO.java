package egovframework.healthcenter.reservation.mapper;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationVO {

	private Long id;
	private String reservationNo;
	private Long healthCenterId;
	private Long memberId;
	private Long serviceTypeId;
	private String serviceTypeName;
	private Long reservationSlotId;
	private LocalDate slotDate;
	private LocalTime startTime;
	private LocalTime endTime;
	private String visitorName;
	private String visitorPhone;
	private String status;
	private LocalDateTime reservedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReservationNo() {
		return reservationNo;
	}

	public void setReservationNo(String reservationNo) {
		this.reservationNo = reservationNo;
	}

	public Long getHealthCenterId() {
		return healthCenterId;
	}

	public void setHealthCenterId(Long healthCenterId) {
		this.healthCenterId = healthCenterId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public Long getServiceTypeId() {
		return serviceTypeId;
	}

	public void setServiceTypeId(Long serviceTypeId) {
		this.serviceTypeId = serviceTypeId;
	}

	public String getServiceTypeName() {
		return serviceTypeName;
	}

	public void setServiceTypeName(String serviceTypeName) {
		this.serviceTypeName = serviceTypeName;
	}

	public Long getReservationSlotId() {
		return reservationSlotId;
	}

	public void setReservationSlotId(Long reservationSlotId) {
		this.reservationSlotId = reservationSlotId;
	}

	public LocalDate getSlotDate() {
		return slotDate;
	}

	public void setSlotDate(LocalDate slotDate) {
		this.slotDate = slotDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public String getVisitorName() {
		return visitorName;
	}

	public void setVisitorName(String visitorName) {
		this.visitorName = visitorName;
	}

	public String getVisitorPhone() {
		return visitorPhone;
	}

	public void setVisitorPhone(String visitorPhone) {
		this.visitorPhone = visitorPhone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getReservedAt() {
		return reservedAt;
	}

	public void setReservedAt(LocalDateTime reservedAt) {
		this.reservedAt = reservedAt;
	}
}
