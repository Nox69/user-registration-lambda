package com.cts.mc.sqs;

import static com.cts.mc.config.AwsClientConfiguration.sqsClient;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.cts.mc.model.User;

/**
 * @author bharatkumar
 *
 */
public class SQSPublishService {

    private SQSPublishService() {
        // Utility classes should not have public constructors (squid:S1118)
    }

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String FIRST_NAME_ATTRIBUTE = "firstName";
    private static final String LAST_NAME_ATTRIBUTE = "lastName";
    private static final String ATTRIBUTE_DATATYPE = "String";

    private static final String EMAIL_TYPE = "register";

    private static Logger log = LoggerFactory.getLogger(SQSPublishService.class);
    private static final String SQS_QUEUE_URL = "https://sqs.us-east-2.amazonaws.com/960560987724/order-processing-queue";

    public static void publishToSQS(User user) {

        log.info("Creating the message Request to be published to Queue.");
        SendMessageRequest messageRequest = new SendMessageRequest().withQueueUrl(SQS_QUEUE_URL).withMessageAttributes(fillMessageAttributes(user))
                .withDelaySeconds(5).withMessageBody(user.getPermamentAccessCode());

        // publish the message with SQS Client
        sqsClient().sendMessage(messageRequest);
    }

    private static Map<String, MessageAttributeValue> fillMessageAttributes(User user) {
        MessageAttributeValue emailAttrVal = new MessageAttributeValue().withStringValue(user.getEmailId()).withDataType(ATTRIBUTE_DATATYPE);
        MessageAttributeValue typeAttrVal = new MessageAttributeValue().withStringValue(EMAIL_TYPE).withDataType(ATTRIBUTE_DATATYPE);
        MessageAttributeValue firstNameAttrVal = new MessageAttributeValue().withStringValue(user.getFirstName()).withDataType(ATTRIBUTE_DATATYPE);
        MessageAttributeValue lastNameAttrVal = new MessageAttributeValue().withStringValue(user.getLastName()).withDataType(ATTRIBUTE_DATATYPE);

        Object[][] messageAttributesMap = new Object[][] { { EMAIL_ATTRIBUTE, emailAttrVal }, { TYPE_ATTRIBUTE, typeAttrVal },
                { FIRST_NAME_ATTRIBUTE, firstNameAttrVal }, { LAST_NAME_ATTRIBUTE, lastNameAttrVal } };

        return Stream.of(messageAttributesMap).collect(Collectors.toMap(data -> (String) data[0], data -> (MessageAttributeValue) data[1]));

    }

}
