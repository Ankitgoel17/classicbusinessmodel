package com.project.cmb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "productlines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductLine {

    @Id
    @NotBlank(message = "Product line name is required")
    @Size(max = 50, message = "Product line name must not exceed 50 characters")
    @Column(name = "productLine", length = 50, nullable = false)
    private String productLine;

    @Size(max = 4000, message = "Text description must not exceed 4000 characters")
    @Column(name = "textDescription", length = 4000)
    private String textDescription;

    @Column(name = "htmlDescription", columnDefinition = "MEDIUMTEXT")
    private String htmlDescription;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image", columnDefinition = "MEDIUMBLOB")
    private byte[] image;

    @OneToMany(mappedBy = "productLine", fetch = FetchType.LAZY)
    private List<Product> products;
}