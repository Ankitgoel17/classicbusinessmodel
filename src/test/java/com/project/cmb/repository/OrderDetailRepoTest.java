package com.project.cmb.repository;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.OrderDetail;
import com.project.cmb.entity.OrderDetailId;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.OrderDetailRepo;
import com.project.cmb.repo.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderDetailRepoTest {

    @Autowired
    private OrderDetailRepo orderDetailRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CustomerRepo customerRepo;

    private final OrderDetailId id1 =
            new OrderDetailId(90001, "S18_1749");

    private final OrderDetailId id2 =
            new OrderDetailId(90001, "S18_2248");

    private final OrderDetailId id3 =
            new OrderDetailId(90002, "S18_1749");

    @BeforeEach
    void setup() {

        Customer customer103 =
                customerRepo.findById(103)
                        .orElseThrow();

        Order o1 = new Order();
        o1.setOrderNumber(90001);
        o1.setOrderDate(LocalDate.of(2024,1,1));
        o1.setRequiredDate(LocalDate.of(2024,1,10));
        o1.setStatus("In Process");
        o1.setCustomer(customer103);

        orderRepo.save(o1);

        Order o2 = new Order();
        o2.setOrderNumber(90002);
        o2.setOrderDate(LocalDate.of(2024,2,1));
        o2.setRequiredDate(LocalDate.of(2024,2,10));
        o2.setStatus("In Process");
        o2.setCustomer(customer103);

        orderRepo.save(o2);

        OrderDetail od1 = new OrderDetail();
        od1.setId(id1);
        od1.setQuantityOrdered(30);
        od1.setPriceEach(
                new BigDecimal("136.00")
        );
        od1.setOrderLineNumber((short)1);

        orderDetailRepo.save(od1);

        OrderDetail od2 = new OrderDetail();
        od2.setId(id2);
        od2.setQuantityOrdered(50);
        od2.setPriceEach(
                new BigDecimal("55.09")
        );
        od2.setOrderLineNumber((short)2);

        orderDetailRepo.save(od2);

        OrderDetail od3 = new OrderDetail();
        od3.setId(id3);
        od3.setQuantityOrdered(20);
        od3.setPriceEach(
                new BigDecimal("136.00")
        );
        od3.setOrderLineNumber((short)1);

        orderDetailRepo.save(od3);
    }

    @Test
    void repo_findAll_shouldReturnOrderDetails() {

        assertThat(
                orderDetailRepo.findAll()
        ).isNotEmpty();
    }

    @Test
    void repo_findById_shouldReturnOrderDetail() {

        Optional<OrderDetail> result =
                orderDetailRepo.findById(id1);

        assertThat(result).isPresent();

        assertThat(
                result.get()
                        .getQuantityOrdered()
        ).isEqualTo(30);
    }

    @Test
    void repo_findById_whenNotExists_shouldReturnEmpty() {

        assertThat(
                orderDetailRepo.findById(
                        new OrderDetailId(
                                99999,
                                "ZZZZZZ"
                        )
                )
        ).isEmpty();
    }

    @Test
    void repo_save_shouldPersistNewOrderDetail() {

        OrderDetailId newId =
                new OrderDetailId(
                        90002,
                        "S18_2248"
                );

        OrderDetail od =
                new OrderDetail();

        od.setId(newId);
        od.setQuantityOrdered(10);
        od.setPriceEach(
                new BigDecimal("55.09")
        );
        od.setOrderLineNumber((short)2);

        orderDetailRepo.save(od);

        assertThat(
                orderDetailRepo.findById(newId)
        ).isPresent();
    }

    @Test
    void repo_save_shouldIncreaseCount() {

        long countBefore =
                orderDetailRepo.count();

        OrderDetailId newId =
                new OrderDetailId(
                        90001,
                        "S18_2957"
                );

        OrderDetail od =
                new OrderDetail();

        od.setId(newId);
        od.setQuantityOrdered(15);
        od.setPriceEach(
                new BigDecimal("42.00")
        );
        od.setOrderLineNumber((short)3);

        orderDetailRepo.save(od);

        assertThat(
                orderDetailRepo.count()
        ).isEqualTo(
                countBefore + 1
        );
    }
}