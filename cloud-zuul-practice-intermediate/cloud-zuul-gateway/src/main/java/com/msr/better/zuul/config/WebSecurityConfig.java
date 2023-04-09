package com.msr.better.zuul.config;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023-04-02 17:58
 **/
public class WebSecurityConfig {
}
// @Order(99)
// @Configuration
// public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//     @Override
//     protected void configure(HttpSecurity http) throws Exception {
//         http.authorizeRequests()
//                 .antMatchers("/login", "/servicea/**")
//                 .permitAll()
//                 .anyRequest()
//                 .authenticated()
//                 .and()
//                 .csrf()
//                 .disable();
//     }
// }
