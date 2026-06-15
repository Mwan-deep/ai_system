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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {
    static String[] PUBLIC_ENDPOINT = {
            "/account",
            "/authen",
            "/authen/introspec",
            "/auth/**",
            "/api/v1/share/download/**" // <--- THÊM DÒNG NÀY CỦA CHÚNG TA VÀO
    };
    static String[] MUST_BE_AUTHENTICATE = {"/account/change-password", "/authen/logout"};

    @Autowired
    CustomJwtDecoder customJwtDecoder;

    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
        httpSecurity
                .authorizeHttpRequests(requestMatcherRegistry ->
                        requestMatcherRegistry
                                //Cho Phep Su Dung Khong Can Quyen, Ai Cung Co The Truy Cap Duoc
                                .requestMatchers(PUBLIC_ENDPOINT).permitAll()
                                //Lay Het Tai Khoan Ra Do Admin Quan Ly - Role: Admin
                                .requestMatchers(HttpMethod.GET, "/account/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/account/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/account/createAccountByAdmin").hasRole("ADMIN")
                                //(phải login)
                                .requestMatchers(HttpMethod.POST, MUST_BE_AUTHENTICATE).authenticated()
                                .anyRequest()
                                .authenticated())
                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(customAccessDeniedHandler))
        ;


        httpSecurity.oauth2ResourceServer(oAuth2 ->
                oAuth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();

    }
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return authenticationConverter;
    }
}
