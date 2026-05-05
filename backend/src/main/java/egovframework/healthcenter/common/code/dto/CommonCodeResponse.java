package egovframework.healthcenter.common.code.dto;

import egovframework.healthcenter.common.code.mapper.CommonCodeVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통코드 응답")
public record CommonCodeResponse(
	@Schema(description = "그룹 코드", example = "RESERVATION_STATUS")
	String groupCode,
	@Schema(description = "코드", example = "RESERVED")
	String code,
	@Schema(description = "코드명", example = "예약 완료")
	String codeName,
	@Schema(description = "설명", example = "사용자가 예약을 완료한 상태")
	String description,
	@Schema(description = "정렬 순서", example = "1")
	int sortOrder
) {

	public static CommonCodeResponse from(CommonCodeVO vo) {
		return new CommonCodeResponse(
			vo.getGroupCode(),
			vo.getCode(),
			vo.getCodeName(),
			vo.getDescription(),
			vo.getSortOrder()
		);
	}
}
