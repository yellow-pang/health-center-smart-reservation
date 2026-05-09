package egovframework.healthcenter.member.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.member.dto.MemberResponse;
import egovframework.healthcenter.member.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/members")
@Tag(name = "MemberController", description = "보건소 회원")
public class MemberController {

	@GetMapping("/me")
	@Operation(
		summary = "내 회원 정보 조회",
		description = "현재 로그인한 회원 정보를 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<MemberResponse>> me(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.failure("AUTH_REQUIRED", "로그인이 필요합니다."));
		}
		return ResponseEntity.ok(ApiResponse.success(MemberResponse.from(principal)));
	}
}
