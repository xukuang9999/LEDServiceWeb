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
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description Exhibition Products Controller
 * @Author System Generated
 * @Date 2025/01/09
 */
@Controller
@RequestMapping("/cms")
public class ExhibitionProductsController extends BaseController {
    private String prefix = "cms";
    private String status = "0"; // 0: show, 1: hide

    @Autowired
    private ISiteService siteService;

    @Autowired
    private IColumnService columnService;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Handle both products URLs
     */
    @RequestMapping({"/products.html", "/exhibition-products.html"})
    public String exhibitionProducts(HttpServletRequest request, Model model) {
        // Site information
        Site site = siteService.selectOneSite();
        model.addAttribute("site", site);

        // Get root column for navigation
        Map<String, Object> rootColumnMap = new HashMap<>();
        rootColumnMap.put("siteId", site.getId());
        rootColumnMap.put("parentId", 0);
        List<Column> rootColumn = columnService.selectColumnList(rootColumnMap);

        // Navigation columns
        Map<String, Object> paramMap = WebUtil.paramsToMap(request.getParameterMap());
        paramMap.put("parentId", rootColumn.getFirst().getId());
        paramMap.put("display", status);
        paramMap.put("siteId", site.getId());
        List<Column> columns = columnService.selectCMSColumnList(paramMap);
        model.addAttribute("columns", columns);

        // Exhibition products articles
        Map<String, Object> productsParamMap = new HashMap<>();
        productsParamMap.put("columnFlag", "products");
        productsParamMap.put("status", status);
        productsParamMap.put("siteId", site.getId());
        List<Article> productsArticles = articleMapper.selectArticleList(productsParamMap);
        model.addAttribute("productsArticles", productsArticles);

        return prefix + "/exhibition-products";
    }
}