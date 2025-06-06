package com.gag.RuiwuYuexin.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Goods implements Serializable {
   private int id ;
   private String goodName;
   private byte[] goodImage;
   @JsonSerialize(using = ToStringSerializer.class)
   private BigDecimal price;
   private List<GoodsImage> images;
   @JsonFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime createTime;
   @JsonFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime updateTime;

   private static final long serialVersionUID = 1L;

}
