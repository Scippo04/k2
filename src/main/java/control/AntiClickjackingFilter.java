package control;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class AntiClickjackingFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inizializzazione del filtro
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Aggiungi gli header anti-clickjacking
        httpResponse.addHeader("X-Frame-Options", "DENY");
        httpResponse.addHeader("Content-Security-Policy", "frame-ancestors 'none'");

        // Passa la richiesta al prossimo filtro nella catena
        chain.doFilter(request, response);
    }

    public void destroy() {
        // Pulizia delle risorse del filtro
    }
}
