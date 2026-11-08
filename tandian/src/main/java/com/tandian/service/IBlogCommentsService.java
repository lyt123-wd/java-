package com.tandian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tandian.dto.Result;
import com.tandian.entity.BlogComments;

public interface IBlogCommentsService extends IService<BlogComments> {

    Result queryByBlogId(Long blogId);

    Result addComment(BlogComments comment);

    Result deleteComment(Long id);
}
