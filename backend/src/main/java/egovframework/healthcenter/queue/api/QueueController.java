package egovframework.healthcenter.queue.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.common.security.AuthenticatedPrincipal;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.queue.application.QueueCommandService;
import egovframework.healthcenter.queue.application.QueueQueryService;
import egovframework.healthcenter.queue.dto.QueueTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/queues")
@Tag(name = "QueueController", description = "대기열")
public class QueueController {

	private final QueueQueryService queueQueryService;
	private final QueueCommandService queueCommandService;

	public QueueController(QueueQueryService queueQueryService, QueueCommandService queueCommandService) {
		this.queueQueryService = queueQueryService;
		this.queueCommandService = queueCommandService;
	}

	@GetMapping
	@Operation(
		summary = "대기열 조회",
		description = "오늘 대기표 목록을 업무 유형과 상태 기준으로 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<List<QueueTicketResponse>> findQueueTickets(
			Authentication authentication,
			@RequestParam(required = false) Long serviceTypeId,
			@RequestParam(required = false) String status) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ApiResponse.success(queueQueryService.findQueueTickets(principal, serviceTypeId, status));
	}

	@PostMapping("/{queueTicketId}/call")
	@Operation(
		summary = "대기자 호출",
		description = "WAITING 또는 HOLD 상태 대기표를 CALLED 상태로 변경한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<QueueTicketResponse>> call(
			Authentication authentication,
			@PathVariable Long queueTicketId) {
		return handleCommand(authentication, queueTicketId, QueueAction.CALL);
	}

	@PostMapping("/{queueTicketId}/start")
	@Operation(
		summary = "처리 시작",
		description = "CALLED 상태 대기표를 IN_PROGRESS 상태로 변경한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<QueueTicketResponse>> start(
			Authentication authentication,
			@PathVariable Long queueTicketId) {
		return handleCommand(authentication, queueTicketId, QueueAction.START);
	}

	@PostMapping("/{queueTicketId}/complete")
	@Operation(
		summary = "처리 완료",
		description = "IN_PROGRESS 상태 대기표를 COMPLETED 상태로 변경한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<QueueTicketResponse>> complete(
			Authentication authentication,
			@PathVariable Long queueTicketId) {
		return handleCommand(authentication, queueTicketId, QueueAction.COMPLETE);
	}

	@PostMapping("/{queueTicketId}/hold")
	@Operation(
		summary = "보류 처리",
		description = "CALLED 상태 대기표를 HOLD 상태로 변경한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<QueueTicketResponse>> hold(
			Authentication authentication,
			@PathVariable Long queueTicketId) {
		return handleCommand(authentication, queueTicketId, QueueAction.HOLD);
	}

	@PostMapping("/{queueTicketId}/no-show")
	@Operation(
		summary = "최종 미응답 처리",
		description = "HOLD 상태 대기표를 NO_SHOW 상태로 변경하고 방문 상태도 NO_SHOW로 변경한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<QueueTicketResponse>> noShow(
			Authentication authentication,
			@PathVariable Long queueTicketId) {
		return handleCommand(authentication, queueTicketId, QueueAction.NO_SHOW);
	}

	@PostMapping("/{queueTicketId}/cancel")
	@Operation(
		summary = "방문/대기 취소",
		description = "WAITING, CALLED, HOLD 상태 대기표를 CANCELED 상태로 변경하고 방문 상태도 CANCELED로 변경한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<QueueTicketResponse>> cancel(
			Authentication authentication,
			@PathVariable Long queueTicketId) {
		return handleCommand(authentication, queueTicketId, QueueAction.CANCEL);
	}

	private ResponseEntity<ApiResponse<QueueTicketResponse>> handleCommand(
		Authentication authentication,
		Long queueTicketId,
		QueueAction action) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		QueueTicketResponse response = switch (action) {
			case CALL -> queueCommandService.call(principal, queueTicketId);
			case START -> queueCommandService.start(principal, queueTicketId);
			case COMPLETE -> queueCommandService.complete(principal, queueTicketId);
			case HOLD -> queueCommandService.hold(principal, queueTicketId);
			case NO_SHOW -> queueCommandService.noShow(principal, queueTicketId);
			case CANCEL -> queueCommandService.cancel(principal, queueTicketId);
		};
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	private enum QueueAction {
		CALL,
		START,
		COMPLETE,
		HOLD,
		NO_SHOW,
		CANCEL
	}
}
