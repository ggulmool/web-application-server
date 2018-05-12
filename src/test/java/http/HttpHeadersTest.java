package http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpHeadersTest {

    @Test
    public void add() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Connection: keep-alive");
        assertEquals("keep-alive", headers.getHeader("Connection"));
    }
}
