package com.zhglxt.web.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyCmsAliasController
{
    @GetMapping({ "/", "/index.html" })
    public String legacyHome()
    {
        return "redirect:/cms/index.html";
    }

    @GetMapping("/about.html")
    public String legacyAbout()
    {
        return "redirect:/cms/about.html";
    }

    @GetMapping("/contact.html")
    public String legacyContact()
    {
        return "redirect:/cms/contact.html";
    }

    @GetMapping("/projects.html")
    public String legacyProjects()
    {
        return "redirect:/cms/projects.html";
    }

    @GetMapping("/rental-service.html")
    public String legacyRentalService()
    {
        return "redirect:/cms/rental-service.html";
    }
}
