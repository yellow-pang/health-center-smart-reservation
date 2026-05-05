package egovframework.healthcenter.common.code.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.code.dto.CommonCodeResponse;
import egovframework.healthcenter.common.code.mapper.CommonCodeMapper;
import egovframework.healthcenter.common.code.mapper.CommonCodeVO;

@Service
@Transactional(readOnly = true)
public class CommonCodeQueryService {

	private final CommonCodeMapper commonCodeMapper;

	public CommonCodeQueryService(CommonCodeMapper commonCodeMapper) {
		this.commonCodeMapper = commonCodeMapper;
	}

	public List<CommonCodeResponse> findActiveCodesByGroupCode(String groupCode) {
		return commonCodeMapper.selectActiveCodesByGroupCode(groupCode)
			.stream()
			.map(CommonCodeResponse::from)
			.toList();
	}

	public Map<String, List<CommonCodeResponse>> findActiveCodesByGroupCodes(List<String> groupCodes) {
		return commonCodeMapper.selectActiveCodesByGroupCodes(groupCodes)
			.stream()
			.collect(Collectors.groupingBy(
				CommonCodeVO::getGroupCode,
				LinkedHashMap::new,
				Collectors.mapping(CommonCodeResponse::from, Collectors.toList())
			));
	}
}
