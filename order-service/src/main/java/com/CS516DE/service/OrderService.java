package com.CS516DE.service;


import com.CS516DE.domain.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.List;

public interface OrderService {
    public APIGatewayProxyResponseEvent getAllOrders(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
    public APIGatewayProxyResponseEvent getOrderById(APIGatewayProxyRequestEvent requestEvent, Context context);
    public APIGatewayProxyResponseEvent placeOrder(APIGatewayProxyRequestEvent requestEvent, Context context) throws IOException;
    public APIGatewayProxyResponseEvent deleteOrderById(APIGatewayProxyRequestEvent requestEvent, Context context);
}
