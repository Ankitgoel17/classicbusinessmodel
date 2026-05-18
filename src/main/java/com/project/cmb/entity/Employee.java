package com.project.cmb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "employeeNumber")
    private Integer employeeNumber;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "lastName", nullable = false)
    private String lastName;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "firstName", nullable = false)
    private String firstName;

    @NotBlank(message = "Extension is required")
    @Size(max = 10, message = "Extension must not exceed 10 characters")
    @Column(name = "extension", nullable = false)
    private String extension;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank(message = "Job title is required")
    @Size(max = 50, message = "Job title must not exceed 50 characters")
    @Column(name = "jobTitle", nullable = false)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officeCode")
    private Office office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportsTo")
    private Employee reportsTo;

    @OneToMany(mappedBy = "reportsTo", fetch = FetchType.LAZY)
    private List<Employee> reportees;

    @OneToMany(mappedBy = "salesRepEmployee", fetch = FetchType.LAZY)
    private List<Customer> customers;
}