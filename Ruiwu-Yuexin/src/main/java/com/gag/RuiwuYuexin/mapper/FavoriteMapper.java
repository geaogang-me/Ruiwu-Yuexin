package com.gag.RuiwuYuexin.mapper;

import com.gag.RuiwuYuexin.dto.FavoriteDto;
import com.gag.RuiwuYuexin.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT INTO favorite(user_id, good_id) VALUES(#{userId}, #{goodId})")
    int insert(Favorite userFavorite);

    @Delete("DELETE FROM favorite WHERE user_id = #{userId} AND good_id = #{goodId}")
    int deleteByUserIdAndGoodId(@Param("userId") Long userId, @Param("goodId") Long goodId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND good_id = #{goodId}")
    Favorite selectByUserIdAndGoodId(@Param("userId") Long userId, @Param("goodId") Long goodId);

    List<Long> selectGoodIdsByUserId(Long userId);

    List<FavoriteDto> selectFavoriteGoodsByUserId(@Param("userId") Long userId);


}