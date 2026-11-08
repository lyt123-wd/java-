package com.tandian.controller;


import com.tandian.dto.Result;
import com.tandian.service.IFollowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
      return followService.follow(followUserId,isFollow);
    }

    @PutMapping("/or/not/{id}")
    public Result follow(@PathVariable("id") Long followUserId) {
       return followService.isFollow(followUserId);
    }
    @GetMapping("/common/{id}")
    public Result commonFollow(@PathVariable("id") Long id) {
        return followService.commonFollow(id);
    }
}
