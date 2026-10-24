package com.stms.notification.grpc;

import com.stms.grpc.NotificationServiceGrpc;
import com.stms.grpc.ReminderRequest;
import com.stms.grpc.ReminderResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/** gRPC server implementation of the NotificationService defined in notification.proto. */
@Slf4j
@GrpcService
public class NotificationGrpcService extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${notification.mail.from}")
    private String from;

    public NotificationGrpcService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendReminder(ReminderRequest request, StreamObserver<ReminderResponse> responseObserver) {
        log.info("REMINDER -> {} <{}>: task '{}' (project '{}') is due {}",
                request.getRecipientName(), request.getRecipientEmail(),
                request.getTaskTitle(), request.getProjectName(), request.getDueDate());

        boolean delivered = true;
        String message = "Reminder logged (no SMTP configured)";

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender != null) {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(from);
                mail.setTo(request.getRecipientEmail());
                mail.setSubject("Reminder: " + request.getTaskTitle());
                mail.setText("Hi " + request.getRecipientName() + ",\n\nYour task \""
                        + request.getTaskTitle() + "\" in project \"" + request.getProjectName()
                        + "\" is due on " + request.getDueDate() + ".\n\n- Smart Task Manager");
                sender.send(mail);
                message = "Email sent";
            } catch (Exception e) {
                log.warn("Could not send email: {}", e.getMessage());
                delivered = false;
                message = "Email failed: " + e.getMessage();
            }
        }

        responseObserver.onNext(ReminderResponse.newBuilder().setDelivered(delivered).setMessage(message).build());
        responseObserver.onCompleted();
    }
}
