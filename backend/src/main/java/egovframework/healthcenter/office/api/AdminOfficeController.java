package egovframework.healthcenter.office.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.office.application.OfficeCommandService;
import egovframework.healthcenter.office.application.OfficeQueryService;
import egovframework.healthcenter.office.dto.ServiceWindowResponse;
import egovframework.healthcenter.office.dto.ServiceWindowUpsertRequest;
import egovframework.healthcenter.office.dto.StaffCreateRequest;
import egovframework.healthcenter.office.dto.StaffResponse;
import egovframework.healthcenter.office.dto.StaffUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "AdminOfficeController", description = "관리자 보건소 기준정보")
public class AdminOfficeController {

	private final OfficeQueryService officeQueryService;
	private final OfficeCommandService officeCommandService;

	public AdminOfficeController(OfficeQueryService officeQueryService, OfficeCommandService officeCommandService) {
		this.officeQueryService = officeQueryService;
		this.officeCommandService = officeCommandService;
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

	@PostMapping("/staff")
	@Operation(
		summary = "직원 생성",
		description = "관리자가 직원 계정을 생성한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<StaffResponse>> createStaff(@RequestBody StaffCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success(officeCommandService.createStaff(request)));
	}

	@PutMapping("/staff/{id}")
	@Operation(
		summary = "직원 수정",
		description = "관리자가 직원 이름, 전화번호, 사용 여부를 수정한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
			@PathVariable Long id,
			@RequestBody StaffUpdateRequest request) {
		return ResponseEntity.ok(ApiResponse.success(officeCommandService.updateStaff(id, request)));
	}

	@PatchMapping("/staff/{id}/deactivate")
	@Operation(
		summary = "직원 비활성화",
		description = "관리자가 직원 계정을 삭제하지 않고 비활성화한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<StaffResponse> deactivateStaff(@PathVariable Long id) {
		return ApiResponse.success(officeCommandService.deactivateStaff(id));
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

	@PostMapping("/service-windows")
	@Operation(
		summary = "창구 생성",
		description = "관리자가 창구와 담당 업무, 담당 직원을 생성한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ServiceWindowResponse>> createServiceWindow(
			@RequestBody ServiceWindowUpsertRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success(officeCommandService.createServiceWindow(request)));
	}

	@PutMapping("/service-windows/{id}")
	@Operation(
		summary = "창구 수정과 담당자 배정",
		description = "관리자가 창구 정보, 담당 업무, 담당 직원을 수정한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ServiceWindowResponse>> updateServiceWindow(
			@PathVariable Long id,
			@RequestBody ServiceWindowUpsertRequest request) {
		return ResponseEntity.ok(ApiResponse.success(officeCommandService.updateServiceWindow(id, request)));
	}

	@PatchMapping("/service-windows/{id}/deactivate")
	@Operation(
		summary = "창구 비활성화",
		description = "관리자가 창구를 삭제하지 않고 비활성화한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<ServiceWindowResponse> deactivateServiceWindow(@PathVariable Long id) {
		return ApiResponse.success(officeCommandService.deactivateServiceWindow(id));
	}
}
