package com.juneqqq.entity.exception;

public class CustomException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private int code;

    public CustomException(int code, String name){
        super(name);
        this.code = code;
    }

    public CustomException(String name){
        super(name);
        code = 500;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
