package AI_Study_Hub.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {
    static String[] PUBLIC_ENDPOINT = {
            "/api/account",
            "/api/authen",
            "/api/authen/introspec",
            "/api/auth/**",
            "/api/v1/share/download/**",
            "/api/authen/verifyOtp2Layer",
            "/api/account/infor/{id}"

    };
    static String[] MUST_BE_AUTHENTICATE = {"/api/account/change-password", "/api/authen/logout"};

    @Autowired
    CustomJwtDecoder customJwtDecoder;

    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    GoogleSignInHandler googleSignInHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
        httpSecurity
                .authorizeHttpRequests(requestMatcherRegistry ->
                        requestMatcherRegistry
                                //Cho Phep Su Dung Khong Can Quyen, Ai Cung Co The Truy Cap Duoc
                                .requestMatchers(PUBLIC_ENDPOINT).permitAll()
                                //Lay Het Tai Khoan Ra Do Admin Quan Ly - Role: Admin
                                .requestMatchers(HttpMethod.GET, "/api/account/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/account/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/account/createAccountByAdmin").hasRole("ADMIN")
                                //(phải login)
                                .requestMatchers(HttpMethod.POST, MUST_BE_AUTHENTICATE).authenticated()
                                .anyRequest()
                                .authenticated())
                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(customAccessDeniedHandler));

        httpSecurity.oauth2Login(oAuth2 -> oAuth2.successHandler(googleSignInHandler));

        httpSecurity.oauth2ResourceServer(oAuth2 ->
                oAuth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();

    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return authenticationConverter;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:5173");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource  = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
