package egovframework.com.security;

import java.util.Arrays;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.support.MultipartFilter;

import egovframework.com.cmm.filter.HTMLTagFilter;
import egovframework.com.jwt.JwtAuthenticationEntryPoint;
import egovframework.com.jwt.JwtAuthenticationFilter;
import egovframework.healthcenter.common.logging.RequestLoggingFilter;
import jakarta.servlet.MultipartConfigElement;

/**
 * fileName : SecurityConfig
 * author : crlee
 * date : 2023/06/10
 * description :
 * ===========================================================
 * DATE AUTHOR NOTE
 * -----------------------------------------------------------
 * 2023/06/10 crlee 최초 생성
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 인증 예외 List
    private String[] AUTH_WHITELIST = {
            "/",
            "/error", // 에러 페이지
            "/api/auth/login", // 보건소 로그인
            "/api/auth/reissue", // 보건소 토큰 재발급
            "/api/common-codes", // 보건소 공통코드 일괄 조회
            "/api/common-codes/**", // 보건소 공통코드 조회
            "/api/service-types", // 보건소 업무 유형 조회
            "/api/congestion/current", // 보건소 현재 혼잡도 조회

            /* 정적 리소스 */
            "/css/**",
            "/js/**",
            "/images/**",
            "/static/**",
            "/favicon.ico",
            "/index.html",

            /* swagger */
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**", // Swagger UI 정적 리소스

    };

    @Value("${Globals.Allow.Origin:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public JwtAuthenticationFilter authenticationTokenFilterBean() throws Exception {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration(
            RequestLoggingFilter requestLoggingFilter) {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>(requestLoggingFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT", "PATCH"));
        configuration.setAllowedOrigins(parseAllowedOrigins());
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private java.util.List<String> parseAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }

    @Bean
    public HTMLTagFilter htmlTagFilter() {
        return new HTMLTagFilter();
    }

    // 멀티파트 필터 빈
    @Bean
    public MultipartFilter multipartFilter() {
        return new MultipartFilter();
    }

    // 서블릿 컨테이너에 멀티파트 구성을 제공하기 위한 설정
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxRequestSize(DataSize.ofMegabytes(100L));
        factory.setMaxFileSize(DataSize.ofMegabytes(100L));
        return factory.createMultipartConfig();
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 페이지는 ADMIN만 접근
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                        .requestMatchers("/api/visits/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/api/queues/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/reservations").hasAnyRole("CITIZEN", "GUARDIAN", "STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reservations/*").hasAnyRole("CITIZEN", "GUARDIAN", "ADMIN")
                        .requestMatchers("/api/reservation-slots/**").authenticated()
                        .requestMatchers("/api/members/me").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(securityContext ->
                        securityContext.securityContextRepository(new NullSecurityContextRepository()))
                .requestCache(requestCache -> requestCache.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(characterEncodingFilter(), ChannelProcessingFilter.class)
                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestLoggingFilter(), JwtAuthenticationFilter.class)
                .addFilterBefore(multipartFilter(), CsrfFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
                .build();
    }

}
