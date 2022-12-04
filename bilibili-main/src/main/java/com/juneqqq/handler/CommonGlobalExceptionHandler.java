package com.juneqqq.handler;


import com.juneqqq.entity.dao.R;
import com.juneqqq.entity.exception.CustomException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    @ResponseBody
    public R<String> conditionExceptionHandler(HttpServletRequest request, Exception e){
        String errorMsg = e.getMessage();
        if(e instanceof CustomException){
            int errorCode = ((CustomException)e).getCode();
            return new R<>(errorCode, errorMsg);
        }else{
            return new R<>(500,errorMsg);
        }
    }


    @ExceptionHandler(value = Exception.class)
    public R<String> commonException(Exception e){
        e.printStackTrace();
        return R.fail(500,e.getMessage());
    }
}
