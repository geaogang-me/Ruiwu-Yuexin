package com.gag.RuiwuYuexin.service.impl;

import com.gag.RuiwuYuexin.entity.Address;
import com.gag.RuiwuYuexin.mapper.AddressMapper;
import com.gag.RuiwuYuexin.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> listByUserId(Long userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public boolean  addAddress(Address address) {
        return  addressMapper.addAddress(address)==1;
    }

    @Override
    public boolean updateAddress(Address address) {
        return addressMapper.updateAddress(address) == 1;
    }

    @Override
    public boolean deleteAddress(Long id) {
        return addressMapper.deleteById(id) == 1;
    }
}