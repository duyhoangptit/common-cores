package com.tiger.cores.aops;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAspect {

    public static final String ROOT = "root";
    public static final String TARGET = "target";

    protected String[] getMethodNames(JoinPoint joinPoint) {
        return new String[] {
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName()
        };
    }

    protected StandardEvaluationContext getEvaluationContext(final JoinPoint joinPoint, Map<String, Object> variables) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        context.setVariable(ROOT, joinPoint.getTarget().getClass());

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }

        MethodSignature sign = (MethodSignature) joinPoint.getSignature();

        String[] parameterNames = sign.getParameterNames();
        Object[] args = joinPoint.getArgs();

        context.setVariable(TARGET, joinPoint.getTarget());

        if (Objects.nonNull(parameterNames)) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (i < args.length) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }
        }

        return context;
    }

    protected Map<String, Object> getExtraVariable() {
        return new HashMap<>();
    }

    protected Object getValueByExpressionFromRequest(ProceedingJoinPoint joinPoint, String keyExpression) {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();

        // Đặt các tham số của phương thức vào EvaluationContext theo tên biến
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        // Đánh giá SpEL expression
        var valueObject = parser.parseExpression(keyExpression).getValue(context);

        return valueObject == null ? null : valueObject;
    }

    protected Object getValueByExpressionFromResponse(Object response, String keyExpression) {
        StandardEvaluationContext context = new StandardEvaluationContext(response);
        ExpressionParser parser = new SpelExpressionParser();

        context.setVariable("result", response);

        // Đánh giá SpEL expression
        var valueObject = parser.parseExpression(keyExpression).getValue(context);

        return valueObject == null ? null : valueObject;
    }
}
