package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            log.info("request line : {}", line);

            if (line == null) {
                return;
            }

            String url = HttpRequestUtils.getUrl(line);
            int contentLength = 0;
            while (!"".equals(line)) {
                //log.info("header : {}", line);
                line = br.readLine();
                if (line.contains("Content-Length")) {
                    HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
                    contentLength = Integer.parseInt(pair.getValue());
                }
            }

            if (url.startsWith("/user/create")) {
                String reqBody = IOUtils.readData(br, contentLength);
                Map<String, String> req = HttpRequestUtils.parseQueryString(reqBody);
                User user = new User(req.get("userId"), req.get("password"), req.get("name"), req.get("email"));
                log.info("user : {}", user);

                DataBase.addUser(user);
                url = "/index.html";

                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, url);
            } else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(Paths.get("./webapp" + url));
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String locationUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + locationUrl + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
