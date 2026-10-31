package com.kuaixietech.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/** Fail closed for revoked/deleted user accounts after JWT verification.
 * No identity is taken from URL parameters or headers. Public catalog remains anonymous.
 */
public final class ActiveProfileFilter extends OncePerRequestFilter {
  private final DomainAccessGuard guard;
  public ActiveProfileFilter(DomainAccessGuard guard) {this.guard=guard;}
  @Override protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    if ("GET".equalsIgnoreCase(request.getMethod()) && (path.equals("/api/auth/captcha") || path.equals("/auth/captcha"))) return true;
    if ("POST".equalsIgnoreCase(request.getMethod()) && (path.equals("/api/auth/login") || path.equals("/auth/login") || path.equals("/api/auth/wechat") || path.equals("/auth/wechat") || path.equals("/api/auth/send-code") || path.equals("/auth/send-code") || path.equals("/api/auth/register") || path.equals("/auth/register") || path.equals("/api/auth/reset-password") || path.equals("/auth/reset-password") || path.equals("/api/auth/recovery-help") || path.equals("/auth/recovery-help"))) return true;
    if (!"GET".equalsIgnoreCase(request.getMethod())) return false;
    return path.startsWith("/api/public/") || path.startsWith("/public/");
  }
  @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
      throws ServletException,IOException {
    Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
    if(authentication instanceof JwtAuthenticationToken && authentication.isAuthenticated()) {
      try {guard.requireActiveProfile();}
      catch(org.springframework.security.access.AccessDeniedException denied) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN,"Inactive account");
        return;
      }
    }
    chain.doFilter(request,response);
  }
}
