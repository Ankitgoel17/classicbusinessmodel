package com.project.cmb.repository;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.projection.RecentOrderView;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepoTest {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CustomerRepo customerRepo;

    private Customer customer103;
    private Customer customer112;

    @BeforeEach
    void setup() {

        customer103 = customerRepo.findById(103)
                .orElseThrow();

        customer112 = customerRepo.findById(112)
                .orElseThrow();

        Order o1 = new Order();
        o1.setOrderNumber(90001);
        o1.setOrderDate(LocalDate.of(2024,1,10));
        o1.setRequiredDate(LocalDate.of(2024,1,20));
        o1.setStatus("Shipped");
        o1.setCustomer(customer103);

        orderRepo.save(o1);

        Order o2 = new Order();
        o2.setOrderNumber(90002);
        o2.setOrderDate(LocalDate.of(2024,2,5));
        o2.setRequiredDate(LocalDate.of(2024,2,15));
        o2.setStatus("In Process");
        o2.setCustomer(customer103);

        orderRepo.save(o2);

        Order o3 = new Order();
        o3.setOrderNumber(90003);
        o3.setOrderDate(LocalDate.of(2024,3,1));
        o3.setRequiredDate(LocalDate.of(2024,3,10));
        o3.setStatus("Cancelled");
        o3.setCustomer(customer112);

        orderRepo.save(o3);
    }

    @Test
    void repo_findAll_shouldReturnOrders() {
        assertThat(orderRepo.findAll()).isNotEmpty();
    }

    @Test
    void repo_findById_shouldReturnOrder() {

        Optional<Order> result =
                orderRepo.findById(90001);

        assertThat(result).isPresent();

        assertThat(result.get().getStatus())
                .isEqualTo("Shipped");
    }

    @Test
    void repo_findById_whenNotExists_shouldReturnEmpty() {

        assertThat(
                orderRepo.findById(99999)
        ).isEmpty();
    }

    @Test
    void repo_save_shouldPersistNewOrder() {

        Order o = new Order();

        o.setOrderNumber(90004);
        o.setOrderDate(LocalDate.of(2024,4,1));
        o.setRequiredDate(LocalDate.of(2024,4,10));
        o.setStatus("In Process");
        o.setCustomer(customer103);

        orderRepo.save(o);

        assertThat(
                orderRepo.findById(90004)
        ).isPresent();
    }

    @Test
    void repo_save_shouldIncreaseCount() {

        long countBefore =
                orderRepo.count();

        Order o = new Order();

        o.setOrderNumber(90005);
        o.setOrderDate(LocalDate.of(2024,5,1));
        o.setRequiredDate(LocalDate.of(2024,5,10));
        o.setStatus("In Process");
        o.setCustomer(customer112);

        orderRepo.save(o);

        assertThat(orderRepo.count())
                .isEqualTo(countBefore + 1);
    }

    @Test
    void repo_update_status_shouldModify() {

        Order o =
                orderRepo.findById(90002)
                        .orElseThrow();

        o.setStatus("Shipped");

        orderRepo.save(o);

        assertThat(
                orderRepo.findById(90002)
                        .orElseThrow()
                        .getStatus()
        ).isEqualTo("Shipped");
    }

    @Test
    void repo_update_shippedDate_shouldModify() {

        Order o =
                orderRepo.findById(90001)
                        .orElseThrow();

        o.setShippedDate(
                LocalDate.of(2024,1,15)
        );

        orderRepo.save(o);

        assertThat(
                orderRepo.findById(90001)
                        .orElseThrow()
                        .getShippedDate()
        ).isEqualTo(
                LocalDate.of(2024,1,15)
        );
    }

    @Test
    void repo_update_shouldNotChangeOtherFields() {

        Order o =
                orderRepo.findById(90001)
                        .orElseThrow();

        o.setStatus("Resolved");

        orderRepo.save(o);

        Order updated =
                orderRepo.findById(90001)
                        .orElseThrow();

        assertThat(
                updated.getCustomer()
                        .getCustomerNumber()
        ).isEqualTo(103);

        assertThat(
                updated.getOrderDate()
        ).isEqualTo(
                LocalDate.of(2024,1,10)
        );
    }

    @Test
    void repo_deleteById_shouldRemoveOrder() {

        orderRepo.deleteById(90003);

        assertThat(
                orderRepo.findById(90003)
        ).isEmpty();
    }

    @Test
    void repo_deleteById_shouldDecreaseCount() {

        long countBefore =
                orderRepo.count();

        orderRepo.deleteById(90003);

        assertThat(
                orderRepo.count()
        ).isEqualTo(
                countBefore - 1
        );
    }
}