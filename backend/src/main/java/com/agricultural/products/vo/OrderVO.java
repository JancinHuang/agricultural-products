package com.agricultural.products.vo;

import com.agricultural.products.entity.Order;
import com.agricultural.products.entity.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Order {
    private List<OrderItem> items = new ArrayList<>();
}
