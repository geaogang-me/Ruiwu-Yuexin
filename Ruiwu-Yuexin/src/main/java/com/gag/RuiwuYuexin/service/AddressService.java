package com.gag.RuiwuYuexin.service;
import com.gag.RuiwuYuexin.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> listByUserId(Long userId);
    boolean  addAddress(Address address);
    boolean  updateAddress(Address address);

    boolean deleteAddress(Long id);
}
