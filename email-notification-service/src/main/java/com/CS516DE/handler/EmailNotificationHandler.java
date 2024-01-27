package com.CS516DE.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

public class EmailNotificationHandler implements RequestHandler<APIGatewayProxyRequestEvent, String> {

    private final AmazonSNS snsClient = AmazonSNSClientBuilder.standard().build();

    @Override
    public String handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            // Extract necessary information from the event (if needed)
            // For example:
            // String email = (String) event.get("email");
            //String message = (String) event.get("message");

            // Compose your email message
            String emailMessage = "A user clicked on the email icon. Message: [message]";

            // Publish a message to your SNS topic
            PublishRequest publishRequest = new PublishRequest()
                    .withMessage(emailMessage)
                    .withSubject("Portfolio Notification")
                    .withTopicArn("arn:aws:sns:us-east-2:915444981694:portfolio-email");

            PublishResult publishResult = snsClient.publish(publishRequest);
            // Optionally, you can retrieve message ID or other information from publishResult

            return "Message published to SNS";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error publishing message to SNS";
        }
    }
}
