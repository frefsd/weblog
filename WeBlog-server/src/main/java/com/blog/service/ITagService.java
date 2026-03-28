package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.TagAddDTO;
import com.blog.dto.TagDeleteDTO;
import com.blog.dto.TagPageDTO;
import com.blog.dto.TagSearchDTO;
import com.blog.entity.Tag;
import com.blog.vo.SelectOptionVO;
import com.blog.vo.TagSimpleVO;

import java.util.List;

/**
 * <p>
 * 标签表 服务类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
public interface ITagService extends IService<Tag> {

    /**
     * 添加标签
     * @param dto
     */
    void addTags(TagAddDTO dto);

    /**
     * 分页查询标签
     * @param dto
     * @return
     */
    IPage<Tag> pageTags(TagPageDTO dto);

    /**
     * 删除标签
     * @param dto
     */
    void deleteTag(TagDeleteDTO dto);

    /**
     * 标签下拉列表
     * @return
     */
    List<SelectOptionVO> selectList();

    /**
     * 标签搜索
     * @param dto
     * @return
     */
    List<SelectOptionVO> search(TagSearchDTO dto);

    /**
     * 获取全部标签（前台使用）
     */
    List<TagSimpleVO> getAllTags();
}
