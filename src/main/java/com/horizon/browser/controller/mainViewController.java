package com.horizon.browser.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

import java.io.*;
import java.net.Socket;

public class mainViewController {
    public AnchorPane root;
    public WebView wbView;
    public TextField txtAddress;

    public void initialize() throws Exception {
        txtAddress.setText("http://ikman.lk/");
        loadWebPage(txtAddress.getText());
        txtAddress.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) Platform.runLater(txtAddress::selectAll);
        });
    }

    public void txtAddressOnAction(ActionEvent actionEvent) throws IOException {
        String url = txtAddress.getText();
        if (url.isBlank()) return;
        loadWebPage(url);
    }

    private void loadWebPage(String url) throws IOException {

        // protocol
        int protocolEndIndex = url.indexOf("://");
        String protocol = (protocolEndIndex != -1) ? url.substring(0, protocolEndIndex) : "http";

        // host
        int hostStartIndex = (protocolEndIndex != -1) ? protocolEndIndex + 3 : 0;
        int hostEndIndex = url.indexOf(':', hostStartIndex);
        if (hostEndIndex == -1) {
            hostEndIndex = url.indexOf('/', hostStartIndex);
            if (hostEndIndex == -1) {
                hostEndIndex = url.indexOf('?', hostStartIndex);
                if (hostEndIndex == -1) {
                    hostEndIndex = url.indexOf('#', hostStartIndex);
                }
            }
        }
        String host = url.substring(hostStartIndex, hostEndIndex == -1 ? url.length() : hostEndIndex);


        //port
        String port = "";
        if (hostEndIndex != -1 && url.charAt(hostEndIndex) == ':') {
            int portStart = hostEndIndex + 1;
            int portEndIndex = url.indexOf('/', portStart);
            if (portEndIndex == -1) {
                portEndIndex = url.indexOf('?', portStart);
                if (portEndIndex == -1) {
                    portEndIndex = url.indexOf('#', portStart);
                }
            }
            port = url.substring(portStart, portEndIndex == -1 ? url.length() : portEndIndex);
        }
        if ((protocol.equals("http") | protocol.equals("https")) && port.isBlank()) {
            if (protocol.equals("http")) {
                port = "80";
            }  else {
                port = "443";
            }

        } else if (((!protocol.equals("http") && (!protocol.equals("https")))   && port.isBlank())){
            System.out.println("Invalid url");
            return;
        }


        //path
        int pathStartIndex = url.indexOf('/', url.indexOf("://") + 3);
        int pathEndIndex = url.indexOf('?', pathStartIndex);
        if (pathEndIndex == -1) {
            pathEndIndex = url.indexOf('#', pathStartIndex);
        }
        if (pathEndIndex == -1) {
            pathEndIndex = url.length();
        }

        String path = (pathStartIndex != -1 && pathStartIndex < url.length())
                ? url.substring(pathStartIndex, pathEndIndex)
                : "/";


        System.out.println("protocol: " + protocol);
        System.out.println("host: " + host);
        System.out.println("port: " + port);
        System.out.println("path: " + path);
        System.out.println("==============");

        Socket socket = new Socket(host, Integer.parseInt(port));
        System.out.println("Connected to " + socket.getRemoteSocketAddress());
        new Thread(() -> {
            try {
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                String statusLine = br.readLine();
                System.out.println(statusLine);
                String[] s = statusLine.split(" ");
                int statusCode = Integer.parseInt(s[1]);
//                System.out.println("status code: " + statusCode);

                String line;
                String header;
                String value = null;
                String contentType = "";

                while ((line = br.readLine()) != null && !line.isBlank()) {
//                    System.out.println(line);
                    String[] split = line.split(":");
                    header = split[0].strip();
                    value = line.substring(header.length() + 1).strip();
//                    System.out.println("header: " + header);
//                    System.out.println("value: " + value);

                    if (statusCode >= 300 && statusCode <= 400) {
                        if (header.contains("Location")) {
                            value = value.strip();
                            loadWebPage(value);
                            break;
                        }
                    }

                    if (header.equalsIgnoreCase("Content-Type")) {
                        contentType = value.strip();
//                        System.out.println("contentType: " + contentType);

                    }
                }




            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        String httpProtocol = """
                    GET %s HTTP/1.1
                    Host: %s
                    User-Agent: Horizon-browser
                    Connection: close
                    Accept: text/html;
                    
                    """.formatted(path, host);

        OutputStream os = socket.getOutputStream();
        os.write(httpProtocol.getBytes());
        os.flush();

    }


}
