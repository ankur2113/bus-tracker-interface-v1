package com.reset.bus_tracker_interface.api.error;

public class ConflictException extends RuntimeException{

    public ConflictException(String message){
        super(message);
    }
}
