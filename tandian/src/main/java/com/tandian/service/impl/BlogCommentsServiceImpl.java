package com.tandian.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tandian.dto.Result;
import com.tandian.dto.UserDTO;
import com.tandian.entity.Blog;
import com.tandian.entity.BlogComments;
import com.tandian.mapper.BlogCommentsMapper;
import com.tandian.service.IBlogCommentsService;
import com.tandian.service.IBlogService;
import com.tandian.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 笔记评论：新增、列表、删除自己的评论，并维护笔记评论计数。
 */
@Service
@RequiredArgsConstructor
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments>
        implements IBlogCommentsService {

    private static final int MAX_CONTENT_LENGTH = 500;

    private final IBlogService blogService;

    @Override
    public Result queryByBlogId(Long blogId) {
        if (blogId == null) {
            return Result.fail("笔记 id 不能为空");
        }
        List<BlogComments> comments = query()
                .eq("blog_id", blogId)
                .eq("status", false)
                .orderByAsc("create_time")
                .list();
        return Result.ok(comments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addComment(BlogComments comment) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        if (comment == null || comment.getBlogId() == null) {
            return Result.fail("笔记 id 不能为空");
        }
        if (StrUtil.isBlank(comment.getContent())) {
            return Result.fail("评论内容不能为空");
        }
        if (comment.getContent().length() > MAX_CONTENT_LENGTH) {
            return Result.fail("评论内容不能超过 " + MAX_CONTENT_LENGTH + " 字");
        }
        Blog blog = blogService.getById(comment.getBlogId());
        if (blog == null) {
            return Result.fail("笔记不存在");
        }

        comment.setId(null);
        comment.setUserId(user.getId());
        comment.setParentId(comment.getParentId() == null ? 0L : comment.getParentId());
        comment.setAnswerId(comment.getAnswerId() == null ? 0L : comment.getAnswerId());
        comment.setLiked(0);
        comment.setStatus(false);
        save(comment);

        blogService.update()
                .setSql("comments = comments + 1")
                .eq("id", blog.getId())
                .update();
        return Result.ok(comment.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteComment(Long id) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        BlogComments comment = getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        if (!user.getId().equals(comment.getUserId())) {
            return Result.fail("只能删除自己的评论");
        }

        removeById(id);
        blogService.update()
                .setSql("comments = comments - 1")
                .eq("id", comment.getBlogId())
                .gt("comments", 0)
                .update();
        return Result.ok();
    }
}
