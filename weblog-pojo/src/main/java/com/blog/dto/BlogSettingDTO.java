package com.blog.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogSettingDTO {
        /**
         * 博客名称 (必填)
         */
        @NotBlank(message = "博客名称不能为空")
        private String blogName;

        /**
         * 作者名 (必填)
         */
        @NotBlank(message = "作者名不能为空")
        private String author;

        /**
         * 作者头像 (必填)
         */
        @NotBlank(message = "作者头像不能为空")
        private String avatar;

        /**
         * 介绍语 (选填)
         */
        private String introduction;

        /**
         * GitHub 主页 (选填)
         *
         */
        private String githubHome;

        /**
         * CSDN 主页 (选填)
         */
        private String csdnHome;

        /**
         * Gitee 主页 (选填)
         */
        private String giteeHome;

        /**
         * 知乎主页 (选填)
         */
        private String zhihuHome;
    }

