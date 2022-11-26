package com.team3d.awad.config;

import com.team3d.awad.repository.UserRepository;
import com.team3d.awad.security.AuthenticationEntryPoint;
import com.team3d.awad.security.CustomUserDetailsService;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.security.oauth2.CustomOAuth2UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LogManager.getLogger(SecurityConfig.class);
    private final TokenProvider tokenProvider;

    private final UserRepository userRepository;

    private final AuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(TokenProvider tokenProvider, UserRepository userRepository,
                          AuthenticationEntryPoint authenticationEntryPoint) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers(HttpMethod.GET,
                        "/oauth2/authorization/**",
                        "/actuator",
                        "/actuator/**",
                        "/auth/login",
                        "/login/**",
                        "/authorize/resume/**",
                        "/oauth2/callback/**",
                        "**/oauth2/**",
                        "**/o/oauth2/v2/auth/**").permitAll()
                .anyExchange().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                    .oauth2Login(oauth2 -> oauth2
                            .authenticationMatcher(new PathPatternParserServerWebExchangeMatcher("/oauth2/callback/{registrationId}"))
                            .authenticationSuccessHandler(this::onAuthenticationSuccess)
                    )
                .logout()
                    .logoutSuccessHandler(logoutSuccessHandler())
                .and()
                    .oauth2Client()
                .and().build();
    }

    /**
     * Configures the logout handling to log users out of Auth0 after successful logout from the application.
     * @return a {@linkplain ServerLogoutSuccessHandler} that will be called on successful logout.
     */
    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        // Change this as needed to URI where users should be redirected to after logout
        String clientId = "NgsQ9u5hqL9HdNZyuMYytajABb9ED2G7";
        String returnTo = "http://localhost:8080/";

        // Build the URL to log the user out of Auth0 and redirect them to the home page.
        // URL will look like https://YOUR-DOMAIN/v2/logout?clientId=YOUR-CLIENT-ID&returnTo=http://localhost:3000
        String logoutUrl = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8080/v2/logout?client_id={clientId}&returnTo={returnTo}")
                .encode()
                .buildAndExpand(clientId, returnTo)
                .toUriString();

        RedirectServerLogoutSuccessHandler handler = new RedirectServerLogoutSuccessHandler();
        handler.setLogoutSuccessUrl(URI.create(logoutUrl));
        return handler;
    }

    @Bean
    protected ReactiveAuthenticationManager reactiveAuthenticationManager() {
        LOGGER.info("Security setup Authentication Manager!");
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService();
        return authentication -> {
            final String username = authentication.getPrincipal().toString();
            return userDetailsService.findByUsername(username)
                    .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                    .map(user -> {
                        return new UsernamePasswordAuthenticationToken(user.getUsername(),
                                null, user.getAuthorities());
                    });
        };

//        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
//                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
//        authenticationManager.setPasswordEncoder(passwordEncoder());
//        return authenticationManager;
    }

//    @Bean
//    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
//        final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();
//
//        return (userRequest) -> delegate.loadUser(userRequest).map(oidcUser -> {
//            Map<String, Object> attributes = oidcUser.getAttributes();
//            LOGGER.info("OAuth2 response: {}", attributes);
//
//            LOGGER.info("Authorities: {}", oidcUser.getAuthorities());
//            return new CustomUserDetails(oidcUser.getEmail(), oidcUser.getEmail(),
//                    null, oidcUser.getAuthorities());
//        });
//    }

    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        LOGGER.info("Security setup OAuth2 User Service!");
        return new CustomOAuth2UserService();
    }

    private Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange, Authentication authentication) {
        LOGGER.info("Hit Authentication Success Handler!");
        RedirectServerAuthenticationSuccessHandler redirectServerAuthenticationSuccessHandler =
                new RedirectServerAuthenticationSuccessHandler();

        String token = tokenProvider.createToken(authentication);
        URI redirectUri = UriComponentsBuilder
                .fromUriString("/")
                .queryParam("token", token)
                .build().toUri();
        redirectServerAuthenticationSuccessHandler.setLocation(redirectUri);

        return redirectServerAuthenticationSuccessHandler.onAuthenticationSuccess(exchange, authentication);
    }
}