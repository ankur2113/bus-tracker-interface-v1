package com.reset.bus_tracker_interface.auth;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(){
        super("Invalid Username or Password.");
    }
}
