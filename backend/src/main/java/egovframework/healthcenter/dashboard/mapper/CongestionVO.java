package egovframework.healthcenter.dashboard.mapper;

public class CongestionVO {

	private Long serviceTypeId;
	private String serviceTypeName;
	private Integer waitingCount;
	private Integer averageProcessingMinutes;

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

	public Integer getWaitingCount() {
		return waitingCount;
	}

	public void setWaitingCount(Integer waitingCount) {
		this.waitingCount = waitingCount;
	}

	public Integer getAverageProcessingMinutes() {
		return averageProcessingMinutes;
	}

	public void setAverageProcessingMinutes(Integer averageProcessingMinutes) {
		this.averageProcessingMinutes = averageProcessingMinutes;
	}
}
