package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//友链列表接口
@RestController
@RequestMapping("/links")
public class LinkController
{
    @Autowired
    private LinkService linkService;

    @SystemLog(businessName = "查询友链列表")
    @GetMapping("getAllLink")
    public ResponseResult getAllLink()
    {
        return linkService.getAllLink();
    }
}
