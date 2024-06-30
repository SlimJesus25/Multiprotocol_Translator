package eu.arrowhead.application.skeleton.consumer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConsumerNotFoundAdvice {
    @ExceptionHandler(ConsumerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String consumerNotFoundHandler(ConsumerNotFoundException ex){
        return ex.getMessage();
    }
}
