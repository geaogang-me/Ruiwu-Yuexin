package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.entity.Address;
import com.gag.RuiwuYuexin.service.AddressService;
import com.gag.RuiwuYuexin.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("address/list")
    public Result<List<Address>> list(
            @RequestParam("userId") Long userId) {
        if (userId == null) {
            return Result.error("缺少 userId");
        }
        List<Address> list = addressService.listByUserId(userId);
        return Result.success(list);
    }
    @PostMapping("address/add")
    public Result<?> add(@RequestBody Address addr) {
        boolean ok = addressService.addAddress(addr);
        if (ok) return Result.success("添加成功");
        else return Result.error("添加失败");
    }
    @PutMapping("address/update")
    public Result<?> update(@RequestBody Address addr) {
        boolean ok = addressService.updateAddress(addr);
        if (ok) return Result.success("修改成功");
        else return Result.error("修改失败");
    }
    @DeleteMapping("address/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean ok = addressService.deleteAddress(id);
        if (ok) return Result.success("删除成功");
        else return Result.error("删除失败");
    }
}