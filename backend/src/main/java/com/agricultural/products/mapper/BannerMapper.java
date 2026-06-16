package com.agricultural.products.mapper;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.entity.Banner;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BannerMapper {

    @Select("SELECT * FROM banner WHERE id = #{id}")
    Banner findById(Long id);

    @Select("SELECT * FROM banner WHERE status = 1 ORDER BY sort ASC, create_time DESC")
    List<Banner> findEnabled();

    @Select("""
            <script>
            SELECT * FROM banner
            <where>
              <if test="keyword != null and keyword != ''">
                AND title LIKE CONCAT('%', #{keyword}, '%')
              </if>
              <if test="status != null">
                AND status = #{status}
              </if>
            </where>
            ORDER BY sort ASC, create_time DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<Banner> findByPage(PageRequest request);

    @Select("""
            <script>
            SELECT COUNT(*) FROM banner
            <where>
              <if test="keyword != null and keyword != ''">
                AND title LIKE CONCAT('%', #{keyword}, '%')
              </if>
              <if test="status != null">
                AND status = #{status}
              </if>
            </where>
            </script>
            """)
    Long countByPage(PageRequest request);

    @Insert("""
            INSERT INTO banner(
              title, subtitle, button_text, link_url, image,
              show_title, title_color, title_font_size, title_font_weight,
              subtitle_color, subtitle_font_size, show_button, button_color,
              sort, status, create_time, update_time
            ) VALUES(
              #{title}, #{subtitle}, #{buttonText}, #{linkUrl}, #{image},
              #{showTitle}, #{titleColor}, #{titleFontSize}, #{titleFontWeight},
              #{subtitleColor}, #{subtitleFontSize}, #{showButton}, #{buttonColor},
              #{sort}, #{status}, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Banner banner);

    @Update("""
            <script>
            UPDATE banner
            <set>
              <if test="title != null">title = #{title},</if>
              <if test="subtitle != null">subtitle = #{subtitle},</if>
              <if test="buttonText != null">button_text = #{buttonText},</if>
              <if test="linkUrl != null">link_url = #{linkUrl},</if>
              <if test="image != null">image = #{image},</if>
              <if test="showTitle != null">show_title = #{showTitle},</if>
              <if test="titleColor != null">title_color = #{titleColor},</if>
              <if test="titleFontSize != null">title_font_size = #{titleFontSize},</if>
              <if test="titleFontWeight != null">title_font_weight = #{titleFontWeight},</if>
              <if test="subtitleColor != null">subtitle_color = #{subtitleColor},</if>
              <if test="subtitleFontSize != null">subtitle_font_size = #{subtitleFontSize},</if>
              <if test="showButton != null">show_button = #{showButton},</if>
              <if test="buttonColor != null">button_color = #{buttonColor},</if>
              <if test="sort != null">sort = #{sort},</if>
              <if test="status != null">status = #{status},</if>
              update_time = NOW()
            </set>
            WHERE id = #{id}
            </script>
            """)
    int update(Banner banner);

    @Delete("DELETE FROM banner WHERE id = #{id}")
    int deleteById(Long id);
}
