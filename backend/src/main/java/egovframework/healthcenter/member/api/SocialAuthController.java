package egovframework.healthcenter.member.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.member.application.SocialLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth/social")
@Tag(name = "SocialAuthController", description = "소셜 로그인")
public class SocialAuthController {

	private final SocialLoginService socialLoginService;

	public SocialAuthController(SocialLoginService socialLoginService) {
		this.socialLoginService = socialLoginService;
	}

	@GetMapping("/{provider}/authorize")
	@Operation(summary = "소셜 로그인 시작", description = "소셜 로그인 제공자의 인증 페이지로 이동한다.")
	public ResponseEntity<Void> authorize(@PathVariable String provider) {
		return ResponseEntity.status(302)
			.location(socialLoginService.buildAuthorizationUri(provider))
			.build();
	}

	@GetMapping("/{provider}/callback")
	@Operation(summary = "소셜 로그인 콜백", description = "소셜 로그인 인증 코드를 처리하고 프론트 콜백 화면으로 이동한다.")
	public ResponseEntity<Void> callback(
		@PathVariable String provider,
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String error
	) {
		URI redirectUri;
		try {
			redirectUri = socialLoginService.loginWithAuthorizationCode(provider, code, error);
		} catch (RuntimeException e) {
			redirectUri = socialLoginService.buildFrontendErrorRedirect(e.getMessage());
		}
		return ResponseEntity.status(302)
			.location(redirectUri)
			.build();
	}
}
