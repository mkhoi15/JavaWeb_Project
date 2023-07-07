/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample.filter;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sample.user.UserDTO;

/**
 *
 * @author ADMIN
 */
@WebFilter(filterName = "AuthenFilter", urlPatterns = {"/*"})
public class AuthenFilter implements Filter {

    private static final boolean debug = true;
    private static List<String> USER_RESOURCES;
    private static List<String> ADMIN_RESOURCES;
    private static List<String> NON_AUTHEN_RESOURCES;
    private static final String AD = "AD";
    private static final String US = "US";
    private static final String LOGIN_PAGE = "login.html";

    private FilterConfig filterConfig = null;

    public AuthenFilter() {
        USER_RESOURCES = new ArrayList<>();
        USER_RESOURCES.add("usPage.jsp");

        ADMIN_RESOURCES = new ArrayList<>();
        ADMIN_RESOURCES.add("admin.jsp");

        NON_AUTHEN_RESOURCES = new ArrayList<>();
        NON_AUTHEN_RESOURCES.add("login.html");
        NON_AUTHEN_RESOURCES.add("login.jsp");
        NON_AUTHEN_RESOURCES.add("MainController");
        NON_AUTHEN_RESOURCES.add("LoginController");
        NON_AUTHEN_RESOURCES.add(".png");
        NON_AUTHEN_RESOURCES.add(".jpg");
        NON_AUTHEN_RESOURCES.add(".gif");
        NON_AUTHEN_RESOURCES.add(".css");
        NON_AUTHEN_RESOURCES.add("fonts");
        NON_AUTHEN_RESOURCES.add("shopping.html");
        NON_AUTHEN_RESOURCES.add("shopping.jsp");
        NON_AUTHEN_RESOURCES.add("viewCart.jsp");
    }

    private void doBeforeProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("AuthenFilter:DoBeforeProcessing");
        }

    }

    private void doAfterProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("AuthenFilter:DoAfterProcessing");
        }

    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            String uri = req.getRequestURI();
            int index = uri.lastIndexOf("/");
            String resource = uri.substring(index + 1);
            boolean checkContain = false;
            for (String value : NON_AUTHEN_RESOURCES) {
                if (uri.contains(value)) {
                    checkContain = true;
                    break;
                }
            }
            if (checkContain) {
                chain.doFilter(request, response);
            } else {
                HttpSession session = req.getSession();
                if (session == null || session.getAttribute("LOGIN_USER") == null) {
                    res.sendRedirect(LOGIN_PAGE);
                } else {
                    UserDTO loginUser = (UserDTO) session.getAttribute("LOGIN_USER");
                    String roleID = loginUser.getRoleID();
                    if (AD.equals(roleID) && ADMIN_RESOURCES.contains(resource)) {
                        chain.doFilter(request, response);
                    } else if (US.equals(roleID) && USER_RESOURCES.contains(resource)) {
                        chain.doFilter(request, response);
                    } else {
                        res.sendRedirect(LOGIN_PAGE);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }

    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("AuthenFilter:Initializing filter");
            }
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("AuthenFilter()");
        }
        StringBuffer sb = new StringBuffer("AuthenFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }

}
