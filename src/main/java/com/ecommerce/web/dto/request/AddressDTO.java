package com.ecommerce.web.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Column(name = "street")
    private String street;

    @NotBlank
    @Size(min = 3, message = "City should be at least 3 characters long")
    @Column(name = "city")
    private String city;

    @NotBlank
    @Size(min = 3, message = "State should be at least 3 characters long")
    @Column(name = "state")
    private String state;

    @NotBlank
    @Size(min = 4, message = "zip / pincode should be at least 4 characters long")
    @Column(name = "zip")
    private String zip;

    @NotBlank
    @Size(min = 3, message = "Country should be at least 3 characters long")
    @Column(name = "country")
    private String country;
}
