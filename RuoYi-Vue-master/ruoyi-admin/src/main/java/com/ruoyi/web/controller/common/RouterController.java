package com.ruoyi.web.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RouterController {

    // 监听所有非静态资源的路径，转发给前端入口 index.html
    // 这里的正则表达式排除掉了带有后缀的文件（如 .js, .css, .png），防止静态资源死循环
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }

    // 针对多级路径的补充（例如 /system/user）
    @RequestMapping(value = "/**/{path:[^\\.]*}")
    public String redirectNested() {
        return "forward:/index.html";
    }
}