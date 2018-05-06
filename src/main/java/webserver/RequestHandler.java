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
import java.util.Collection;
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
            boolean isLogin = false;
            while (!"".equals(line)) {
                //log.info("header : {}", line);
                line = br.readLine();
                if (line.contains("Content-Length")) {
                    HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
                    contentLength = Integer.parseInt(pair.getValue());
                } else if (line.contains("Cookie")) {
                    HttpRequestUtils.Pair cookieHeaders = HttpRequestUtils.parseHeader(line);
                    Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieHeaders.getValue());
                    isLogin = Boolean.parseBoolean(cookies.get("logined"));
                }
            }

            if ("/user/create".equals(url)) {
                String reqBody = IOUtils.readData(br, contentLength);
                Map<String, String> req = HttpRequestUtils.parseQueryString(reqBody);
                User user = new User(req.get("userId"), req.get("password"), req.get("name"), req.get("email"));
                log.info("user : {}", user);

                DataBase.addUser(user);
                url = "/index.html";

                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, url);
            } else if ("/user/login".equals(url)) {
                String reqBody = IOUtils.readData(br, contentLength);
                Map<String, String> req = HttpRequestUtils.parseQueryString(reqBody);
                User loginUser = DataBase.findUserById(req.get("userId"));

                if (loginUser == null) {
                    responseResource(out, "/user/login_failed.html");
                    return;
                }

                if (loginUser.getPassword().equals(req.get("password"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302LoginSuccessHandler(dos);
                } else {
                    responseResource(out, "/user/login_failed.html");
                }
            } else if ("/user/list".equals(url)) {
                if (isLogin) {
                    Collection<User> users = DataBase.findAll();
                    StringBuilder sb = new StringBuilder();
                    sb.append("<table border='1'>");
                    for (User user : users) {
                        sb.append("<tr>");
                        sb.append("<td>" + user.getUserId() + "</td>");
                        sb.append("<td>" + user.getName() + "</td>");
                        sb.append("<td>" + user.getEmail() + "</td>");
                        sb.append("<tr>");
                    }
                    sb.append("</table>");
                    byte[] body = sb.toString().getBytes();
                    DataOutputStream dos = new DataOutputStream(out);
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                } else {
                    responseResource(out, "/user/login.html");
                }
            } else if (url.endsWith(".css")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(Paths.get("./webapp" + url));
                response200CssHeader(dos, body.length);
                responseBody(dos, body);
            } else {
                responseResource(out, url);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
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

    private void response302LoginSuccessHandler(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
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

    private void responseResource(OutputStream out, String url) {
        try {
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(Paths.get("./webapp" + url));
            response200Header(dos, body.length);
            responseBody(dos, body);
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
