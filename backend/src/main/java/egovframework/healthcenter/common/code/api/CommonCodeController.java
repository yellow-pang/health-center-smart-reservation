package egovframework.healthcenter.common.code.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.code.application.CommonCodeQueryService;
import egovframework.healthcenter.common.code.dto.CommonCodeResponse;
import egovframework.healthcenter.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/common-codes")
@Tag(name = "CommonCodeController", description = "보건소 공통코드 조회")
public class CommonCodeController {

	private final CommonCodeQueryService commonCodeQueryService;

	public CommonCodeController(CommonCodeQueryService commonCodeQueryService) {
		this.commonCodeQueryService = commonCodeQueryService;
	}

	@GetMapping("/{groupCode}")
	@Operation(summary = "그룹별 공통코드 조회", description = "특정 그룹 코드에 속한 사용 중 공통코드를 조회한다.")
	public ApiResponse<List<CommonCodeResponse>> findByGroupCode(@PathVariable String groupCode) {
		return ApiResponse.success(commonCodeQueryService.findActiveCodesByGroupCode(groupCode));
	}

	@GetMapping
	@Operation(summary = "공통코드 일괄 조회", description = "여러 그룹의 사용 중 공통코드를 한 번에 조회한다.")
	public ApiResponse<Map<String, List<CommonCodeResponse>>> findByGroupCodes(
			@RequestParam(required = false) String groupCodes) {
		return ApiResponse.success(commonCodeQueryService.findActiveCodesByGroupCodes(parseGroupCodes(groupCodes)));
	}

	private List<String> parseGroupCodes(String groupCodes) {
		if (groupCodes == null || groupCodes.isBlank()) {
			return List.of();
		}
		return Arrays.stream(groupCodes.split(","))
			.map(String::trim)
			.filter(groupCode -> !groupCode.isBlank())
			.toList();
	}
}
