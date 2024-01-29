package com.CS516DE.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SnsException;

public class EmailNotificationHandler {

    private AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();

    public void handleRequest(APIGatewayProxyRequestEvent event, Context context) {
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

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error publishing message to SNS");
        }
    }

//    private void sentTextMessage(String first, String phone) {
//
//        //SnsClient snsClient = SnsClient.create()
//                SnsClient sns = SnsClient.builder()
//                .build();
//        String message = first +" happy one year anniversary. We are very happy that you have been working here for a year! ";
//
//        try {
//            PublishRequest request = new PublishRequest();
//                    request.setMessage("Email has been sent from portfolio");
//                    request.setPhoneNumber("5108699475");
//                    request.setTargetArn("arn:aws:sns:us-east-2:915444981694:portfolio-email");
//
//
//            snsClient.publish(request);
//        } catch (SnsException e) {
//            System.err.println(e.awsErrorDetails().errorMessage());
//            System.exit(1);
//        }
    //}
}
