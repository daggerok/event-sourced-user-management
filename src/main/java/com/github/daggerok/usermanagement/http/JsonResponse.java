package com.github.daggerok.usermanagement.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class JsonResponse {

    private final ObjectMapper objectMapper;

    public Response.ResponseBuilder<Object> builder() {
        return Response.builder()
                       .status(Response.Status.OK)
                       .objectMapper(objectMapper);
    }
}
