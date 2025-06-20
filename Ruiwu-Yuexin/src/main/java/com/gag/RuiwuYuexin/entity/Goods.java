package com.gag.RuiwuYuexin.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(value = { "createTime", "updateTime" }, allowGetters = true)
public class Goods implements Serializable {
   private Integer  id ;
   private String goodName;
   private byte[] goodImage;
   @JsonSerialize(using = ToStringSerializer.class)
   private BigDecimal price;
   private Integer BelongShop;
   private List<GoodsImage> images;
   private Integer stock;
   private Integer status;
   private String description;
   @JsonFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime createTime;
   @JsonFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime updateTime;

   private static final long serialVersionUID = 1L;

}
