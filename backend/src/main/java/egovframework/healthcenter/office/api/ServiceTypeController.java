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
import egovframework.healthcenter.office.dto.ServiceTypeCreateRequest;
import egovframework.healthcenter.office.dto.ServiceTypeResponse;
import egovframework.healthcenter.office.dto.ServiceTypeUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "ServiceTypeController", description = "보건소 업무 유형")
public class ServiceTypeController {

	private final OfficeQueryService officeQueryService;
	private final OfficeCommandService officeCommandService;

	public ServiceTypeController(OfficeQueryService officeQueryService, OfficeCommandService officeCommandService) {
		this.officeQueryService = officeQueryService;
		this.officeCommandService = officeCommandService;
	}

	@GetMapping("/api/service-types")
	@Operation(summary = "업무 유형 조회", description = "예약과 현장 접수에서 선택할 수 있는 활성 업무 유형을 조회한다.")
	public ApiResponse<List<ServiceTypeResponse>> findServiceTypes() {
		return ApiResponse.success(officeQueryService.findActiveServiceTypes());
	}

	@GetMapping("/api/admin/service-types")
	@Operation(
		summary = "관리자 업무 유형 전체 조회",
		description = "관리자가 활성/비활성 업무 유형을 모두 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<List<ServiceTypeResponse>> findAllServiceTypes() {
		return ApiResponse.success(officeQueryService.findAllServiceTypes());
	}

	@PostMapping("/api/admin/service-types")
	@Operation(
		summary = "업무 유형 생성",
		description = "관리자가 보건소 업무 유형을 생성한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ServiceTypeResponse>> createServiceType(
			@RequestBody ServiceTypeCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success(officeCommandService.createServiceType(request)));
	}

	@PutMapping("/api/admin/service-types/{id}")
	@Operation(
		summary = "업무 유형 수정",
		description = "관리자가 업무명, 설명, 기본 예약 가능 인원, 사용 여부를 수정한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ServiceTypeResponse>> updateServiceType(
			@PathVariable Long id,
			@RequestBody ServiceTypeUpdateRequest request) {
		return ResponseEntity.ok(ApiResponse.success(officeCommandService.updateServiceType(id, request)));
	}

	@PatchMapping("/api/admin/service-types/{id}/deactivate")
	@Operation(
		summary = "업무 유형 비활성화",
		description = "관리자가 업무 유형을 삭제하지 않고 비활성화한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<ServiceTypeResponse> deactivateServiceType(@PathVariable Long id) {
		return ApiResponse.success(officeCommandService.deactivateServiceType(id));
	}

	@PatchMapping("/api/admin/service-types/{id}/activate")
	@Operation(
		summary = "업무 유형 재활성화",
		description = "관리자가 비활성화된 업무 유형을 다시 활성화한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<ServiceTypeResponse> activateServiceType(@PathVariable Long id) {
		return ApiResponse.success(officeCommandService.activateServiceType(id));
	}
}
