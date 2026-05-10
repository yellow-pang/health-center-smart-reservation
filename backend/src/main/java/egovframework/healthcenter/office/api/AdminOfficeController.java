package egovframework.healthcenter.office.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.office.application.OfficeQueryService;
import egovframework.healthcenter.office.dto.ServiceWindowResponse;
import egovframework.healthcenter.office.dto.StaffResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "AdminOfficeController", description = "관리자 보건소 기준정보")
public class AdminOfficeController {

	private final OfficeQueryService officeQueryService;

	public AdminOfficeController(OfficeQueryService officeQueryService) {
		this.officeQueryService = officeQueryService;
	}

	@GetMapping("/staff")
	@Operation(
		summary = "직원 목록 조회",
		description = "관리자가 활성 직원 목록을 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<List<StaffResponse>> findStaff() {
		return ApiResponse.success(officeQueryService.findActiveStaff());
	}

	@GetMapping("/service-windows")
	@Operation(
		summary = "창구 업무 매핑 조회",
		description = "관리자가 창구와 담당 업무 유형 매핑을 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<List<ServiceWindowResponse>> findServiceWindows() {
		return ApiResponse.success(officeQueryService.findServiceWindows());
	}
}
