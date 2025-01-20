// src/main/java/com/SmartRiceAgriculture/SmartRiceAgriculture/exception/LandNotFoundExceptionException.java
package com.SmartRiceAgriculture.SmartRiceAgriculture.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LandNotFoundException extends RuntimeException {
    public LandNotFoundException(String message) {
        super(message);
    }
}
