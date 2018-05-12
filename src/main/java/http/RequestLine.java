package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class RequestLine {

    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

    private HttpMethod method;
    private String path;
    private String queryString;

    public RequestLine(String line) {
        log.debug("request line : {}", line);

        String[] tokens = line.split(" ");
        if (tokens.length != 3) {
            throw new IllegalArgumentException(line + "이 형식에 맞지 않습니다.");
        }

        method = HttpMethod.valueOf(tokens[0]);

        String[] url = tokens[1].split("\\?");
        log.info("url : {}", Arrays.toString(url));

        path = url[0];
        if (url.length == 2) {
            queryString = url[1];
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }
}
