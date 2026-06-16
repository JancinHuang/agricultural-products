package com.agricultural.products.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Banner {
    private Long id;
    private String title;
    private String subtitle;
    private String buttonText;
    private String linkUrl;
    private String image;
    private String imageUrl;
    private Integer showTitle;
    private String titleColor;
    private Integer titleFontSize;
    private Integer titleFontWeight;
    private String subtitleColor;
    private Integer subtitleFontSize;
    private Integer showButton;
    private String buttonColor;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
