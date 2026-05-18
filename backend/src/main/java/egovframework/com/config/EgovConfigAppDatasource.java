package egovframework.com.config;

import jakarta.annotation.PostConstruct;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @ClassName : EgovConfigAppDatasource.java
 * @Description : DataSource 설정
 *
 * @author : 윤주호
 * @since  : 2021. 7. 20
 * @version : 1.0
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2021. 7. 20    윤주호               최초 생성
 * </pre>
 *
 */
@Configuration
public class EgovConfigAppDatasource {

    private final Environment env;

    public EgovConfigAppDatasource(Environment env) {
        this.env = env;
    }

	private String dbType;

	private String className;

	private String url;

	private String userName;

	private String password;

	private String timeZone;

	@PostConstruct
	void init() {
		dbType = env.getProperty("Globals.DbType");
		//Exception 처리 필요
		className = env.getProperty("Globals." + dbType + ".DriverClassName");
		url = env.getProperty("Globals." + dbType + ".Url");
		userName = env.getProperty("Globals." + dbType + ".UserName");
		password = env.getProperty("Globals." + dbType + ".Password");
		timeZone = validateTimeZone(env.getProperty("Globals.TimeZone", "Asia/Seoul"));
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(timeZone)));
	}

	/**
	 * @return [dataSource 설정] basicDataSource 설정
	 */
	private DataSource basicDataSource() {
		BasicDataSource basicDataSource = new BasicDataSource();
		basicDataSource.setDriverClassName(className);
		basicDataSource.setUrl(url);
		basicDataSource.setUsername(userName);
		basicDataSource.setPassword(password);
		basicDataSource.setConnectionInitSqls(List.of("SET TIME ZONE '" + timeZone + "'"));
		return basicDataSource;
	}

	private String validateTimeZone(String value) {
		if (value == null || value.isBlank()) {
			return "Asia/Seoul";
		}
		String trimmed = value.trim();
		if (!trimmed.matches("[A-Za-z0-9_+./:-]+")) {
			throw new IllegalArgumentException("Invalid time zone value: " + trimmed);
		}
		try {
			ZoneId.of(trimmed);
		} catch (DateTimeException exception) {
			throw new IllegalArgumentException("Invalid time zone value: " + trimmed, exception);
		}
		return trimmed;
	}

	/**
	 * @return [DataSource 설정]
	 */
	@Bean(name = {"dataSource", "egov.dataSource", "egovDataSource"})
	public DataSource dataSource() {
		return basicDataSource();
	}
}
