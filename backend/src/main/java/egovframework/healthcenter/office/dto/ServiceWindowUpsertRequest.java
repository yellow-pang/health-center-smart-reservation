package egovframework.healthcenter.office.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "창구 생성/수정 요청")
public record ServiceWindowUpsertRequest(
	@Schema(description = "창구 번호", example = "4")
	Integer windowNumber,
	@Schema(description = "창구명", example = "4번 창구")
	String name,
	@Schema(description = "창구 상태", example = "OPEN")
	String status,
	@Schema(description = "사용 여부", example = "true")
	Boolean active,
	@Schema(description = "담당 직원 ID", example = "2")
	Long staffId,
	@Schema(description = "담당 업무 유형 ID 목록", example = "[1, 2]")
	List<Long> serviceTypeIds
) {
}
