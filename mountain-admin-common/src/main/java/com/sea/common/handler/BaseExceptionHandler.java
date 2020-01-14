package com.sea.common.handler;

import com.sea.common.entity.Result;
import com.sea.common.entity.ResultCode;
import com.sea.common.exception.CommonException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义的公共异常
 */
@ControllerAdvice
public class BaseExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result error(HttpServletRequest request, HttpServletResponse response, Exception e) {
        e.printStackTrace();
        if(e.getClass() == CommonException.class) {
            CommonException commonException = (CommonException) e;
            return new Result(commonException.getResultCode());
        } else if(e.getClass() == AccessDeniedException.class) {
            return new Result(ResultCode.UNAUTHORISE);
        }else {
            return new Result(ResultCode.SERVER_ERROR);
        }
    }
}
