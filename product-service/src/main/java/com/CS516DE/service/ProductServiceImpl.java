package com.CS516DE.service;


import com.CS516DE.domain.Product;
import com.CS516DE.utility.Utility;
import com.CS516DE.vo.Availability;
import com.CS516DE.vo.OrderItem;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;


import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductServiceImpl implements ProductService {

    private DynamoDBMapper dynamoDBMapper;
    private static  String jsonBody = null;

    @Override
    public APIGatewayProxyResponseEvent getProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        String productId = apiGatewayRequest.getPathParameters().get("productId");
        Product product =   dynamoDBMapper.load(Product.class,productId)  ;
        if(product!=null) {
            jsonBody = Utility.convertObjToString(product, context);
            context.getLogger().log("fetch product By ID:::" + jsonBody);
            return createAPIResponse(jsonBody,200,Utility.createHeaders());
        }else{
            jsonBody = "Product Not Found Exception :" + productId;
            return createAPIResponse(jsonBody,400,Utility.createHeaders());
        }

    }
    @Override
    public APIGatewayProxyResponseEvent getAllProducts(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        List<Product> products = dynamoDBMapper.scan(Product.class,new DynamoDBScanExpression());
        jsonBody =  Utility.convertListOfObjToString(products,context);
        context.getLogger().log("fetch product List:::" + jsonBody);
        return createAPIResponse(jsonBody,200,Utility.createHeaders());
    }


    @Override
    public APIGatewayProxyResponseEvent saveProduct(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        Product product = Utility.convertStringToObj(apiGatewayRequest.getBody(),context);
        dynamoDBMapper.save(product);
        jsonBody = Utility.convertObjToString(product,context) ;
        context.getLogger().log("data saved successfully to dynamodb:::" + jsonBody);
        return createAPIResponse(jsonBody,201,Utility.createHeaders());
    }

    @Override
    public APIGatewayProxyResponseEvent deleteProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        String productId = apiGatewayRequest.getPathParameters().get("productId");
        Product product =  dynamoDBMapper.load(Product.class,productId)  ;
        if(product!=null) {
            dynamoDBMapper.delete(product);
            context.getLogger().log("data deleted successfully :::" + productId);
            return createAPIResponse("data deleted successfully." + productId,200,Utility.createHeaders());
        }else{
            jsonBody = "product Not Found Exception :" + productId;
            return createAPIResponse(jsonBody,400,Utility.createHeaders());
        }
    }




    @Override
    public APIGatewayProxyResponseEvent addQuantityByProductId(APIGatewayProxyRequestEvent requestEvent, Context context) {
        initDynamoDB();
        String productId = requestEvent.getPathParameters().get("productId");
        String increaseAmount = requestEvent.getQueryStringParameters().get("quantity");
        Product product = dynamoDBMapper.load(Product.class, productId);
        int newQuantity = product.getQuantity() + Integer.getInteger(increaseAmount);
        product.setQuantity(newQuantity);
        dynamoDBMapper.save(product);
        context.getLogger().log("data updated successfully to dynamodb:::" + jsonBody);
        jsonBody = Utility.convertObjToString(product, context);
        return createAPIResponse(jsonBody, 201, Utility.createHeaders());
    }

    @Override
    public APIGatewayProxyResponseEvent fulfillOrder(APIGatewayProxyRequestEvent requestEvent, Context context) {
        initDynamoDB();
        context.getLogger().log("in the product fulfillOrder method");

        List<OrderItem> orderItems = Utility.convertStringToListObj(requestEvent.getBody(), context);
        List<String> productIds = orderItems.stream().map(p -> p.getProductId()).collect(Collectors.toList());
        //List<Product> orderProducts = dynamoDBMapper.load(Product.class, productIds);
        List<Product> orderProducts = productIds.stream().map(id -> dynamoDBMapper.load(Product.class,id)).collect(Collectors.toList());
        Map<Product, Integer> productAndQuantity = new HashMap<>();

        for (Product product : orderProducts) {
            context.getLogger().log("in the for loop");
            for (OrderItem orderItem : orderItems) {
                if (Objects.equals(orderItem.getProductId(), product.getProductId())) {
                    if (product.getQuantity() < orderItem.getQuantity()) {
                        jsonBody = Utility.convertObjToString(new Availability(false), context);
                        return createAPIResponse(jsonBody, 200, Utility.createHeaders());
                    } else {
                        productAndQuantity.put(product, orderItem.getQuantity());
                    }
                }
            }
        }
        productAndQuantity.forEach((p, q) -> {
            p.setQuantity(p.getQuantity() - q);
            if (p.getQuantity() < 50){

                context.getLogger().log("This product [{} {}] quantity is {}. Quantity is running low!" +
                        p.getProductId() + p.getName() + p.getQuantity());

            }
        });
        context.getLogger().log("product-service before returning to the order-service");


        jsonBody = Utility.convertObjToString(new Availability(true), context);
        context.getLogger().log(jsonBody);
        return createAPIResponse(jsonBody, 200, Utility.createHeaders());
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
