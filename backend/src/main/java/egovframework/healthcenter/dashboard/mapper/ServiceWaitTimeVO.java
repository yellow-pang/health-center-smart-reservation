package egovframework.healthcenter.dashboard.mapper;

public class ServiceWaitTimeVO {

	private Long serviceTypeId;
	private String serviceTypeName;
	private Integer averageWaitMinutes;
	private Integer calledCount;

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

	public Integer getAverageWaitMinutes() {
		return averageWaitMinutes;
	}

	public void setAverageWaitMinutes(Integer averageWaitMinutes) {
		this.averageWaitMinutes = averageWaitMinutes;
	}

	public Integer getCalledCount() {
		return calledCount;
	}

	public void setCalledCount(Integer calledCount) {
		this.calledCount = calledCount;
	}
}
