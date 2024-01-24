package com.CS516DE.handler;

import com.CS516DE.service.ProductService;
import com.CS516DE.service.ProductServiceImpl;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    @Override
    public APIGatewayProxyResponseEvent  handleRequest(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
        ProductService productService = new ProductServiceImpl();

        switch (apiGatewayRequest.getHttpMethod()) {

            case "POST":
                return productService.saveProduct(apiGatewayRequest, context);

            case "GET":
                if (apiGatewayRequest.getPathParameters() != null) {
                    return productService.getProductById(apiGatewayRequest, context);
                }
                return productService.getAllProducts(apiGatewayRequest, context);
            case "DELETE":
                if (apiGatewayRequest.getPathParameters() != null) {
                    return productService.deleteProductById(apiGatewayRequest, context);
                }
            case "PUT":

                    context.getLogger().log("product-service has been invoked");

                    return productService.fulfillOrder(apiGatewayRequest, context);

            default:
                throw new Error("Unsupported Methods:::" + apiGatewayRequest.getHttpMethod());

        }
    }
 }