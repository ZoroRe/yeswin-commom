package com.yeswin.common.core.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeswin.common.core.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;

@Slf4j
public class AbstractWebLogAspect {
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${api-alert.slow-limit:5000}")
    private Integer slowLimit;

    /**
     * 切入点
     * 子类可以重写
     */
    @Pointcut(
            "@target(org.springframework.web.bind.annotation.RestController) && " +
                    "(@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.GetMapping) ||" +
                    "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PostMapping))"
    )
    protected void webLog() {

    }

    /**
     * 得到当前请求需要记录的信息
     *
     * @param request
     * @return
     */
    protected String getHttpServletRequestLogInfoStr(HttpServletRequest request) {
        // 记录下请求内容
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("URL: %s, ", RequestUtils.getRequestUrl()));
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            StringBuilder headStr = new StringBuilder();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String header = request.getHeader(name);
            }
            stringBuilder.append(String.format("header: [%s]", headStr));
        }
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null && !parameterMap.isEmpty()) {
            StringBuilder strBuilder = new StringBuilder();
            parameterMap.forEach((key, value) -> {
                try {
                    strBuilder.append(key).append("=").append(objectMapper.writeValueAsString(value)).append(";");
                } catch (JsonProcessingException e) {
                    //忽略异常
                }
            });
            if (strBuilder.length() > 0) {
                stringBuilder.append("请求参数:----").append(strBuilder.toString());
            }
        }
        //这里得到body里面的数据
        if (request instanceof MultipartHttpServletRequest) {
            stringBuilder.append(",可能存在文件上传:");
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> multiFileMap = multipartHttpServletRequest.getMultiFileMap();
            stringBuilder.append("[");
            multiFileMap.forEach((k, v) -> {
                stringBuilder.append("文件名:" + k).append(",文件大小:").append(v.size()).append(";");
            });
            stringBuilder.append("]");
        }
        return stringBuilder.toString();
    }

    /**
     * 得到当前代理方法 需要记录的信息
     * 方便用于子类重写
     *
     * @param joinPoint
     * @return
     */
    protected String getJoinPointLogInfoStr(JoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            String name = method.getName();
            String[] parameterNames = methodSignature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            StringBuilder stringBuilder = new StringBuilder("执行的方法名:" + name);
            stringBuilder.append(" [");
            if (parameterNames != null && parameterNames.length > 0) {
                //开始遍历拼接
                for (int i = 0; i < parameterNames.length; i++) {
                    String parameterName = parameterNames[i];
                    stringBuilder.append(parameterName).append("=");
                    try {
                        if (args[i] != null) {
                            stringBuilder.append(objectMapper.writeValueAsString(args[i]));
                        } else {
                            stringBuilder.append("null");
                        }
                    } catch (JsonProcessingException e) {
                        //忽略异常
                        stringBuilder.append("notConvertJsonString");
                    }
                    stringBuilder.append(";");
                }
            }
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
        return "";
    }

    private String getRequestLogFullInfo(JoinPoint joinPoint) {

        StringBuilder stringBuilder = new StringBuilder();
        // 接收到请求，记录请求内容
        String requestLogInfoStr = getHttpServletRequestLogInfoStr(RequestUtils.getHttpServletRequest());
        if (StringUtils.isNotEmpty(requestLogInfoStr)) {
            stringBuilder.append(requestLogInfoStr).append(",");
        }

        String signatureLogInfoStr = getJoinPointLogInfoStr(joinPoint);
        if (StringUtils.isNotEmpty(signatureLogInfoStr)) {
            stringBuilder.append(signatureLogInfoStr);
        }
        return stringBuilder.toString();
    }

    /**
     * 环绕通知
     *
     * @param proceedingJoinPoint
     */
    @Around(value = "webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        StringBuilder stringBuilder = new StringBuilder(getRequestLogFullInfo(proceedingJoinPoint));

        long startTime = System.currentTimeMillis();
        //执行目标方法
        Object proceed = proceedingJoinPoint.proceed();
        long costTime = System.currentTimeMillis() - startTime;
        String returnType = null;
        String returnVal = "null";
        if (proceed != null) {
            returnType = proceed.getClass().getSimpleName();
            returnVal = objectMapper.writeValueAsString(proceed);
//            returnVal = disposeResponse(returnVal);
        }

        stringBuilder.append("==============>执行完成! ")
                .append(",返回值类型: ").append(returnType)
                .append(",返回值: ").append(returnVal)
                .append(",执行时间: ").append(costTime).append("ms");
        log.info(stringBuilder.toString());

        if (costTime >= slowLimit ) {
            String signatureLogInfoStr = getJoinPointLogInfoStr(proceedingJoinPoint);
            String title = "接口返回超过" + slowLimit + " ms" ;
            String msg = "返回时间超过 " + slowLimit + " ms 的 URL: [" + RequestUtils.getRequestUrl() + "] " + signatureLogInfoStr + ", costTime: " + costTime + "ms";
        }

        return proceed;
    }

    private String disposeResponse(String returnVal) {
        if (StringUtils.isEmpty(returnVal) || returnVal.length() <= 1024) {
            return returnVal;
        }
        return returnVal.substring(0, 1024);
    }

    /**
     * 异常通知 参数 Throwable ex 一定要在配置中指出. throwing指出参数名
     * 如果产生异常 此通知是最后执行的
     *
     * @param joinPoint
     * @param ex
     */
    @AfterThrowing(pointcut = "webLog()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("执行出异常! ");

        String signatureLogInfoStr = getJoinPointLogInfoStr(joinPoint);
        if (StringUtils.isNotEmpty(signatureLogInfoStr)) {
            stringBuilder.append(signatureLogInfoStr);
        }
        stringBuilder.append("URL:").append(RequestUtils.getRequestUrl())
                .append(",异常类型: ").append(ex.getClass().getSimpleName())
                .append(",异常消息: ").append(ex.getMessage());
        log.warn(stringBuilder.toString());
    }

}
