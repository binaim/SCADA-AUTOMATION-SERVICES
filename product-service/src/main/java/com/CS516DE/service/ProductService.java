package com.CS516DE.service;



import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.net.http.HttpResponse;

public interface ProductService {
    public APIGatewayProxyResponseEvent getAllProducts(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
    public APIGatewayProxyResponseEvent getProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
    public APIGatewayProxyResponseEvent saveProduct(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
    public APIGatewayProxyResponseEvent deleteProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
//    public Product reduceQuantityByProductId(Long id, int reduceAmount);
    public APIGatewayProxyResponseEvent addQuantityByProductId(APIGatewayProxyRequestEvent requestEvent, Context context);
    public APIGatewayProxyResponseEvent fulfillOrder(APIGatewayProxyRequestEvent requestEvent, Context context);
}
