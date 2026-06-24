//package config;
//
//import org.springframework.context.annotation.*;
//        import org.springframework.web.cors.*;
//        import org.springframework.web.filter.CorsFilter;
//
//import java.util.Arrays;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(Arrays.asList(
//                "https://golf-charity-subscription-platform-frontend-oiq3qlhh5.vercel.app",
//                "http://localhost:4000",
//                "http://localhost:5173"
//        ));
//        config.addAllowedMethod("*");
//        config.addAllowedHeader("*");
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/api/**", config);
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }
//}