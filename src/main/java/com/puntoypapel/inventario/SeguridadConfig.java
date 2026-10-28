package com.puntoypapel.inventario;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SeguridadConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return username -> usuarios.findByNombreUsuario(username.toLowerCase())
                .map(usuario -> (UserDetails) User.withUsername(usuario.getNombreUsuario())
                        .password(usuario.getPasswordHash())
                        .roles(usuario.getRol().name())
                        .disabled(!usuario.isActivo())
                        .build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "Usuario no encontrado"));
    }

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sesiones -> sesiones.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/api/usuarios/**", "/api/saludo", "/", "/index.html",
                                "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/productos/**")
                                .hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers("/api/productos/**").hasRole("ADMIN")
                        .requestMatchers("/api/ventas/resumen").hasRole("ADMIN")
                        .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .anyRequest().authenticated())
                .httpBasic(basico -> { });
        return http.build();
    }
}
