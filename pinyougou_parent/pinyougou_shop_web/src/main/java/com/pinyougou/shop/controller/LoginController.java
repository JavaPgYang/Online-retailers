package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map getLoginName() {
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        HashMap<String, String> loginMap = new HashMap<String, String>();
        loginMap.put("loginName", loginName);
        return loginMap;
    }

}
