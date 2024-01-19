package com.CS516DE.service;



import com.CS516DE.domain.Product;
import com.CS516DE.vo.OrderItem;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.List;

public interface ProductService {
    public APIGatewayProxyResponseEvent getAllProducts(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
    public APIGatewayProxyResponseEvent getProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
    public APIGatewayProxyResponseEvent saveProduct(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
    public APIGatewayProxyResponseEvent deleteProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context);
//    public Product reduceQuantityByProductId(Long id, int reduceAmount);
    public Product addQuantityByProductId(Long id, int increaseAmount);
    public boolean fulfillOrder(List<OrderItem> orderItems);
}
