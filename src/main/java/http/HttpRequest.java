package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String line = br.readLine();
        if (line == null) {
            return;
        }
        log.info("request line : {}", line);

        this.method = line.split(" ")[0];
        String url = HttpRequestUtils.getUrl(line);

        if ("GET".equals(method)) {
            int index = url.indexOf("?");
            this.path = url.substring(0, index);
            String queryString = url.substring(index + 1);
            this.params = HttpRequestUtils.parseQueryString(queryString);
        } else {
            this.path = url;
        }

        line = br.readLine();
        while (!"".equals(line)) {
            log.info("header : {}", line);
            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
            headers.put(pair.getKey(), pair.getValue());
            line = br.readLine();
        }

        if ("POST".equals(method)) {
            line = br.readLine();
            log.info("body : {}", line);
            this.params = HttpRequestUtils.parseQueryString(line);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}
