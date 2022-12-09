package com.team3d.awad.config;

import com.team3d.awad.repository.UserRepository;
import com.team3d.awad.security.AuthenticationEntryPoint;
import com.team3d.awad.security.CustomUserDetails;
import com.team3d.awad.security.CustomUserDetailsService;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.security.oauth2.CustomOAuth2UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
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
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurerComposite;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import org.springframework.web.server.session.WebSessionManager;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LogManager.getLogger(SecurityConfig.class);
    private final TokenProvider tokenProvider;

    private AuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static final String ALLOWED_HEADERS = "x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN";
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS";
    private static final String ALLOWED_ORIGIN = "*";
    private static final String MAX_AGE = "3600";
    private static final String FRONTEND_LOCALHOST = "http://localhost:3000";
    private static final String FRONTEND_STAGING = "http://localhost:3000";

    public CorsConfigurationSource createCorsConfigSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000/");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.setExposedHeaders(Arrays.asList("Authorization", "content-type", "Access-Control-Allow-Headers"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "content-type", "Access-Control-Allow-Headers"));

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http

//                .requestCache().requestCache(NoOpServerRequestCache.getInstance())
//                .and()
//                    .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                    .authorizeExchange()
                        .pathMatchers(
                                "/favicon.ico",
                                "/oauth2/authorization/**",
                                "/actuator",
                                "/actuator/**",
                                "/auth/login",
                                "/login/**",
                                "/authorize/resume/**",
                                "/oauth2/callback/**",
                                "**/oauth2/**",
                                "**/o/oauth2/v2/auth/**",
                                "/api/**").permitAll()
                        .anyExchange().permitAll()
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint((swe, e) -> Mono.fromRunnable(() -> {
                        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    })).accessDeniedHandler((swe, e) -> Mono.fromRunnable(() -> {
                        swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    }))
                .and()
                    .oauth2Login(oauth2 -> oauth2
                            .authenticationMatcher(new PathPatternParserServerWebExchangeMatcher("/oauth2/callback/{registrationId}"))
                            .authenticationSuccessHandler(this::onAuthenticationSuccess)
                    )
                    .formLogin().disable()
                .logout()
                    .logoutSuccessHandler(logoutSuccessHandler())
                .and()
                    .oauth2Client()
                .and()
                .formLogin().disable()
                .cors().configurationSource(createCorsConfigSource()).and()
                .csrf().disable()
                .httpBasic().disable()
                .build();
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

//    @Bean
//    public ReactiveUserDetailsService userDetailsService() {
//        LOGGER.info("Security setup UserDetails Service!");
//        return new CustomUserDetailsService();
//    }

//    @Bean
//    protected ReactiveAuthenticationManager reactiveAuthenticationManager() {
//        LOGGER.info("Security setup Authentication Manager!");
//        CustomUserDetailsService userDetailsService = new CustomUserDetailsService();
//        return authentication -> {
//            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//            LOGGER.info("Authenticate with {}", userDetails);
//            return userDetailsService.findByUserId(userDetails.getId())
//                    .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
//                    .map(user -> new UsernamePasswordAuthenticationToken(user.getUsername(),
//                                user.getPassword(), user.getAuthorities()));
//        };
//
//        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
//                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
//        authenticationManager.setPasswordEncoder(passwordEncoder());
//        return authenticationManager;
//    }

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
                .fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("token", token)
                .build().toUri();
        redirectServerAuthenticationSuccessHandler.setLocation(redirectUri);

        return redirectServerAuthenticationSuccessHandler.onAuthenticationSuccess(exchange, authentication);
    }
}
