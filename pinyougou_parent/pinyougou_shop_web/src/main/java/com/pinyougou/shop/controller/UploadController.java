package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

/**
 * 文件上传Controller
 */
@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String file_server_url;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        Result result = null;

        // 获取上传文件扩展名
        String originalFilename = file.getOriginalFilename();
        String exName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        try {
            // 创建FastDFS的客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            // 执行上传处理
            String path = fastDFSClient.uploadFile(file.getBytes(), exName);

            // 拼接返回的url
            String url = file_server_url + path;

            result = new Result(true, url);
        } catch (Exception e) {
            result = new Result(false, "上传失败");
            e.printStackTrace();
        }

        return result;
    }

}
