package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {

    private static final String COOKIE = "Cookie";
    private static final String CONTENT_LENGTH = "Content-Length";

    private static final Logger log = LoggerFactory.getLogger(HttpHeaders.class);

    private Map<String, String> headers = new HashMap<>();

    void add(String line) {
        log.debug("header : {}", line);
        HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
        headers.put(pair.getKey(), pair.getValue());
    }

    String getHeader(String name) {
        return headers.get(name);
    }

    int getIntHeader(String name) {
        String header = getHeader(name);
        return header == null ? 0 : Integer.parseInt(header);
    }

    int getContentLength() {
        return getIntHeader(CONTENT_LENGTH);
    }

    HttpCookie getCookies() {
        return new HttpCookie(getHeader(COOKIE));
    }

    HttpSession getSession() {
        return HttpSessions.getSession(getCookies().getCookie("JSESSIONID"));
    }
}
