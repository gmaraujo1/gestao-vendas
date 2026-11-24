package br.com.gestao.gestaovendas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessRecusedException extends RuntimeException {
    public AccessRecusedException(String message) {
        super(message);
    }
}
