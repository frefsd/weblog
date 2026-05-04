package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 博客设置表
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("blog_setting")
public class BlogSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 博客名称
     */
    private String blogName;

    /**
     * 作者名
     */
    private String author;

    /**
     * 介绍语
     */
    private String introduction;

    /**
     * 作者头像
     */
    private String avatar;

    /**
     * GitHub 主页访问地址
     */
    private String githubHome;

    /**
     * CSDN 主页访问地址
     */
    private String csdnHome;

    /**
     * Gitee 主页访问地址
     */
    private String giteeHome;

    /**
     * 知乎主页访问地址
     */
    private String zhihuHome;


}
