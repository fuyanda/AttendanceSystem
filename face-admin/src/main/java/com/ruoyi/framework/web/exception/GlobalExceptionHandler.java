package com.ruoyi.framework.web.exception;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.DemoModeException;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.filter.RepeatedlyRequestWrapper;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.EscapeUtil;
import com.ruoyi.common.utils.http.HttpHelper;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.face.util.FaceLogSupport;

/**
 * 全局异常处理器
 * 
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 权限校验异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',权限校验失败'{}',参数'{}'", requestURI, e.getMessage(), resolveRequestData(request));
        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
            HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求,参数'{}'", requestURI, e.getMethod(), resolveRequestData(request));
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String detail = e.getDetailMessage();
        if (StringUtils.isNotEmpty(detail))
        {
            log.error("请求地址'{}',请求方式'{}',业务异常'{}',明细'{}',参数'{}'",
                    requestURI, request.getMethod(), e.getMessage(), detail, resolveRequestData(request), e);
        }
        else
        {
            log.error("请求地址'{}',请求方式'{}',业务异常'{}',参数'{}'",
                    requestURI, request.getMethod(), e.getMessage(), resolveRequestData(request), e);
        }
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常. 参数'{}'", requestURI, resolveRequestData(request), e);
        return AjaxResult.error(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String value = Convert.toStr(e.getValue());
        if (StringUtils.isNotEmpty(value))
        {
            value = EscapeUtil.clean(value);
        }
        log.error("请求参数类型不匹配'{}',发生系统异常. name={}, requiredType={}, value={}, 参数'{}'",
                requestURI, e.getName(),
                e.getRequiredType() == null ? null : e.getRequiredType().getName(),
                value, resolveRequestData(request), e);
        return AjaxResult.error(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), value));
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',请求方式'{}',发生未知异常. 参数'{}'",
                requestURI, request.getMethod(), resolveRequestData(request), e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',请求方式'{}',发生系统异常. 参数'{}'",
                requestURI, request.getMethod(), resolveRequestData(request), e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e, HttpServletRequest request)
    {
        log.error("请求地址'{}',参数校验失败'{}',参数'{}'",
                request.getRequestURI(), e.getMessage(), resolveRequestData(request), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request)
    {
        log.error("请求地址'{}',参数校验失败'{}',参数'{}'",
                request.getRequestURI(), e.getMessage(), resolveRequestData(request), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 演示模式异常
     */
    @ExceptionHandler(DemoModeException.class)
    public AjaxResult handleDemoModeException(DemoModeException e)
    {
        return AjaxResult.error("演示模式，不允许操作");
    }

    /**
     * 读取并脱敏请求参数（query / form / JSON body），大图与密钥只保留摘要。
     */
    private String resolveRequestData(HttpServletRequest request)
    {
        try
        {
            Map<String, String> paramMap = ServletUtils.getParamMap(request);
            String queryPart = FaceLogSupport.sanitizeMap(paramMap);
            String bodyPart = "";
            String contentType = request.getContentType();
            if (request instanceof RepeatedlyRequestWrapper
                    && contentType != null
                    && contentType.toLowerCase().contains("application/json"))
            {
                bodyPart = FaceLogSupport.sanitizePayload(HttpHelper.getBodyString(request));
            }
            if (StringUtils.isNotEmpty(bodyPart) && StringUtils.isNotEmpty(queryPart))
            {
                return "query=" + queryPart + ", body=" + bodyPart;
            }
            if (StringUtils.isNotEmpty(bodyPart))
            {
                return bodyPart;
            }
            if (StringUtils.isNotEmpty(queryPart))
            {
                return queryPart;
            }
            return "";
        }
        catch (Exception ex)
        {
            return "resolveRequestDataFailed:" + ex.getMessage();
        }
    }
}
