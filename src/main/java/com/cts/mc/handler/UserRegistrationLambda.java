package com.cts.mc.handler;

import static com.cts.mc.s3.S3UploadService.uploadToS3;
import static com.cts.mc.shared.util.UserRegistrationUtil.generatePermamentAccessCode;
import static com.cts.mc.sqs.SQSPublishService.publishToSQS;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.cts.mc.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author bharatkumar
 *
 */
public class UserRegistrationLambda implements RequestHandler<SNSEvent, String> {

    private static final String SUCCESSFUL = "User is registered Successfully and Email is sent.";

    private static Logger log = LoggerFactory.getLogger(UserRegistrationLambda.class);

    @Override
    public String handleRequest(SNSEvent request, Context context) {

        // Start the Registration
        log.info("Registration started : [{}]", LocalDateTime.now());
        String userDetails = request.getRecords().get(0).getSNS().getMessage();

        try {

            // Parse the message and add the Access Code
            User user = retrieveUser(userDetails);
            log.info("Registering User  : [{}]", user.getEmailId());
            user.setPermamentAccessCode(generatePermamentAccessCode());

            // Upload the new user to S3 Bucket
            log.info("Uploading User to S3 Bucket");
            if (!uploadToS3(user))
                log.error("Unable to uploadFile in S3 Bucket");

            // Process the message to SQS Queue
            publishToSQS(user);
            log.info("Successfully published the message");

        } catch (AmazonServiceException e) {
            log.error("Unable to process further due to sudden interruption");
        } catch (Exception e) {
            log.error("Exception Occurred while processing SNS Event : [{}] at [{}] with exception {}", userDetails, LocalDateTime.now(),
                    e.getMessage());
        }
        return SUCCESSFUL;
    }

    private User retrieveUser(String userDetails) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(userDetails, User.class);
        } catch (JsonSyntaxException e) {
            log.error("Unable to Parse String to User Object.");
            throw new AmazonServiceException("Unable to Retrieve User");
        }
    }

}
