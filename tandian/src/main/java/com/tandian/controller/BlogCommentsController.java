package com.tandian.controller;

import com.tandian.dto.Result;
import com.tandian.entity.BlogComments;
import com.tandian.service.IBlogCommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "笔记评论")
@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {

    @Resource
    private IBlogCommentsService blogCommentsService;

    @Operation(summary = "查询笔记评论")
    @GetMapping("/{blogId}")
    public Result queryByBlogId(@PathVariable("blogId") Long blogId) {
        return blogCommentsService.queryByBlogId(blogId);
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result addComment(@RequestBody BlogComments comment) {
        return blogCommentsService.addComment(comment);
    }

    @Operation(summary = "删除自己的评论")
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable("id") Long id) {
        return blogCommentsService.deleteComment(id);
    }
}
