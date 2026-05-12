package egovframework.healthcenter.dashboard.mapper;

import java.math.BigDecimal;

public class VisitTypeRatioVO {

	private Integer totalVisitCount;
	private Integer reservedVisitCount;
	private Integer walkInVisitCount;
	private BigDecimal reservedVisitRatio;
	private BigDecimal walkInVisitRatio;

	public Integer getTotalVisitCount() {
		return totalVisitCount;
	}

	public void setTotalVisitCount(Integer totalVisitCount) {
		this.totalVisitCount = totalVisitCount;
	}

	public Integer getReservedVisitCount() {
		return reservedVisitCount;
	}

	public void setReservedVisitCount(Integer reservedVisitCount) {
		this.reservedVisitCount = reservedVisitCount;
	}

	public Integer getWalkInVisitCount() {
		return walkInVisitCount;
	}

	public void setWalkInVisitCount(Integer walkInVisitCount) {
		this.walkInVisitCount = walkInVisitCount;
	}

	public BigDecimal getReservedVisitRatio() {
		return reservedVisitRatio;
	}

	public void setReservedVisitRatio(BigDecimal reservedVisitRatio) {
		this.reservedVisitRatio = reservedVisitRatio;
	}

	public BigDecimal getWalkInVisitRatio() {
		return walkInVisitRatio;
	}

	public void setWalkInVisitRatio(BigDecimal walkInVisitRatio) {
		this.walkInVisitRatio = walkInVisitRatio;
	}
}
