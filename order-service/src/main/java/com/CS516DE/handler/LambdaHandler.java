package com.CS516DE.handler;


import com.CS516DE.service.OrderService;
import com.CS516DE.service.OrderServiceImpl;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.io.IOException;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    @Override
    public APIGatewayProxyResponseEvent  handleRequest(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
        OrderService orderService = new OrderServiceImpl();

        switch (apiGatewayRequest.getHttpMethod()) {

            case "POST":
                try {
                    return orderService.placeOrder(apiGatewayRequest, context);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

            case "GET":
                if (apiGatewayRequest.getPathParameters() != null) {
                    return orderService.getOrderById(apiGatewayRequest, context);
                }
                return orderService.getAllOrders(apiGatewayRequest, context);
            case "DELETE":
                if (apiGatewayRequest.getPathParameters() != null) {
                    return orderService.deleteOrderById(apiGatewayRequest, context);
                }
            default:
                throw new Error("Unsupported Methods:::" + apiGatewayRequest.getHttpMethod());

        }
    }
 }