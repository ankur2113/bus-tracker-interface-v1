package com.reset.bus_tracker_interface.api.error;

public class NotFoundException extends RuntimeException{

    public NotFoundException(String message){
        super(message);
    }
}
