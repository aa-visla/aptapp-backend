package com.creatix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StreamUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Controller for HTML pages.
 */
@RestController
@ControllerAdvice
class IndexController {

    private final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private ObjectMapper jsonMapper;

    @ApiOperation(value = "Download static file")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not found")})
    @RequestMapping(value = "/static/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> downloadNotificationPhoto(@PathVariable @NotEmpty String fileName) throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        final Path absolutePath = Paths.get("static", fileName);
        final String strAbsolutePath = absolutePath.toString();
        final byte[] fileData;
        try ( final InputStream templateResourceStream = classLoader.getResourceAsStream(strAbsolutePath) ) {
            if ( templateResourceStream == null ) {
                throw new IOException("Resource " + strAbsolutePath + " not found.");
            }

            fileData = IOUtils.toByteArray(templateResourceStream);
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(Files.probeContentType(absolutePath)));
        headers.setContentLength(fileData.length);

        return new HttpEntity<>(fileData, headers);
    }

    @RequestMapping(path = "appVersion", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpEntity<String> getAppVersion() throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        final String fileData;
        try ( final InputStream fileInputStream = classLoader.getResourceAsStream("version_number") ) {
            if ( fileInputStream == null ) {
                throw new IOException("Version file not found!");
            }
            fileData = StreamUtils.copyToString(fileInputStream, Charset.forName("utf-8"));
        }

        return new HttpEntity<>(fileData);
    }

    @RequestMapping(value = { "/app", "/app/" }, method = RequestMethod.GET)
    public RedirectView redirectApp() {
        return new RedirectView("/app/index.html");
    }

    @ExceptionHandler({Throwable.class, Exception.class})
    public void serverError(Exception ex, HttpServletResponse response) throws IOException {
        handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public void badCredentials(Exception ex, HttpServletResponse response) throws IOException {
        handleException(new BadCredentialsException("Incorrect Credentials", ex), HttpStatus.UNAUTHORIZED, response);
    }

    @ExceptionHandler({AuthenticationException.class, UsernameNotFoundException.class})
    public void unauthorized(Exception ex, HttpServletResponse response) throws IOException {
        handleException(ex, HttpStatus.UNAUTHORIZED, response);
    }

    @ExceptionHandler({SecurityException.class, AccessDeniedException.class})
    public void forbidden(Exception ex, HttpServletResponse response) throws IOException {
        handleException(ex, HttpStatus.FORBIDDEN, response);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public void notFound(Exception ex, HttpServletResponse response) throws IOException {
        handleException(ex, HttpStatus.NOT_FOUND, response);
    }

    @ExceptionHandler({NullPointerException.class, DataIntegrityViolationException.class, ConstraintViolationException.class, IllegalArgumentException.class, IllegalStateException.class})
    public void integrityViolation(Exception ex, HttpServletResponse response) throws IOException {
        handleException(ex, HttpStatus.UNPROCESSABLE_ENTITY, response);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public void validationExceptionHandling(Exception ex, HttpServletResponse response) throws IOException {
        MethodArgumentNotValidException validException = (MethodArgumentNotValidException) ex;
        List<ObjectError> errors = validException.getBindingResult().getAllErrors();
        final HashMap<String, HashMap<String, String>> validationErrorMap = new HashMap<>();
        errors.forEach(e -> {
            if (e instanceof FieldError) {
                FieldError fieldError = (FieldError)e;
                if ( ! validationErrorMap.containsKey(fieldError.getField())) {
                    validationErrorMap.put(fieldError.getField(), new HashMap<>());
                }
                validationErrorMap.get(fieldError.getField())
                        .put(
                                "code",
                                fieldError.getCodes()[fieldError.getCodes().length - 1]
                        );
                validationErrorMap.get(fieldError.getField())
                        .put(
                                "message",
                                fieldError.getDefaultMessage()
                        );
            }
        });
        final ValidationErrorMessage errorMessage = new ValidationErrorMessage();
        fillErrorMessage(ex, HttpStatus.UNPROCESSABLE_ENTITY, errorMessage);
        errorMessage.setValidationErrors(validationErrorMap);
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().print(jsonMapper.writeValueAsString(errorMessage));
    }

    private void handleException(Exception ex, HttpStatus status, HttpServletResponse resp) throws IOException {
        logger.error("Error processing request", ex);
        resp.setStatus(status.value());
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);

        final ErrorMessage errorMessage = new ErrorMessage();
        fillErrorMessage(ex, status, errorMessage);
        resp.getOutputStream().print(jsonMapper.writeValueAsString(errorMessage));
    }

    private ErrorMessage fillErrorMessage(Exception ex, HttpStatus status, ErrorMessage errorMessage) {
        final String exceptionMessage = StringUtils.trim(StringUtils.isEmpty(ex.getMessage()) ? status.getReasonPhrase() : ex.getMessage());
        errorMessage.setError(exceptionMessage);
        errorMessage.setException(ex.getClass().getCanonicalName());
        errorMessage.setMessage(exceptionMessage);
        errorMessage.setStatus(status.value());
        errorMessage.setTimestamp(System.currentTimeMillis());
        errorMessage.setPath(httpServletRequest.getServletPath());
        return errorMessage;
    }

    @Data
    private static class ErrorMessage {
        private long timestamp;
        private int status;
        private String error;
        private String exception;
        private String message;
        private String path;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class ValidationErrorMessage extends ErrorMessage {
        private HashMap<String, HashMap<String, String>> validationErrors;
    }
}
