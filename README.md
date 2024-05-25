# Servlet Router - CSRF Protection

This library provides CSRF protection middleware for __[Servlet Router](https://jirkasa.github.io/servlet-router/)__ library.

- __[MVN Repository](https://mvnrepository.com/artifact/io.github.jirkasa/servlet-router-csrf-protection)__
- __[JavaDoc](https://jirkasa.github.io/servlet-router-csrf-protection/io/github/jirkasa/csrfprotection/package-summary.html)__

##How to use
Implementing CSRF protection using the middleware provided by this library is simple. You just need to create a subclass of the __[CSRFProtection](https://jirkasa.github.io/servlet-router-csrf-protection/io/github/jirkasa/csrfprotection/CSRFProtection.html)__ class and implement the handleError method. This method is called when no CSRF token or an incorrect CSRF token is provided, and you can use it to, for example, send a page with an error message. Then, you just need to register the middleware at the beginning of your main router. It will take care of generating the CSRF token for each session, setting it as an attribute in the request, and of course, verifying for each request sent with an HTTP method that changes data whether the CSRF token is provided and correct. The following example shows the creation of the middleware and its registration.
```java
public class MyCSRFProtection extends CSRFProtection {
    @Override
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
```
```java
public class AppRouter extends HttpRouter {
    public AppRouter() {
        register(new BaseURLAttributeSetter());
        register(new MyCSRFProtection());
        register("/", HomeController.class);
        register("/games", new GamesRouter());
        registerErrorController(MyErrorController.class);
    }
}
```
The CSRFProtection middleware sets the CSRF token as a request attribute, so you can then add it to forms in JSP pages as a hidden input.
```html
<form action="${BASE_URL}/login" method="POST">
    <input name="CSRF_TOKEN" value="${CSRF_TOKEN}" type="hidden">
    <input name="username" type="text" required>
    <input name="password" type="password" required>
    <button type="submit">Login</button>
</form>
```
If you send requests using JavaScript, you can set CSRF token as a global variable.
```html
<script>
    const CSRF_TOKEN = ${CSRF_TOKEN};
</script>
```
By default, only POST, PUT, PATCH, and DELETE methods are protected against CSRF attacks. This is because methods like GET, HEAD, TRACE, and OPTIONS are assumed not to be used for changing any data on the server. Forms submitted via the GET method should only be used to retrieve data, so they are not considered dangerous in terms of CSRF attacks. However, if you want to change this, you can define which HTTP methods should be secured (although I do not recommend it).
```java
public class MyCSRFProtection extends CSRFProtection {
    public MyCSRFProtection() {
        // only POST and PUT methods will be CSRF protected
        super(new String[]{"POST", "PUT"});
    }
    
    @Override
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
```
If the attribute name "CSRF_TOKEN" does not suit you, it can also be changed, as shown in the following example.
```java
public class MyCSRFProtection extends CSRFProtection {
    public MyCSRFProtection() {
        super("MY_CSRF_TOKEN");
    }
    
    @Override
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
```
If you want to change both the attribute name and the protected methods at the same time, you can pass two parameters to the constructor, as shown in the following example.
```java
public class MyCSRFProtection extends CSRFProtection {
    public MyCSRFProtection() {
        super("MY_CSRF_TOKEN", new String[]{"POST", "PUT"});
    }
    
    @Override
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
```