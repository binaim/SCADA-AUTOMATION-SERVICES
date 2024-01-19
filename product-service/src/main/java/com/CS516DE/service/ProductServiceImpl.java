package com.CS516DE.service;


import com.CS516DE.domain.Product;
import com.CS516DE.utility.Utility;
import com.CS516DE.vo.OrderItem;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lombok.extern.slf4j.Slf4j;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class ProductServiceImpl implements ProductService {

    private DynamoDBMapper dynamoDBMapper;
    private static  String jsonBody = null;

    @Override
    public APIGatewayProxyResponseEvent getProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        String productId = apiGatewayRequest.getPathParameters().get("productID");
        Product product =   dynamoDBMapper.load(Product.class,productId)  ;
        if(product!=null) {
            jsonBody = Utility.convertObjToString(product, context);
            context.getLogger().log("fetch employee By ID:::" + jsonBody);
            return createAPIResponse(jsonBody,200,Utility.createHeaders());
        }else{
            jsonBody = "Employee Not Found Exception :" + productId;
            return createAPIResponse(jsonBody,400,Utility.createHeaders());
        }

    }
    @Override
    public APIGatewayProxyResponseEvent getAllProducts(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        List<Product> employees = dynamoDBMapper.scan(Product.class,new DynamoDBScanExpression());
        jsonBody =  Utility.convertListOfObjToString(employees,context);
        context.getLogger().log("fetch employee List:::" + jsonBody);
        return createAPIResponse(jsonBody,200,Utility.createHeaders());
    }
//    @Override
//    public APIGatewayProxyResponseEvent saveProduct(Product product) {
//        productRepository.save(product);
//        log.info("Product {} is saved.", product.getId());
//        return product;
//    }

    @Override
    public APIGatewayProxyResponseEvent saveProduct(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        Product product = Utility.convertStringToObj(apiGatewayRequest.getBody(),context);
        dynamoDBMapper.save(product);
        jsonBody = Utility.convertObjToString(product,context) ;
        context.getLogger().log("data saved successfully to dynamodb:::" + jsonBody);
        return createAPIResponse(jsonBody,201,Utility.createHeaders());
    }
//    @Override
//    public void deleteProductById(Long id) {
//        productRepository.deleteById(id);
//    }

//    @Override
//    public Product reduceQuantityByProductId(Long id, int reduceAmount) {
//        Product product = productRepository.findById(id).get();
//        int newQuantity = product.getQuantity() - reduceAmount;
//        product.setQuantity(newQuantity);
//        return productRepository.save(product);
//    }
    @Override
    public APIGatewayProxyResponseEvent deleteProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context){
        initDynamoDB();
        String productId = apiGatewayRequest.getPathParameters().get("empId");
        Product product =  dynamoDBMapper.load(Product.class,productId)  ;
        if(product!=null) {
            dynamoDBMapper.delete(product);
            context.getLogger().log("data deleted successfully :::" + productId);
            return createAPIResponse("data deleted successfully." + productId,200,Utility.createHeaders());
        }else{
            jsonBody = "Employee Not Found Exception :" + productId;
            return createAPIResponse(jsonBody,400,Utility.createHeaders());
        }
    }

    @Override
    public Product addQuantityByProductId(Long id, int increaseAmount) {
        return null;
    }

    @Override
    public boolean fulfillOrder(List<OrderItem> orderItems) {
        return false;
    }

//    @Override
//    public Product addQuantityByProductId(Long id, int increaseAmount) {
//        Product product = productRepository.findById(id).get();
//        int newQuantity = product.getQuantity() + increaseAmount;
//        product.setQuantity(newQuantity);
//        return productRepository.save(product);
//    }

//    @Override
//    public boolean fulfillOrder(List<OrderItem> orderItems) {
//
//        List<Long> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
//        List<Product> orderProducts = productRepository.findByIdIn(productIds);
//
//        Map<Product, Integer> productAndQuantity = new HashMap<>();
//
//        for (Product product : orderProducts) {
//            for (OrderItem orderItem : orderItems) {
//                if (product.getId() == orderItem.getProductId()) {
//                    if (product.getQuantity() < orderItem.getQuantity()) {
//                        return false;
//                    } else {
//                        productAndQuantity.put(product, orderItem.getQuantity());
//                    }
//                }
//            }
//        }
//        productAndQuantity.forEach((p, q) -> {
//            p.setQuantity(p.getQuantity() - q);
//            if (p.getQuantity() < 50){
//                log.info("This product [{} {}] quantity is {}. Quantity is running low!",
//                        p.getId(), p.getName(), p.getQuantity());
//            }
//        });
//
//        return true;
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


}
