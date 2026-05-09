package egovframework.healthcenter.member.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.member.application.AuthCommandService;
import egovframework.healthcenter.member.dto.LoginRequest;
import egovframework.healthcenter.member.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
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
	public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
		try {
			return ResponseEntity.ok(ApiResponse.success(authCommandService.login(request)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.failure("AUTH_INVALID_CREDENTIALS", e.getMessage()));
		}
	}
}
