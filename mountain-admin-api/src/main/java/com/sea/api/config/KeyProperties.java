package com.sea.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties("encrypt")
public class KeyProperties {
    private String key;
    private String salt = "deadbeef";
    private boolean failOnError = true;
    private KeyProperties.KeyStore keyStore = new KeyProperties.KeyStore();

    public KeyProperties() {
    }

    public boolean isFailOnError() {
        return this.failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public KeyProperties.KeyStore getKeyStore() {
        return this.keyStore;
    }

    public void setKeyStore(KeyProperties.KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public static class KeyStore {
        private Resource location;
        private String password;
        private String alias;
        private String secret;

        public KeyStore() {
        }

        public String getAlias() {
            return this.alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public Resource getLocation() {
            return this.location;
        }

        public void setLocation(Resource location) {
            this.location = location;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSecret() {
            return this.secret == null ? this.password : this.secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}

