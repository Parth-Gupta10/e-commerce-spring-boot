package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.AddressDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getUserAddresses();

    AddressDTO updateAddress(Long addressId, @Valid AddressDTO addressDTO);

    void deleteAddress(Long addressId);
}
