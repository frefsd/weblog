package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.CategoryAddDTO;
import com.blog.dto.CategoryDeleteDTO;
import com.blog.dto.CategoryPageDTO;
import com.blog.dto.CategorySearchDTO;
import com.blog.entity.Category;
import com.blog.vo.CategorySimpleVO;
import com.blog.vo.SelectOptionVO;

import java.util.List;

/**
 * <p>
 * 文章分类表 服务类
 * </p>
 *
 * @author fanchen
 * @since 2026-03-08
 */
public interface ICategoryService extends IService<Category> {

    /**
     * 添加分类
     * @param dto
     */
    void addCategory(CategoryAddDTO dto);

    /**
     * 分页查询文章分类
     * @param dto
     * @return
     */
    IPage<Category> pageCategories(CategoryPageDTO dto);

    /**
     * 删除分类
     * @param dto
     */
    void deleteCategory(CategoryDeleteDTO dto);

    /**
     * 获取分类列表
     * @return
     */
    List<SelectOptionVO> selectList();

    /**
     * 分类搜索
     * @param dto
     * @return
     */
    List<SelectOptionVO> search(CategorySearchDTO dto);

    /**
     * 获取全部分类（前台使用）
     */
    List<CategorySimpleVO> getAllCategories();
}
