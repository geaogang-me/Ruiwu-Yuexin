package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.GoodsDetailDTO;
import com.gag.RuiwuYuexin.service.GoodsService;
import com.gag.RuiwuYuexin.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @GetMapping("/good")
    public Result getgood(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String type,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(goodsService.findGoodsPage(keyword, type, page, size));
    }
    // 详情
    @GetMapping("/good/{id}")
    public Result<GoodsDetailDTO> detail(@PathVariable int id) {
        GoodsDetailDTO detail = goodsService.findGoodsDetail(id);
        return Result.success(detail);
    }
}
