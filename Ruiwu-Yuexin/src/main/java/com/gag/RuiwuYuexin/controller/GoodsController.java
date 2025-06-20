package com.gag.RuiwuYuexin.controller;

import com.gag.RuiwuYuexin.dto.GoodsDetailDTO;
import com.gag.RuiwuYuexin.entity.Goods;
import com.gag.RuiwuYuexin.entity.User;
import com.gag.RuiwuYuexin.service.GoodsService;
import com.gag.RuiwuYuexin.common.Result;
import com.gag.RuiwuYuexin.service.UserService;
import com.gag.RuiwuYuexin.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.LongToIntFunction;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @GetMapping("/good")
    public Result<Map<String, Object>> getgood(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return Result.success(goodsService.findGoodsPage(keyword, type, page, size));
    }
    @GetMapping("/shop/goods")
    public Result<Map<String, Object>> getShopGoods(
            @RequestParam Long shopId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> data = goodsService.findGoodsPageByShop(shopId,status, page, size);
        return Result.success(data);
    }

    @PutMapping("/shop/good/update")
    public Result<String> updateShopGood(@RequestBody Goods goods) {
        // 校验：必须带上商品 ID 和所属店铺
        if (goods.getId() == null || goods.getBelongShop() == null) {
            return Result.error("缺少商品 ID 或 店铺 ID");
        }
        boolean ok = goodsService.updateGoods(goods);
        if (ok) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败，商品可能不存在或不属于当前店铺");
        }
    }
    // 详情
    @GetMapping("/good/{id}")
    public Result<GoodsDetailDTO> detail(@PathVariable int id) {
        GoodsDetailDTO detail = goodsService.findGoodsDetail(id);
        return Result.success(detail);
    }
    @PostMapping("/good/create")
    public Result<Long> createGood(
            HttpServletRequest request,
            @RequestParam("goodName") String goodName,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stock") Integer stock,
            @RequestParam("status") Integer status,
            @RequestParam("description") String description,
            @RequestPart("mainImage") MultipartFile mainImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        // 从 Header 拿 token
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Result.error("未登录");
        }
        String token = auth.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 再通过 userId 查 shopId
        User user = userService.findById(userId);
        if (user == null || user.getId() == null) {
            return Result.error("无店铺信息");
        }
        Long shopId = user.getId();

        // 构造 Goods 实体并保存
        Goods g = new Goods();
        g.setGoodName(goodName);
        g.setPrice(price);
        g.setStock(stock);
        g.setStatus(status);
        g.setDescription(description);
        g.setBelongShop(shopId.intValue());

        Long newId = goodsService.createGoodWithImages(g, mainImage, images);
        return Result.success(newId);
    }
    // 单个删除
    @DeleteMapping("/shop/good/delete/{id}")
    public Result<String> deleteGood(@PathVariable Integer id,
                                     @RequestParam Long shopId) {
        boolean ok = goodsService.deleteByIdAndShop(id, shopId);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 批量删除
    @PostMapping("/shop/good/deleteBatch")
    public Result<String> deleteBatch(@RequestParam Long shopId,
                                      @RequestBody List<Integer> ids) {
        int removed = goodsService.deleteBatchByShop(ids, shopId);
        return Result.success(removed + " 条商品已删除");
    }
}
