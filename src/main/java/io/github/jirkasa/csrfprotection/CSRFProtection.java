package io.github.jirkasa.csrfprotection;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.github.jirkasa.servletrouter.HttpMiddleware;

/**
 * Middleware for CSRF protection. It generates a CSRF token for each session and sets it as an attribute for each request. It also checks that correct CSRF token has been passed for CSRF protected HTTP methods. By default POST, PUT, PATCH and DELETE methods are set as CSRF protected. This can be changed by passing array of HTTP methods to constructor. Name of CSRF token attribute is by default "CSRF_TOKEN", but that can also be changed by passing name to constructor.
 */
public abstract class CSRFProtection extends HttpMiddleware {
	/** Default name to be used for CSRF token attribute. */
	private static final String DEFAULT_CSRF_TOKEN_ATTRIBUTE_NAME = "CSRF_TOKEN";
	/** Default methods that are CSRF protected. */
	private static final String[] DEFAULT_PROTECTED_METHODS = {"POST", "PUT", "PATCH", "DELETE"};
	
	/** Name of CSRF token attribute. */
	private String csrfTokenAttributeName;
	/** CSRF protected HTTP methods. */
	private Set<String> protectedMethods = new HashSet<>();
	
	/**
	 * Creates new CSRF protection middleware (protected methods are POST, PUT, PATCH and DELETE; name of CSRF attribute is "CSRF_TOKEN").
	 */
	public CSRFProtection() {
		this(DEFAULT_CSRF_TOKEN_ATTRIBUTE_NAME);
	}
	
	/**
	 * Creates new CSRF protection middleware (protected methods are POST, PUT, PATCH and DELETE).
	 * @param csrfTokenAttributeName Name of CSRF token attribute.
	 */
	public CSRFProtection(String csrfTokenAttributeName) {
		this(csrfTokenAttributeName, DEFAULT_PROTECTED_METHODS);
	}
	
	/**
	 * Creates new CSRF protection middleware (name of CSRF attribute is "CSRF_TOKEN").
	 * @param protectedMethods HTTP methods to be protected.
	 */
	public CSRFProtection(String[] protectedMethods) {
		this(DEFAULT_CSRF_TOKEN_ATTRIBUTE_NAME, protectedMethods);
	}
	
	/**
	 * Creates new CSRF protection middleware.
	 * @param csrfTokenAttributeName Name of CSRF token attribute.
	 * @param protectedMethods HTTP methods to be protected.
	 */
	public CSRFProtection(String csrfTokenAttributeName, String[] protectedMethods) {
		this.csrfTokenAttributeName = csrfTokenAttributeName;
		for (String method : protectedMethods) {
			this.protectedMethods.add(method);
		}
	}
	
	@Override
	public boolean handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession session = request.getSession();
		
		String csrfToken = (String) session.getAttribute(csrfTokenAttributeName);
		String requestMethod = request.getMethod();
		String passedCsrfToken = (String) request.getParameter(csrfTokenAttributeName);
		
		if (this.protectedMethods.contains(requestMethod)) {
			if (
				csrfToken == null
				|| passedCsrfToken == null
				|| !csrfToken.equals(passedCsrfToken)
		    ) {
				handleError(request, response);
				return false;
			}
				
		}
		
		if (csrfToken == null) {
			session.setAttribute(csrfTokenAttributeName, generateToken());
		}
		
		return true;
	}
	
	/**
	 * Handles request sent with no or bad CSRF token.
	 * @param request Request.
	 * @param response Response.
	 * @throws Exception
	 */
	public abstract void handleError(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public static String generateToken() throws NoSuchAlgorithmException {
	    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
	    byte[] data = new byte[16];
	    secureRandom.nextBytes(data);
	
	    return Base64.getEncoder().encodeToString(data);
	}
}