package com.horizon.browser.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

import java.io.IOException;

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
    }



}
