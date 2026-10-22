package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DemoMode {

    private final boolean readOnly;

    public DemoMode(@Value("${app.demo.read-only:false}") boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void requireWritable() {
        if (readOnly) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This demo is read-only");
        }
    }
}
