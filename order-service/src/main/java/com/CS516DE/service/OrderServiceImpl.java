package com.CS516DE.service;



import com.CS516DE.domain.Order;
import com.CS516DE.domain.OrderItem;
import com.CS516DE.utility.Utility;
import com.CS516DE.vo.Account;
import com.CS516DE.vo.Availability;
import com.CS516DE.vo.Payment;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWS4UnsignedPayloadSigner;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.Region;


import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

public class OrderServiceImpl implements OrderService{
    private URL url = null;
    private DynamoDBMapper dynamoDBMapper;

    private static String jsonBody = null;
    @Override
    public APIGatewayProxyResponseEvent getAllOrders(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        List<Order> orders = dynamoDBMapper.scan(Order.class,new DynamoDBScanExpression());
        jsonBody =  Utility.convertListOfObjToString(orders, context);
        context.getLogger().log("fetch product List:::" + jsonBody);
        return createAPIResponse(jsonBody,200,Utility.createHeaders());
    }

    @Override
    public APIGatewayProxyResponseEvent getOrderById(APIGatewayProxyRequestEvent requestEvent, Context context) {

        initDynamoDB();
        String orderId = requestEvent.getPathParameters().get("productId");
        Order order =   dynamoDBMapper.load(Order.class, orderId)  ;
        if(order!=null) {
            jsonBody = Utility.convertObjToString(order, context);
            context.getLogger().log("fetch order By ID:::" + jsonBody);
            return createAPIResponse(jsonBody,200,Utility.createHeaders());
        }else{
            jsonBody = "Order Not Found Exception :" + orderId;
            return createAPIResponse(jsonBody,400,Utility.createHeaders());
        }
    }

    @Override
    public APIGatewayProxyResponseEvent deleteOrderById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        String orderId = apiGatewayRequest.getPathParameters().get("productId");
        Order order =  dynamoDBMapper.load(Order.class, orderId)  ;
        if(order!=null) {
            dynamoDBMapper.delete(order);
            context.getLogger().log("data deleted successfully :::" + orderId);
            return createAPIResponse("data deleted successfully." + orderId,200,Utility.createHeaders());
        }else{
            jsonBody = "product Not Found Exception :" + orderId;
            return createAPIResponse(jsonBody,400,Utility.createHeaders());
        }
    }

    @Override
    public APIGatewayProxyResponseEvent placeOrder(APIGatewayProxyRequestEvent requestEvent, Context context) throws IOException {
        Order order = Utility.convertStringToObj(requestEvent.getBody(), context);
        List<OrderItem> orderItems = order.getOrderItems();
//        Boolean isFulfilled = webClientBuilder.build().put()
//                .uri("http://PRODUCT-SERVICE/api/products")
//                .bodyValue(order.getOrderItems())
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .block();

        String requestBody = new ObjectMapper().writeValueAsString(orderItems);
        String apiUrl = "https://keiz0ctap5.execute-api.us-east-2.amazonaws.com/dev/products";
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        boolean isPaid = false;
        jsonBody = Utility.convertObjToString(new Availability(true), context);
        return createAPIResponse(jsonBody,200, Utility.createHeaders());
//
//        if (isFulfilled){
//            Account account = webClientBuilder.build().get()
//                    .uri("http://USER-SERVICE/api/users/" + order.getBuyerId() + "/account")
//                    .headers(h -> h.setBearerAuth("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJidXllciIsImlhdCI6MTY1NDQ5NzAwOCwiZXhwIjoxNjU0NTgzNDA4fQ.o2kQRsBBZoBx8Orq1CkR2lmygSl6uT_NLHjtx81A3exaVFqjXVpJeyoU2w5iW_fAQfyo59yDBvmXgIXZ8CLbtw"))
////                    .header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJidXllciIsImlhdCI6MTY1NDQ5NzAwOCwiZXhwIjoxNjU0NTgzNDA4fQ.o2kQRsBBZoBx8Orq1CkR2lmygSl6uT_NLHjtx81A3exaVFqjXVpJeyoU2w5iW_fAQfyo59yDBvmXgIXZ8CLbtw")
//                    .retrieve()
//                    .bodyToMono(Account.class)
//                    .block();
//
//            Payment payment = account.getPreferredPaymentMethod();
//
//            order.setPaymentId(payment.getId());
//            order.setShippingAddressId(account.getShippingAddress().getId());
//
//            String paymentType = payment.getPaymentType().getName().name();
////            Map<String, String> env = System.getenv();
////            env.get(paymentType);
//            String paymentServiceUrl = environment.getProperty(paymentType);
//            System.out.println("This is the payment type enum: " + paymentType);
//            System.out.println("This is the env variable: " + paymentServiceUrl);
//
//            isPaid = webClientBuilder.build().post()
//                    .uri(paymentServiceUrl)
//                    .bodyValue(payment)
//                    .retrieve()
//                    .bodyToMono(Boolean.class)
//                    .block();
//        }
//        else {
//            throw new IllegalArgumentException("Product is not in-stock. Please try again later.");
//        }
//
//        if (isPaid){
//            return orderRepository.save(order);
//        }
//        else {
//            throw new IllegalArgumentException("Payment denied. Please check payment info!");
//        }

    }

//    @Override
//    public void deleteOrderById(Long id) {
//        orderRepository.deleteById(id);
//    }

    private void initDynamoDB(){
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        dynamoDBMapper = new DynamoDBMapper(client);
    }

    private APIGatewayProxyResponseEvent createAPIResponse(String body, int statusCode, Map<String,String> headers ){
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setBody(body);
        responseEvent.setHeaders(headers);
        responseEvent.setStatusCode(statusCode);
        return responseEvent;
    }

//    private static <T> Availability callApi(String apiUrl, Class<?> responseClass) throws Exception {
//        AwsCredentialsProvider credentialsProvider =
//                StaticCredentialsProvider.create(AwsBasicCredentials
//                        .create("product-service-access-key", "your-secret-key"));
//
//
//        java.net.http.HttpClient httpClient = Htt
//        String bearerToken = "";
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(apiUrl))
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer " + bearerToken)
//                .GET()
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//        int responseCode = response.statusCode();
//        String responseBody = response.body();
//        Object responseObject = deserializeResponseBody(responseBody, responseClass);
//
//        return new ApiResponse(responseCode, responseBody, responseObject);
//    }
}
