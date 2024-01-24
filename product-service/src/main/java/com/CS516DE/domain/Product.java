package com.CS516DE.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

@DynamoDBTable(tableName = "product")
public class Product {

    @DynamoDBHashKey(attributeName = "productId")
    private String productId;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBAttribute(attributeName = "vendor")
    private String vendor;

    @DynamoDBAttribute(attributeName = "category")
    private String category;

    @DynamoDBAttribute(attributeName = "quantity")
    private int quantity;

    @DynamoDBAttribute(attributeName = "price")
    private double price;



}
