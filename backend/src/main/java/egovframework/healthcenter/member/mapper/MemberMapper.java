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

	public MemberVO selectActiveMemberByNameAndPhone(String name, String phone) {
		Map<String, Object> params = new HashMap<>();
		params.put("name", name);
		params.put("phone", phone);
		return selectOne("MemberMapper.selectActiveMemberByNameAndPhone", params);
	}

	public MemberVO selectActiveMemberByEmailAndPhone(String email, String phone) {
		Map<String, Object> params = new HashMap<>();
		params.put("email", email);
		params.put("phone", phone);
		return selectOne("MemberMapper.selectActiveMemberByEmailAndPhone", params);
	}

	public MemberVO selectActiveMemberByPasswordResetToken(String tokenHash) {
		return selectOne("MemberMapper.selectActiveMemberByPasswordResetToken", tokenHash);
	}

	public MemberVO selectActiveMemberBySocialAccount(String provider, String providerUserId) {
		Map<String, Object> params = new HashMap<>();
		params.put("provider", provider);
		params.put("providerUserId", providerUserId);
		return selectOne("MemberMapper.selectActiveMemberBySocialAccount", params);
	}

	public void insertSocialMember(String email, String password, String name) {
		Map<String, Object> params = new HashMap<>();
		params.put("email", email);
		params.put("password", password);
		params.put("name", name);
		insert("MemberMapper.insertSocialMember", params);
	}

	public void insertSocialAccount(
		Long memberId,
		String provider,
		String providerUserId,
		String providerEmail,
		LocalDateTime linkedAt
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("provider", provider);
		params.put("providerUserId", providerUserId);
		params.put("providerEmail", providerEmail);
		params.put("linkedAt", linkedAt);
		insert("MemberMapper.insertSocialAccount", params);
	}

	public void insertRefreshToken(Long memberId, String refreshToken, LocalDateTime expiresAt) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("refreshToken", refreshToken);
		params.put("expiresAt", expiresAt);
		insert("MemberMapper.insertRefreshToken", params);
	}

	public void insertPasswordResetToken(Long memberId, String tokenHash, LocalDateTime expiresAt) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("tokenHash", tokenHash);
		params.put("expiresAt", expiresAt);
		insert("MemberMapper.insertPasswordResetToken", params);
	}

	public int markMemberPasswordResetTokensUsed(Long memberId) {
		return update("MemberMapper.markMemberPasswordResetTokensUsed", memberId);
	}

	public int markPasswordResetTokenUsed(String tokenHash) {
		return update("MemberMapper.markPasswordResetTokenUsed", tokenHash);
	}

	public int updateMemberPassword(Long memberId, String password) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("password", password);
		return update("MemberMapper.updateMemberPassword", params);
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

	public int revokeMemberRefreshTokens(Long memberId) {
		return update("MemberMapper.revokeMemberRefreshTokens", memberId);
	}
}
