package egovframework.healthcenter.office.mapper;

public class ServiceWindowMappingVO {

	private Long windowId;
	private Long healthCenterId;
	private Integer windowNumber;
	private String windowName;
	private String status;
	private boolean windowActive;
	private Long staffId;
	private String staffName;
	private Long serviceTypeId;
	private String serviceTypeCode;
	private String serviceTypeName;
	private String serviceTypeDescription;
	private int defaultCapacity;
	private boolean serviceTypeActive;

	public Long getWindowId() {
		return windowId;
	}

	public void setWindowId(Long windowId) {
		this.windowId = windowId;
	}

	public Long getHealthCenterId() {
		return healthCenterId;
	}

	public void setHealthCenterId(Long healthCenterId) {
		this.healthCenterId = healthCenterId;
	}

	public Integer getWindowNumber() {
		return windowNumber;
	}

	public void setWindowNumber(Integer windowNumber) {
		this.windowNumber = windowNumber;
	}

	public String getWindowName() {
		return windowName;
	}

	public void setWindowName(String windowName) {
		this.windowName = windowName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isWindowActive() {
		return windowActive;
	}

	public void setWindowActive(boolean windowActive) {
		this.windowActive = windowActive;
	}

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	public String getStaffName() {
		return staffName;
	}

	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}

	public Long getServiceTypeId() {
		return serviceTypeId;
	}

	public void setServiceTypeId(Long serviceTypeId) {
		this.serviceTypeId = serviceTypeId;
	}

	public String getServiceTypeCode() {
		return serviceTypeCode;
	}

	public void setServiceTypeCode(String serviceTypeCode) {
		this.serviceTypeCode = serviceTypeCode;
	}

	public String getServiceTypeName() {
		return serviceTypeName;
	}

	public void setServiceTypeName(String serviceTypeName) {
		this.serviceTypeName = serviceTypeName;
	}

	public String getServiceTypeDescription() {
		return serviceTypeDescription;
	}

	public void setServiceTypeDescription(String serviceTypeDescription) {
		this.serviceTypeDescription = serviceTypeDescription;
	}

	public int getDefaultCapacity() {
		return defaultCapacity;
	}

	public void setDefaultCapacity(int defaultCapacity) {
		this.defaultCapacity = defaultCapacity;
	}

	public boolean isServiceTypeActive() {
		return serviceTypeActive;
	}

	public void setServiceTypeActive(boolean serviceTypeActive) {
		this.serviceTypeActive = serviceTypeActive;
	}
}
