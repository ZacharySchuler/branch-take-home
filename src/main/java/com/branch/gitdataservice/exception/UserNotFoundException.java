package com.branch.gitdataservice.exception;

//Used to indicate when a user has requested data for a user that does not exist
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super();
    }
}
