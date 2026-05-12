package egovframework.healthcenter.dashboard.mapper;

import java.math.BigDecimal;

public class DashboardSummaryVO {

	private Integer todayVisitCount;
	private Integer currentWaitingCount;
	private Integer averageWaitMinutes;
	private BigDecimal noShowRate;

	public Integer getTodayVisitCount() {
		return todayVisitCount;
	}

	public void setTodayVisitCount(Integer todayVisitCount) {
		this.todayVisitCount = todayVisitCount;
	}

	public Integer getCurrentWaitingCount() {
		return currentWaitingCount;
	}

	public void setCurrentWaitingCount(Integer currentWaitingCount) {
		this.currentWaitingCount = currentWaitingCount;
	}

	public Integer getAverageWaitMinutes() {
		return averageWaitMinutes;
	}

	public void setAverageWaitMinutes(Integer averageWaitMinutes) {
		this.averageWaitMinutes = averageWaitMinutes;
	}

	public BigDecimal getNoShowRate() {
		return noShowRate;
	}

	public void setNoShowRate(BigDecimal noShowRate) {
		this.noShowRate = noShowRate;
	}
}
