package egovframework.healthcenter.office.mapper;

public class StaffVO {

	private Long id;
	private Long healthCenterId;
	private String email;
	private String name;
	private String phone;
	private String role;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
