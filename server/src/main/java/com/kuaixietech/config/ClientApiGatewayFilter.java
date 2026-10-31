package com.kuaixietech.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Matches the frontend Vite gateway: clients never send /api, Java controllers live under /api.
 * WeChat / App have no Vite proxy, so this filter is the production equivalent of
 * {@code rewrite: path => '/api' + path}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientApiGatewayFilter extends OncePerRequestFilter {
    private static final Pattern CLIENT_PREFIX = Pattern.compile("^/(public|rpc|portal|support|me|auth)(/|$)");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String context = request.getContextPath() == null ? "" : request.getContextPath();
        String path = request.getRequestURI().substring(context.length());
        if (path.startsWith("/api/") || path.equals("/api") || !CLIENT_PREFIX.matcher(path).find()) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new PrefixedRequest(request, context, path), response);
    }

    private static final class PrefixedRequest extends HttpServletRequestWrapper {
        private final String context;
        private final String prefixed;

        private PrefixedRequest(HttpServletRequest request, String context, String path) {
            super(request);
            this.context = context;
            this.prefixed = "/api" + path;
        }

        @Override public String getRequestURI() { return context + prefixed; }
        @Override public String getServletPath() { return prefixed; }
        @Override public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            url.append(getScheme()).append("://").append(getServerName());
            int port = getServerPort();
            if (port > 0 && port != 80 && port != 443) url.append(':').append(port);
            url.append(getRequestURI());
            return url;
        }
    }
}
