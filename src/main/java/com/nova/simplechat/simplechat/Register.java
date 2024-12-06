package com.nova.simplechat.simplechat;

/**
 * Created by Raji Zakariyya
 * <p>
 * Message to Register the chat-server with the backend.
 */

    public class Register {
        public static final String ACTION = "register";
        private String name;
        private Header header;
        private Integer port;
        private String ip;

        public Register() {
        }

        public Register(String name, Integer port, String ip) {
            this.name = name;
            this.port = port;
            this.ip = ip;
            this.header = new Header(ACTION);
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIp(){return ip;}

        public void setIp(String ip){
            this.ip = ip;
        }

        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }
    }


