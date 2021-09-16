package com.gant.trade.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class BusinessRuntimeExceptionHandler implements ProblemHandling {

    @Autowired
    private ExceptionUtil exceptionUtil;

    @ExceptionHandler({BusinessRuntimeException.class})
    public ResponseEntity<Problem> handleBusinessException(BusinessRuntimeException e, NativeWebRequest request) {
        Problem problem = buildProblem(exceptionUtil.getkeyPrefix(e), e);
        return this.create(e, problem, request);
    }

    private Problem buildProblem(String prefix, BusinessRuntimeException e) {
        ProblemBuilder problemBuilder = Problem.builder();
        problemBuilder.withTitle(exceptionUtil.getLocalizedCode(prefix));
        problemBuilder.withDetail(exceptionUtil.getLocalizedDetail(prefix, e.getParameters()));
        problemBuilder.withStatus(exceptionUtil.getCustomStatus(prefix, e.getStatus()));
        return problemBuilder.build();
    }
}
