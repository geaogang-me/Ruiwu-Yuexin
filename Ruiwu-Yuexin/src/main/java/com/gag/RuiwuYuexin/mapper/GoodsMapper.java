package com.gag.RuiwuYuexin.mapper;

import com.gag.RuiwuYuexin.dto.FavoriteDto;
import com.gag.RuiwuYuexin.entity.Goods;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GoodsMapper {
    List<Goods> findGoodsPage(@Param("keyword") String keyword,
                              @Param("type") String type,
                              @Param("offset") int offset,
                              @Param("size") int size);

    int countGoods(@Param("keyword") String keyword,
                     @Param("type") String type);

    List<Goods> selectPageByShop(@Param("shopId") Long shopId,
                                 @Param("status") Integer status,
                                 @Param("offset") int offset,
                                 @Param("size") int size);
    long countByShop(@Param("shopId") Long shopId,@Param("status") Integer status);
    Goods selectById(@Param("id") Integer id);

    int updateByPrimaryKeySelective(Goods record);



    // 新增：按 id 查并关联 images
    @Select("SELECT * FROM goods WHERE id = #{id}")
    @Results({
            @Result(column="id", property="id", id=true),
            @Result(column="good_name", property="goodName"),
            @Result(column="price", property="price"),
            @Result(column="createTime", property="createTime"),
            @Result(property="images", column="id",
                    many=@Many(select="com.gag.RuiwuYuexin.mapper.GoodsImageMapper.selectBygoodId"))
    })
    Goods selectByIdWithImages(@Param("id") int id);

    List<FavoriteDto> selectGoodsByIds(@Param("ids") List<Long> ids);
    int insertSelective(Goods record);

    int deleteByIdAndShop(@Param("id") Integer id,
                          @Param("shopId") Long shopId);

    /**
     * 批量删除
     */
    int deleteBatchByShop(@Param("list") List<Integer> ids,
                          @Param("shopId") Long shopId);
}
