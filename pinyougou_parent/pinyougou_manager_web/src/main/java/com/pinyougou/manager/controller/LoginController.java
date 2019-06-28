package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/login")
public class LoginController {

    @ResponseBody
    @RequestMapping("/name")
    public Map name() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("loginName", username);
        return hashMap;
    }

}
