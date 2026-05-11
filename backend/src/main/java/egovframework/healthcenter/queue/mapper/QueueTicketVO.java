package egovframework.healthcenter.queue.mapper;

public class QueueTicketVO {

	private Long id;
	private Integer ticketNumber;
	private String status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getTicketNumber() {
		return ticketNumber;
	}

	public void setTicketNumber(Integer ticketNumber) {
		this.ticketNumber = ticketNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
