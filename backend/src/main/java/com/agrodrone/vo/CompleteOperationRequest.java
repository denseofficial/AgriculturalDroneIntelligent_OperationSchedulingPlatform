package com.agrodrone.vo;

import java.math.BigDecimal;

public class CompleteOperationRequest {
    private BigDecimal actualAreaMu;
    private String result;
    private String exceptionRemark;

    public BigDecimal getActualAreaMu() {
        return actualAreaMu;
    }

    public void setActualAreaMu(BigDecimal actualAreaMu) {
        this.actualAreaMu = actualAreaMu;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getExceptionRemark() {
        return exceptionRemark;
    }

    public void setExceptionRemark(String exceptionRemark) {
        this.exceptionRemark = exceptionRemark;
    }
}
