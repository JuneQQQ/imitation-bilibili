package io.juneqqq.core.exception;


import io.juneqqq.dao.entity.R;
import io.juneqqq.core.exception.CustomException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class CommonGlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = CustomException.class)
    public R<String> conditionExceptionHandler(HttpServletRequest request, CustomException e){
        return new R<>(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(value = Exception.class)
    public R<String> commonException(Exception e){
        e.printStackTrace();
        return R.fail(500,e.getMessage());
    }
}
