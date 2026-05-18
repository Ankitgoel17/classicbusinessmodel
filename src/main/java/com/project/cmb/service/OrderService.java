package com.project.cmb.service;

import com.project.cmb.entity.Order;
import com.project.cmb.entity.OrderDetail;
import com.project.cmb.entity.Product;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.OrderDetailRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.repo.ProductRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final OrderDetailRepo orderDetailRepo;
    private final ProductRepo productRepo;
    private final PaymentRepo paymentRepo;

    public long getTotalOrders() {
        return orderRepo.count();
    }

    public long getTotalShipped() {
        return orderRepo.countByStatus("Shipped");
    }

    public long getTotalInProcess() {
        return orderRepo.countByStatus("In Process");
    }

    public long getTotalCancelled() {
        return orderRepo.countByStatus("Cancelled");
    }

    public BigDecimal getOrderTotal(Integer orderNumber) {

        return orderDetailRepo.findById_OrderNumber(orderNumber)
                .stream()
                .map(od ->
                        od.getPriceEach().multiply(
                                new BigDecimal(
                                        od.getQuantityOrdered()
                                )
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPaymentReceived(
            Integer orderNumber) {

        return paymentRepo.findByOrderNumber(orderNumber)
                .stream()
                .map(PaymentListView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPendingPayment(
            Integer orderNumber) {

        return getOrderTotal(orderNumber)
                .subtract(
                        getTotalPaymentReceived(orderNumber)
                );
    }

    @Transactional
    public Order createOrder(
            Order order,
            List<OrderDetail> orderDetails) {

        for (OrderDetail detail : orderDetails) {

            Product product =
                    productRepo.findById(
                                    detail.getId()
                                            .getProductCode()
                            )
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "Product not found: "
                                                    + detail.getId()
                                                    .getProductCode()
                                    )
                            );

            if (product.getQuantityInStock()
                    < detail.getQuantityOrdered()) {

                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getProductName()
                                + ". Available: "
                                + product.getQuantityInStock()
                                + ", Requested: "
                                + detail.getQuantityOrdered()
                );
            }
        }

        Order savedOrder =
                orderRepo.save(order);

        for (OrderDetail detail : orderDetails) {

            detail.getId().setOrderNumber(
                    savedOrder.getOrderNumber()
            );

            orderDetailRepo.save(detail);

            Product product =
                    productRepo.findById(
                                    detail.getId()
                                            .getProductCode()
                            )
                            .orElseThrow();

            product.setQuantityInStock(
                    (short)
                            (
                                    product.getQuantityInStock()
                                            - detail.getQuantityOrdered()
                            )
            );

            productRepo.save(product);
        }

        return savedOrder;
    }
}