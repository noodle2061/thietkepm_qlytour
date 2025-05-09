    package com.b21dccn449.quanlytour.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// import org.springframework.context.annotation.Bean;
    // import org.springframework.context.annotation.Configuration;
    // import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    // import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    // import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    // import org.springframework.security.crypto.password.PasswordEncoder;
    // import org.springframework.security.web.SecurityFilterChain;
    //
    // import static org.springframework.security.config.Customizer.withDefaults;

    /**
     * Lớp cấu hình cơ bản cho Spring Security.
     * Kích hoạt bảo mật web và cấu hình các quy tắc truy cập.
     * NẾU BẠN ĐÃ EXCLUDE SecurityAutoConfiguration TRONG QuanlytourApplication,
     * THÌ FILE NÀY SẼ KHÔNG CÓ TÁC DỤNG VÀ CÓ THỂ GÂY LỖI BIÊN DỊCH.
     * HÃY COMMENT OUT TOÀN BỘ HOẶC XÓA NẾU MUỐN TẮT HOÀN TOÀN SECURITY.
     */
    // @Configuration
    // @EnableWebSecurity
    public class SecurityConfig {

        /**
         * Cấu hình bộ lọc bảo mật (Security Filter Chain).
         * Mặc định, tất cả các yêu cầu cần được xác thực.
         * Sử dụng form đăng nhập mặc định của Spring Security.
         *
         * @param http Đối tượng HttpSecurity để cấu hình.
         * @return SecurityFilterChain đã được cấu hình.
         * @throws Exception Nếu có lỗi trong quá trình cấu hình.
         */
        // @Bean
        // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //     http
        //         .authorizeHttpRequests(authorize -> authorize
        //             .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
        //             .requestMatchers("/", "/home", "/register", "/login").permitAll() // Cho phép trang đăng ký và đăng nhập
        //             .requestMatchers("/register/process").permitAll() // Cho phép xử lý đăng ký
        //             .anyRequest().authenticated() // Các yêu cầu khác cần xác thực
        //         )
        //         .formLogin(formLogin -> formLogin
        //             .loginPage("/login") // Trang đăng nhập tùy chỉnh
        //             .loginProcessingUrl("/perform_login") // URL xử lý đăng nhập
        //             .defaultSuccessUrl("/booking/new", true) // Chuyển hướng sau khi đăng nhập thành công
        //             .failureUrl("/login?error=true") // Chuyển hướng khi đăng nhập thất bại
        //             .permitAll()
        //         )
        //         .logout(logout -> logout
        //             .logoutUrl("/perform_logout")
        //             .logoutSuccessUrl("/login?logout=true") // Chuyển hướng sau khi đăng xuất
        //             .permitAll()
        //         )
        //         .httpBasic(withDefaults());
        //     return http.build();
        // }

        /**
         * Cung cấp một bean PasswordEncoder.
         *
         * @return một instance của PasswordEncoder.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
    