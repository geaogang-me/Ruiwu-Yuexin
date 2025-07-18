package com.gag.RuiwuYuexin.mapper;

import com.gag.RuiwuYuexin.dto.OrderDetailDto;
import com.gag.RuiwuYuexin.entity.Order;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderMapper {

    /** 根据商品ID查询单价 */
    @Select("SELECT price FROM goods WHERE id = #{goodId}")
    BigDecimal selectUnitPriceByGoodId(@Param("goodId") Long goodId);

    /** 插入订单 */
    @Insert("""
      INSERT INTO orders
        (user_id, good_id, address_id, num, price, status, created)
      VALUES
        (#{userId}, #{goodId}, #{addressId}, #{num}, #{price}, #{status}, #{created})
      """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(Order order);

    @Update("""
    UPDATE orders
    SET status = #{status}
    WHERE id = #{orderId}
    """)
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") int status);

    List<OrderDetailDto> getOrderDetailsByUserId(@Param("userId") Long userId);

    List<OrderDetailDto> findOrderDetailsByShopId(@Param("shopId") Long shopId);

    /** 根据订单ID查询订单 */
    @Select("SELECT * FROM orders WHERE id = #{orderId}")
    Order selectById(@Param("orderId") Long orderId);

    /** 根据订单ID查询订单状态 */
    @Select("SELECT status FROM orders WHERE id = #{orderId}")
    Integer selectStatusByOrderId(@Param("orderId") Long orderId);

    /** 更新单个订单状态 */
    @Update("""
      UPDATE orders
      SET status = #{status}
      WHERE id = #{orderId}
      """ )
    int updateStatus(@Param("orderId") Long orderId, @Param("status") int status);

    @Update({
            "<script>",
            "UPDATE orders SET status = #{status} WHERE id IN",
            "<foreach item='id' collection='orderIds' open='(' separator=',' close=')'>",
            "   #{id}",
            "</foreach>",
            "</script>"
    })
    void batchUpdateOrderStatus(
            @Param("orderIds") List<Long> orderIds,
            @Param("status") int status
    );

    @Delete("DELETE FROM orders WHERE id = #{orderId}")
    int deleteByPrimaryKey(@Param("orderId") Long orderId);
    /** 根据订单ID更新订单状态 */
    @Update("""
  UPDATE orders
  SET status = #{status}
  WHERE id = #{id}
  """)
    int updateById(Order order);

}
