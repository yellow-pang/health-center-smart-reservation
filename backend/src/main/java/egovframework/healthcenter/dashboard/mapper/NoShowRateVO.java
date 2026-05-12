package egovframework.healthcenter.dashboard.mapper;

import java.math.BigDecimal;

public class NoShowRateVO {

	private Integer targetReservationCount;
	private Integer noShowReservationCount;
	private BigDecimal noShowRate;

	public Integer getTargetReservationCount() {
		return targetReservationCount;
	}

	public void setTargetReservationCount(Integer targetReservationCount) {
		this.targetReservationCount = targetReservationCount;
	}

	public Integer getNoShowReservationCount() {
		return noShowReservationCount;
	}

	public void setNoShowReservationCount(Integer noShowReservationCount) {
		this.noShowReservationCount = noShowReservationCount;
	}

	public BigDecimal getNoShowRate() {
		return noShowRate;
	}

	public void setNoShowRate(BigDecimal noShowRate) {
		this.noShowRate = noShowRate;
	}
}
