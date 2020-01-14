package com.sea.common.exception;

import com.sea.common.entity.ResultCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ResultCode resultCode = ResultCode.SERVER_ERROR;

    public CommonException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
}
