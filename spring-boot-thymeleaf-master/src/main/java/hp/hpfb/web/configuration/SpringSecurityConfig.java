package hp.hpfb.web.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import hp.hpfb.web.handler.CdnAccessDeniedHandler;
import hp.hpfb.web.handler.CdnAuthenticationFailureHandler;
import hp.hpfb.web.handler.CdnSuccessHandler;
import hp.hpfb.web.handler.RestAuthenticationEntryPoint;
import hp.hpfb.web.service.utils.CdnPasswordProvider;

@Configuration
@EnableWebSecurity(debug = false)
@Order
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CdnAccessDeniedHandler accessDeniedHandler;
    @Autowired
    private CdnSuccessHandler cdnSuccessHandler;
    @Autowired
	private CdnAuthenticationFailureHandler cdnFailureHandler;
    @Autowired
	private CdnPasswordProvider cdnPasswordProvider;
    @Autowired
	private RestAuthenticationEntryPoint unauthorizedHandler;

	@Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable()
                .authorizeRequests()
					.antMatchers("/*.gif","/", "/login", "/leftMenu", "/validateXML", "/renderXML", "/xmlHtml", "/j_spring_security_check", "/downloadXML", "/js/**","/css/**").permitAll()
					.antMatchers("/admin/**").hasAnyRole("ADMIN")
					.anyRequest().authenticated()
                .and()
                .formLogin()
					.successHandler(cdnSuccessHandler)
					.failureHandler(cdnFailureHandler)
					.usernameParameter("userId")
					.passwordParameter("password")
					.loginPage("/login")
					.failureUrl("/")
					.permitAll()
					.and()
                .logout()
					.permitAll()
					.and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(unauthorizedHandler);
    }

    // create two users, admin and user
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	auth.authenticationProvider(cdnPasswordProvider);
    }

}
