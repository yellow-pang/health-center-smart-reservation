package egovframework.healthcenter.office.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import egovframework.healthcenter.office.dto.ServiceTypeCreateRequest;
import egovframework.healthcenter.office.dto.ServiceTypeUpdateRequest;

@Repository("officeMapper")
public class OfficeMapper extends EgovAbstractMapper {

	public List<ServiceTypeVO> selectActiveServiceTypes() {
		return selectList("OfficeMapper.selectActiveServiceTypes");
	}

	public List<ServiceTypeVO> selectAllServiceTypes() {
		return selectList("OfficeMapper.selectAllServiceTypes");
	}

	public ServiceTypeVO selectServiceTypeById(Long serviceTypeId) {
		return selectOne("OfficeMapper.selectServiceTypeById", serviceTypeId);
	}

	public void insertServiceType(Long healthCenterId, ServiceTypeCreateRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("code", request.code());
		params.put("name", request.name());
		params.put("description", request.description());
		params.put("defaultCapacity", request.defaultCapacity());
		insert("OfficeMapper.insertServiceType", params);
	}

	public int updateServiceType(Long serviceTypeId, ServiceTypeUpdateRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("serviceTypeId", serviceTypeId);
		params.put("name", request.name());
		params.put("description", request.description());
		params.put("defaultCapacity", request.defaultCapacity());
		params.put("active", request.active());
		return update("OfficeMapper.updateServiceType", params);
	}

	public int deactivateServiceType(Long serviceTypeId) {
		return update("OfficeMapper.deactivateServiceType", serviceTypeId);
	}

	public int activateServiceType(Long serviceTypeId) {
		return update("OfficeMapper.activateServiceType", serviceTypeId);
	}

	public List<StaffVO> selectActiveStaff() {
		return selectList("OfficeMapper.selectActiveStaff");
	}

	public List<ServiceWindowMappingVO> selectServiceWindowMappings() {
		return selectList("OfficeMapper.selectServiceWindowMappings");
	}
}
