package egovframework.healthcenter.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FindIdRequest(
	@Schema(description = "가입자 이름", example = "홍길동")
	String name,
	@Schema(description = "휴대폰 번호", example = "010-0000-0001")
	String phone
) {
}
