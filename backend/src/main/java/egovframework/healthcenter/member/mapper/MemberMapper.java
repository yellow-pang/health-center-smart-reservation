package egovframework.healthcenter.member.mapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

@Repository("memberMapper")
public class MemberMapper extends EgovAbstractMapper {

	public MemberVO selectActiveMemberByEmail(String email) {
		return selectOne("MemberMapper.selectActiveMemberByEmail", email);
	}

	public MemberVO selectActiveMemberById(Long memberId) {
		return selectOne("MemberMapper.selectActiveMemberById", memberId);
	}

	public MemberVO selectActiveMemberByRefreshToken(String refreshToken) {
		return selectOne("MemberMapper.selectActiveMemberByRefreshToken", refreshToken);
	}

	public void insertRefreshToken(Long memberId, String refreshToken, LocalDateTime expiresAt) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("refreshToken", refreshToken);
		params.put("expiresAt", expiresAt);
		insert("MemberMapper.insertRefreshToken", params);
	}

	public int revokeRefreshTokenByToken(String refreshToken) {
		return update("MemberMapper.revokeRefreshTokenByToken", refreshToken);
	}

	public int revokeMemberRefreshToken(Long memberId, String refreshToken) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("refreshToken", refreshToken);
		return update("MemberMapper.revokeMemberRefreshToken", params);
	}
}
