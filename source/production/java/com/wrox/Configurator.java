package com.wrox;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;

/**
 * Created by EKAC on 17.10.2017 г..
 */
@WebListener
public class Configurator implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        ServletContext context = event.getServletContext();
        FilterRegistration.Dynamic registration = context.addFilter(
                "authenticationFilter", (Filter) new AuthenticationFilter()
        );
        registration.setAsyncSupported(true);
        registration.addMappingForUrlPatterns(
                null, false, "/tickets", "/chat", "/sessions"
        );
    }
    @Override
    public void contextDestroyed(ServletContextEvent event) { }
}