package com.cts.mc.s3;

import static com.cts.mc.config.AwsClientConfiguration.s3Client;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.cts.mc.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * @author bharatkumar
 *
 */
public class S3UploadService {

    private S3UploadService() {
        // Utility classes should not have public constructors (squid:S1118)
    }

    private static Logger log = LoggerFactory.getLogger(S3UploadService.class);
    private static final String S3_USER_BUCKET = "aws-user-registration";
    private static final String S3_USER_FILE_KEY = "user.json";
    private static final String TEMP_FILE_PATH = "/tmp/user.json";
    private static Gson gson = new Gson();

    public static boolean uploadToS3(User user) {
        boolean isOk = false;
        try {
            // Get the existing object from Bucket.
            S3Object s3Object = s3Client().getObject(S3_USER_BUCKET, S3_USER_FILE_KEY);

            // parse the s3Object.
            List<User> dataToModify = readS3Object(s3Object);

            if (dataToModify.isEmpty())
                throw new AmazonServiceException("Data not present in S3 Bucket");

            // Add the new user to List.
            dataToModify.add(user);

            File file = writeToTempFile(dataToModify, new File(TEMP_FILE_PATH));

            // Delete the object as we will append with new data. AWS S3 doesn't support
            // appending.
            s3Client().deleteObject(S3_USER_BUCKET, S3_USER_FILE_KEY);

            // Finally upload the modified file back to S3.
            log.info("Uploading the modified Json file to S3 with bucketname [{}] and key [{}]", S3_USER_BUCKET, S3_USER_FILE_KEY);
            s3Client().putObject(S3_USER_BUCKET, S3_USER_FILE_KEY, file);

            log.info("File Successfully Uploaded [{}]", file);
            isOk = true;
        } catch (AmazonServiceException e) {
            log.error("Unable to either parse the user.json or its not available");
        } catch (Exception e) {
            log.error("Unexpected Exception occurred. Check the previous logs for more info.");
        }
        return isOk;

    }

    private static List<User> readS3Object(S3Object s3Object) {
        try {
            return gson.fromJson(new InputStreamReader(s3Object.getObjectContent(), UTF_8), List.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            log.error("Unable to parse the S3 Object");
        }
        return Collections.emptyList();
    }

    private static File writeToTempFile(List<User> userList, File tempFile) {
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            gson.toJson(userList, fileWriter);
        } catch (IOException | JsonIOException e) {
            log.error("Unable to create temporary File");
        }
        return tempFile;
    }

}
