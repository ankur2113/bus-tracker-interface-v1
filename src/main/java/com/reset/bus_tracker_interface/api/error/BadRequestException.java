package com.reset.bus_tracker_interface.api.error;

public class BadRequestException extends RuntimeException{

    public BadRequestException(String message){
        super(message);
    }
}
