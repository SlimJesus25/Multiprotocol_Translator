package eu.arrowhead.application.skeleton.consumer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InvalidQoSAdvice {
    @ExceptionHandler(InvalidQoSException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String invalidQoS(InvalidQoSException ex){
        return ex.getMessage();
    }
}
