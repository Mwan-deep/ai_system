package AI_Study_Hub.config;

import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.Role;
import AI_Study_Hub.repository.AccountRespository;
import AI_Study_Hub.repository.RoleRespository;
import AI_Study_Hub.service.AuthenticateService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleSignInHandler implements AuthenticationSuccessHandler{
    AccountRespository accountRepository;
    RoleRespository  roleRespository;
    AuthenticateService  authenticateService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User  oAuth2User = (OAuth2User) authentication.getPrincipal();
        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        String picture = oAuth2User.getAttribute("picture");

        HashSet<Role> roles = new HashSet<>();
        Role userRole = roleRespository.findRoleByRoleId("USER");
        roles.add(userRole);

        Account account = Account.builder()
                .userName(email)
                .passwordHash(UUID.randomUUID().toString())
                .fullName(name)
                .email(email)
                .dob(null)
                .gender(null)
                .avatarUrl(picture)
                .bio(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .roles(roles)
                .build();

        account = accountRepository.save(account);

        String token = authenticateService.generateToken(account);
//      response.sendRedirect("https://localhost:5173//oauth2-success?token=" + token);
        response.setContentType("application/json");
        response.getWriter().write("""
                                   {
                                    "authenticated": true,
                                    "token": "%s"
                                   }
                                   """.formatted(token));
    }
}
