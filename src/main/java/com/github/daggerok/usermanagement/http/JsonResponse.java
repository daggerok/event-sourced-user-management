package com.github.daggerok.usermanagement.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonResponse {

    @Getter
    private final ObjectMapper objectMapper;

    public Response.ResponseBuilder<Object> builder() {
        return Response.builder()
                       .status(Response.Status.OK)
                       .objectMapper(objectMapper);
    }
}
