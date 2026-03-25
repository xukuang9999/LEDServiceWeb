package com.zhglxt.web.controller.cms;
import com.zhglxt.cms.entity.Article;
import com.zhglxt.cms.entity.Column;
import com.zhglxt.cms.entity.Site;
import com.zhglxt.cms.mapper.ArticleMapper;
import com.zhglxt.cms.service.IColumnService;
import com.zhglxt.cms.service.ISiteService;
import com.zhglxt.common.core.controller.BaseController;
import com.zhglxt.common.utils.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 官网Index控制层
 * @Author liuwy
 * @Date 2019/12/4
 */
@Controller
@RequestMapping("/cms")
public class IndexController extends BaseController {
    private String prefix = "cms";

    //状态默认显示
    private String status = "0";

    @Autowired
    private ISiteService siteService;

    @Autowired
    private IColumnService columnService;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 初始化官网栏目数据
     */
    @RequestMapping("/index.html")
    public String cmsMain(Model model) {
        Site site = addSiteToModel(model);
        String siteId = site != null ? site.getId() : null;

        model.addAttribute("projects", limit(selectProjectArticles(siteId), 3));
        return prefix + "/index";
    }

    @RequestMapping("/about.html")
    public String about(Model model) {
        Site site = addSiteToModel(model);
        String siteId = site != null ? site.getId() : null;
        model.addAttribute("projects", limit(selectProjectArticles(siteId), 2));
        return prefix + "/about";
    }

    @RequestMapping("/contact.html")
    public String contact(Model model) {
        addSiteToModel(model);
        return prefix + "/contact";
    }

    @RequestMapping("/projects.html")
    public String projects(Model model) {
        Site site = addSiteToModel(model);
        String siteId = site != null ? site.getId() : null;
        model.addAttribute("projects", selectProjectArticles(siteId));
        return prefix + "/projects";
    }

    @RequestMapping("/led-products.html")
    public String productsCatalog(Model model) {
        addSiteToModel(model);
        return prefix + "/products";
    }

    @RequestMapping("/rental-service.html")
    public String rentalService(Model model) {
        addSiteToModel(model);
        return prefix + "/rental-service";
    }

    @RequestMapping("/{url}.html")
    public String columnTabData(@PathVariable("url") String url, HttpServletRequest request, Model model) {
        // Handle exhibition page with full exhibition data
        if ("exhibition".equals(url)) {
            return handleExhibitionPage(request, model);
        }
        
        // Redirect specific exhibition routes to their dedicated controllers
        if ("multi-industry-solutions".equals(url) || "solutions".equals(url)) {
            return "redirect:/cms/multi-industry-solutions.html";
        }
        if ("hospitality".equals(url)) {
            return "redirect:/cms/hospitality-exhibition.html";
        }
        if ("products".equals(url)) {
            return "redirect:/cms/exhibition-products.html";
        }
        //站点
        Site site = siteService.selectOneSite();
        model.addAttribute("site", site);
        if (site == null) {
            model.addAttribute("columns", new ArrayList<>());
            model.addAttribute(url + "s", new ArrayList<>());
            return prefix + "/" + url;
        }

        //查询出当前站点顶级栏目的栏目id
        Map<String, Object> rootColumnMap = new HashMap<>();
        rootColumnMap.put("siteId", site.getId());
        rootColumnMap.put("parentId", 0);
        List<Column> rootColumn = columnService.selectColumnList(rootColumnMap);
        if (CollectionUtils.isEmpty(rootColumn)) {
            model.addAttribute("columns", new ArrayList<>());
            model.addAttribute(url + "s", new ArrayList<>());
            return prefix + "/" + url;
        }

        Map<String, Object> paramMap = WebUtil.paramsToMap(request.getParameterMap());
        paramMap.put("parentId", rootColumn.getFirst().getId());//父id为当前站点，顶级栏目id
        paramMap.put("display", status);//0：显示的
        paramMap.put("siteId", site.getId());//站点id
        List<Column> columns = columnService.selectCMSColumnList(paramMap);
        model.addAttribute("columns", columns);

        Map<String, Object> tabParamMap = new HashMap<>();
        tabParamMap.put("columnFlag", url);
        tabParamMap.put("status", status);//0显示 1隐藏
        tabParamMap.put("siteId", site.getId());//站点id
        List<Article> tabList = articleMapper.selectArticleList(tabParamMap);
        model.addAttribute(url + "s", tabList);

        return prefix + "/" +url;
    }

    /**
     * Handle exhibition page with full exhibition data
     */
    private String handleExhibitionPage(HttpServletRequest request, Model model) {
        //站点
        Site site = siteService.selectOneSite();
        model.addAttribute("site", site);
        if (site == null) {
            model.addAttribute("columns", new ArrayList<>());
            model.addAttribute("exhibitions", new ArrayList<>());
            return prefix + "/exhibition";
        }

        //查询出当前站点顶级栏目的栏目id
        Map<String, Object> rootColumnMap = new HashMap<>();
        rootColumnMap.put("siteId", site.getId());
        rootColumnMap.put("parentId", 0);
        List<Column> rootColumn = columnService.selectColumnList(rootColumnMap);
        if (CollectionUtils.isEmpty(rootColumn)) {
            model.addAttribute("columns", new ArrayList<>());
            model.addAttribute("exhibitions", new ArrayList<>());
            return prefix + "/exhibition";
        }

        //栏目
        Map<String, Object> paramMap = WebUtil.paramsToMap(request.getParameterMap());
        paramMap.put("parentId", rootColumn.getFirst().getId());//父id为当前站点，顶级栏目id
        paramMap.put("display", status);//0：显示的
        paramMap.put("siteId", site.getId());//站点id
        List<Column> columns = columnService.selectCMSColumnList(paramMap);
        model.addAttribute("columns", columns);

        //展会信息 - Load all exhibition articles
        Map<String, Object> tabParamMap = new HashMap<>();
        tabParamMap.put("columnFlag", "exhibition");
        tabParamMap.put("status", status);//0显示 1隐藏
        tabParamMap.put("siteId", site.getId());//站点id
        List<Article> tabList = articleMapper.selectArticleList(tabParamMap);
        model.addAttribute("exhibitions", tabList);

        return prefix + "/exhibition";
    }

    private Site addSiteToModel(Model model) {
        Site site = siteService.selectOneSite();
        model.addAttribute("site", site);
        return site;
    }

    private List<Article> selectArticles(String siteId, String columnFlag) {
        Map<String, Object> articleParamMap = new HashMap<>();
        articleParamMap.put("columnFlag", columnFlag);
        articleParamMap.put("status", status);
        if (siteId != null) {
            articleParamMap.put("siteId", siteId);
        }
        return articleMapper.selectArticleList(articleParamMap);
    }

    private List<Article> selectProjectArticles(String siteId) {
        String[] candidateFlags = {"projects", "exhibition", "service", "news"};
        for (String candidateFlag : candidateFlags) {
            List<Article> articles = selectArticles(siteId, candidateFlag);
            if (!CollectionUtils.isEmpty(articles)) {
                return articles;
            }
        }
        return new ArrayList<>();
    }

    private List<Article> limit(List<Article> articles, int maxItems) {
        if (CollectionUtils.isEmpty(articles) || articles.size() <= maxItems) {
            return articles;
        }
        return new ArrayList<>(articles.subList(0, maxItems));
    }
}
