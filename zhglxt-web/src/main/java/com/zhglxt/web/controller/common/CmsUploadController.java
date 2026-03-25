package com.zhglxt.web.controller.common;

import com.zhglxt.common.config.GlobalConfig;
import com.zhglxt.common.config.ServerConfig;
import com.zhglxt.common.core.domain.AjaxResult;
import com.zhglxt.common.utils.StringUtils;
import com.zhglxt.common.utils.file.FileUploadUtils;
import com.zhglxt.common.utils.file.FileUtils;
import com.zhglxt.common.utils.file.MimeTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * CMS文件上传处理
 * 
 * @author zhglxt
 */
@Controller
@RequestMapping("/common/cms")
public class CmsUploadController
{
    private static final Logger log = LoggerFactory.getLogger(CmsUploadController.class);

    @Autowired
    private ServerConfig serverConfig;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * CMS文件上传请求
     */
    @PostMapping("/upload")
    @ResponseBody
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            // 上传文件路径 - 使用CMS专用路径
            String filePath = GlobalConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
            String url = serverConfig.getUrl() + "/profile/" + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            log.error("CMS文件上传失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }
}