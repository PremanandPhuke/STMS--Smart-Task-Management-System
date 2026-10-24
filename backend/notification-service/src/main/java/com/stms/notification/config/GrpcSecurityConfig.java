package com.stms.notification.config;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Rejects every gRPC call that does not carry the correct "x-api-key" header.
 * This is a simple service-to-service authentication mechanism.
 */
@Configuration
public class GrpcSecurityConfig {

    private static final Metadata.Key<String> API_KEY_HEADER =
            Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);

    @Bean
    @GrpcGlobalServerInterceptor
    ServerInterceptor apiKeyServerInterceptor(@Value("${notification.api-key}") String expectedKey) {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                String provided = headers.get(API_KEY_HEADER);
                boolean ok = provided != null && MessageDigest.isEqual(
                        expectedKey.getBytes(StandardCharsets.UTF_8),
                        provided.getBytes(StandardCharsets.UTF_8));
                if (ok) {
                    return next.startCall(call, headers);
                }
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid API key"), new Metadata());
                return new ServerCall.Listener<>() {
                };
            }
        };
    }
}
