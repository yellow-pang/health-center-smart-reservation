package egovframework.healthcenter.common.code.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

@Repository("commonCodeMapper")
public class CommonCodeMapper extends EgovAbstractMapper {

	public List<CommonCodeVO> selectActiveCodesByGroupCode(String groupCode) {
		return selectList("CommonCodeMapper.selectActiveCodesByGroupCode", groupCode);
	}

	public List<CommonCodeVO> selectActiveCodesByGroupCodes(List<String> groupCodes) {
		if (groupCodes == null || groupCodes.isEmpty()) {
			return selectList("CommonCodeMapper.selectActiveCodes");
		}
		return selectList("CommonCodeMapper.selectActiveCodesByGroupCodes", groupCodes);
	}
}
