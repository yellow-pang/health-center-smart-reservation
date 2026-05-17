package egovframework.healthcenter.member.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.common.security.AuthenticatedPrincipal;
import egovframework.healthcenter.member.application.AuthCommandService;
import egovframework.healthcenter.member.dto.LoginRequest;
import egovframework.healthcenter.member.dto.LoginResponse;
import egovframework.healthcenter.member.dto.LogoutRequest;
import egovframework.healthcenter.member.dto.ReissueTokenRequest;
import egovframework.healthcenter.member.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "AuthController", description = "보건소 인증")
public class AuthController {

	private final AuthCommandService authCommandService;

	public AuthController(AuthCommandService authCommandService) {
		this.authCommandService = authCommandService;
	}

	@PostMapping("/login")
	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 Access Token과 Refresh Token을 발급한다.")
	public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
		return ApiResponse.success(authCommandService.login(request));
	}

	@PostMapping("/reissue")
	@Operation(summary = "토큰 재발급", description = "Refresh Token을 검증하고 Access Token과 Refresh Token을 재발급한다.")
	public ApiResponse<LoginResponse> reissue(@RequestBody ReissueTokenRequest request) {
		return ApiResponse.success(authCommandService.reissue(request));
	}

	@PostMapping("/logout")
	@Operation(
		summary = "로그아웃",
		description = "현재 로그인한 회원의 Refresh Token을 폐기한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<Void>> logout(
		Authentication authentication,
		@RequestBody LogoutRequest request
	) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		authCommandService.logout(principal, request);
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
