package egovframework.healthcenter.reservation.mapper;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationSlotVO {

	private Long id;
	private Long healthCenterId;
	private Long serviceTypeId;
	private String serviceTypeName;
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private int capacity;
	private int reservedCount;
	private boolean active;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getHealthCenterId() {
		return healthCenterId;
	}

	public void setHealthCenterId(Long healthCenterId) {
		this.healthCenterId = healthCenterId;
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

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
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

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getReservedCount() {
		return reservedCount;
	}

	public void setReservedCount(int reservedCount) {
		this.reservedCount = reservedCount;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
