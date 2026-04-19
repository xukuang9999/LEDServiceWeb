package com.zhglxt.web.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ModernFrontendController
{
    @GetMapping({
        "/modern",
        "/modern/",
        "/modern/{path:[a-zA-Z0-9\\-]+}",
        "/modern/{path:[a-zA-Z0-9\\-]+}/"
    })
    public String modernFrontend(@PathVariable(value = "path", required = false) String path)
    {
        if (path == null || path.isBlank() || "index".equals(path))
        {
            return "redirect:/cms/index.html";
        }
        if ("projects".equals(path))
        {
            return "redirect:/cms/projects.html";
        }
        if ("rental-service".equals(path))
        {
            return "redirect:/cms/rental-service.html";
        }
        if ("about".equals(path))
        {
            return "redirect:/cms/about.html";
        }
        if ("contact".equals(path))
        {
            return "redirect:/cms/contact.html";
        }
        if ("404".equals(path))
        {
            return "redirect:/cms/index.html";
        }
        if ("led-products".equals(path))
        {
            return "redirect:/cms/led-products.html";
        }
        if ("solution".equals(path) || "solutions".equals(path))
        {
            return "redirect:/cms/multi-industry-solutions.html";
        }
        if ("exhibition".equals(path))
        {
            return "redirect:/cms/exhibition.html";
        }
        if ("exhibition-detail".equals(path))
        {
            return "redirect:/cms/exhibition-detail.html";
        }
        if ("hospitality".equals(path) || "hospitality-exhibition".equals(path))
        {
            return "redirect:/cms/hospitality-exhibition.html";
        }
        if ("products".equals(path) || "exhibition-products".equals(path))
        {
            return "redirect:/cms/exhibition-products.html";
        }
        if ("news".equals(path) || "news-detail".equals(path))
        {
            return "redirect:/cms/news.html";
        }
        if ("service".equals(path) || "service-detail".equals(path))
        {
            return "redirect:/cms/service.html";
        }
        if ("develop-history".equals(path))
        {
            return "redirect:/cms/developHistory.html";
        }

        return "redirect:/cms/index.html";
    }
}
