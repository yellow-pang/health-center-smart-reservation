package egovframework.healthcenter.reservation.mapper;

import java.time.LocalDateTime;

public class ReservationVO {

	private Long id;
	private String reservationNo;
	private Long healthCenterId;
	private Long memberId;
	private Long serviceTypeId;
	private Long reservationSlotId;
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

	public Long getReservationSlotId() {
		return reservationSlotId;
	}

	public void setReservationSlotId(Long reservationSlotId) {
		this.reservationSlotId = reservationSlotId;
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
