package com.CS516DE.service;



import com.CS516DE.domain.Order;
import com.CS516DE.domain.OrderItem;
import com.CS516DE.utility.Utility;
import com.CS516DE.vo.Account;
import com.CS516DE.vo.Availability;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;



import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;


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
        String orderId = requestEvent.getPathParameters().get("orderId");
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
        String orderId = apiGatewayRequest.getPathParameters().get("orderId");
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
    public APIGatewayProxyResponseEvent placeOrder(APIGatewayProxyRequestEvent requestEvent, Context context) throws IOException, InterruptedException {
        Order order = Utility.convertStringToObj(requestEvent.getBody(), context);
        List<OrderItem> orderItems = order.getOrderItems();


        String requestBody = Utility.convertListOfObjToString(orderItems, context);
        context.getLogger().log("calling the product-service");
        String apiUrl = "https://keiz0ctap5.execute-api.us-east-2.amazonaws.com/dev/products";
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String availabilityJson = httpResponse.body();
        Availability availability = new ObjectMapper().readValue(availabilityJson, Availability.class);
        context.getLogger().log(availabilityJson);

        boolean isPaid = true;
        //jsonBody = Utility.convertObjToString(new Availability(false), context);
        //return createAPIResponse(jsonBody, 200, Utility.createHeaders());

        if(availability.getIsAvailable()) {

            if (isPaid) {
                initDynamoDB();
                Order orderedPlaced = Utility.convertStringToObj(requestEvent.getBody(), context);
                dynamoDBMapper.save(orderedPlaced);
                String orderedId = orderedPlaced.getOrderId();

                Order savedOrder = dynamoDBMapper.load(Order.class, orderedId);
                jsonBody = Utility.convertObjToString(savedOrder, context);
                String orderIdValidation = savedOrder.getOrderId();
                if (orderedId.equals(orderIdValidation)) {
                    context.getLogger().log("order has been placed");
                    return createAPIResponse(jsonBody, 200, Utility.createHeaders());

                } else {
                    return createAPIResponse("{order not placed Please select a payment type}", 400, Utility.createHeaders());
                }

            } else {
                throw new IllegalArgumentException("Payment denied. Please check payment info!");
            }

        }
        else {
            throw new IllegalArgumentException("Product is not in-stock. Please try again later");
        }

    }

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

}
