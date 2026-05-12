package egovframework.healthcenter.dashboard.mapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

@Repository("dashboardMapper")
public class DashboardMapper extends EgovAbstractMapper {

	public DashboardSummaryVO selectSummary(Long healthCenterId, LocalDate targetDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("targetDate", targetDate);
		return selectOne("DashboardMapper.selectSummary", params);
	}

	public List<CongestionVO> selectCurrentCongestion(Long healthCenterId) {
		return selectList("DashboardMapper.selectCurrentCongestion", healthCenterId);
	}
}
