package http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RequestLineTest {

    @Test
    public void create_method() {
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals(HttpMethod.GET, line.getMethod());
        assertEquals("/index.html", line.getPath());

        line = new RequestLine("POST /user/list HTTP/1.1");
        assertEquals(HttpMethod.POST, line.getMethod());
        assertEquals("/user/list", line.getPath());
    }

    @Test
    public void create_path_and_params() {
        RequestLine line = new RequestLine("GET /user/create?userId=ggulmool&password=passowrd HTTP/1.1");
        assertEquals(HttpMethod.GET, line.getMethod());
        assertEquals("/user/create", line.getPath());
        assertEquals("userId=ggulmool&password=passowrd", line.getQueryString());
    }
}
