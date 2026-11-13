package com.jamesoliveira.autocare.booking.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** Lets controller methods simply take a CurrentUser parameter. */
@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        String id = webRequest.getHeader("X-User-Id");
        String role = webRequest.getHeader("X-User-Role");
        String name = webRequest.getHeader("X-User-Name");
        if (id == null || role == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please log in first");
        }
        try {
            // The gateway URL-encodes the name so characters like é survive the HTTP header
            String decodedName = name == null ? "Customer" : URLDecoder.decode(name, StandardCharsets.UTF_8);
            return new CurrentUser(Long.parseLong(id), decodedName, role);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please log in first");
        }
    }
}
