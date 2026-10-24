package com.stms.task.grpc;

import com.stms.grpc.NotificationServiceGrpc;
import com.stms.grpc.ReminderRequest;
import com.stms.grpc.ReminderResponse;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/** Thin wrapper around the generated gRPC stub. Never throws: a failed reminder must not break the scheduler. */
@Slf4j
@Service
public class NotificationClient {

    @GrpcClient("notification-service")
    private NotificationServiceGrpc.NotificationServiceBlockingStub stub;

    public boolean sendReminder(String email, String name, String taskTitle, String projectName, String dueDate) {
        try {
            ReminderResponse response = stub.withDeadlineAfter(5, TimeUnit.SECONDS).sendReminder(
                    ReminderRequest.newBuilder()
                            .setRecipientEmail(email)
                            .setRecipientName(name)
                            .setTaskTitle(taskTitle)
                            .setProjectName(projectName)
                            .setDueDate(dueDate)
                            .build());
            return response.getDelivered();
        } catch (StatusRuntimeException e) {
            log.warn("notification-service unavailable or rejected the call: {}", e.getStatus());
            return false;
        }
    }
}
