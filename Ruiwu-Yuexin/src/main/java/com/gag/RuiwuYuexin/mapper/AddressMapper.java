package com.gag.RuiwuYuexin.mapper;

import com.gag.RuiwuYuexin.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {
    List<Address> selectByUserId(@Param("userId") Long userId);
    int  addAddress(Address address);

    int  updateAddress(Address address);
    int deleteById(@Param("id") Long id);
}
