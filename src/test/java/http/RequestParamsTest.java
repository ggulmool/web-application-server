package http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RequestParamsTest {

    @Test
    public void add() {
        RequestParams params = new RequestParams();
        params.addQueryString("id=1");
        params.addBody("userId=ggulmool&password=password");
        assertEquals("1", params.getParameter("id"));
        assertEquals("ggulmool", params.getParameter("userId"));
        assertEquals("password", params.getParameter("password"));
    }
}
