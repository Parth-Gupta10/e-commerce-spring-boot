package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.AddressDTO;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.exception.ResourceNotFoundException;
import com.ecommerce.web.model.Address;
import com.ecommerce.web.model.User;
import com.ecommerce.web.repository.AddressRepository;
import com.ecommerce.web.repository.UserRepository;
import com.ecommerce.web.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        if (addressDTO == null) {
            throw new APIException("Address data is missing", HttpStatus.BAD_REQUEST);
        }

        User currentUser;
        try {
            currentUser = authUtil.loggedInUser();
        } catch (UsernameNotFoundException e) {
            throw new APIException("User not logged in", HttpStatus.BAD_REQUEST);
        }

        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(currentUser);

        Address savedAddress = addressRepository.save(address);

        currentUser.getAddresses().add(savedAddress);
        userRepository.save(currentUser);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        if (addressId == null) {
            throw new APIException("Address ID is missing", HttpStatus.BAD_REQUEST);
        }

        Address address = addressRepository.findById(addressId).
                orElseThrow(() -> new ResourceNotFoundException("address", "addressId", addressId));

        return modelMapper.map(address, AddressDTO.class);
    }
}
