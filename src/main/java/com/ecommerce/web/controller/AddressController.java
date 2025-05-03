package com.ecommerce.web.controller;

import com.ecommerce.web.dto.request.AddressDTO;
import com.ecommerce.web.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @PostMapping()
    public ResponseEntity<AddressDTO> creteAddress(@Valid @RequestBody AddressDTO addressDTO) {
        return  new ResponseEntity<>(addressService.createAddress(addressDTO), HttpStatus.CREATED);
    }
}
