package io.juneqqq.core.exception;


import io.juneqqq.pojo.dao.entity.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class CommonGlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public R<Void> conditionExceptionHandler(BusinessException e){
        return R.fail(e.getErrorCodeEnum());
    }
    @ExceptionHandler(value = Exception.class)
    public R<Void> unknowException(Exception e){
        e.printStackTrace();
        log.error("捕捉到未知异常："+e.getMessage());
        return R.fail(ErrorCodeEnum.SYSTEM_ERROR);
    }
}
