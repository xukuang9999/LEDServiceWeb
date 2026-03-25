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
 * @Description Hospitality Equipment Exhibition Controller
 * @Author System Generated
 * @Date 2025/01/09
 */
@Controller
@RequestMapping("/cms")
public class HospitalityExhibitionController extends BaseController {
    private String prefix = "cms";
    private String status = "0"; // 0: show, 1: hide

    @Autowired
    private ISiteService siteService;

    @Autowired
    private IColumnService columnService;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Handle both hospitality URLs
     */
    @RequestMapping({"/hospitality.html", "/hospitality-exhibition.html"})
    public String hospitalityExhibition(HttpServletRequest request, Model model) {
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

        // Hospitality exhibition articles
        Map<String, Object> hospitalityParamMap = new HashMap<>();
        hospitalityParamMap.put("columnFlag", "hospitality");
        hospitalityParamMap.put("status", status);
        hospitalityParamMap.put("siteId", site.getId());
        List<Article> hospitalityArticles = articleMapper.selectArticleList(hospitalityParamMap);
        model.addAttribute("hospitalityArticles", hospitalityArticles);

        // Profile articles for exhibition profile section
        Map<String, Object> profileParamMap = new HashMap<>();
        profileParamMap.put("subSection", "profile");
        profileParamMap.put("status", status);
        profileParamMap.put("siteId", site.getId());
        List<Article> profileArticles = articleMapper.selectArticleList(profileParamMap);
        model.addAttribute("profileArticles", profileArticles);

        // Event Purpose article (subSection='purpose', sort=10) - Single article
        Map<String, Object> eventPurposeParamMap = new HashMap<>();
        eventPurposeParamMap.put("subSection", "purpose");
        eventPurposeParamMap.put("sort", 10);
        eventPurposeParamMap.put("status", status);
        eventPurposeParamMap.put("siteId", site.getId());
        List<Article> eventPurposeList = articleMapper.selectArticleList(eventPurposeParamMap);
        Article eventPurposeArticle = eventPurposeList.isEmpty() ? null : eventPurposeList.get(0);
        model.addAttribute("eventPurposeArticle", eventPurposeArticle);

        // Exhibition Information article (subSection='purpose', sort=20) - Single article
        Map<String, Object> exhibitionInfoParamMap = new HashMap<>();
        exhibitionInfoParamMap.put("subSection", "purpose");
        exhibitionInfoParamMap.put("sort", 20);
        exhibitionInfoParamMap.put("status", status);
        exhibitionInfoParamMap.put("siteId", site.getId());
        List<Article> exhibitionInfoList = articleMapper.selectArticleList(exhibitionInfoParamMap);
        Article exhibitionInfoArticle = exhibitionInfoList.isEmpty() ? null : exhibitionInfoList.get(0);
        model.addAttribute("exhibitionInfoArticle", exhibitionInfoArticle);

        // Australian Market Potential article (subSection='purpose', sort=30) - Single article
        Map<String, Object> marketPotentialParamMap = new HashMap<>();
        marketPotentialParamMap.put("subSection", "purpose");
        marketPotentialParamMap.put("sort", 30);
        marketPotentialParamMap.put("status", status);
        marketPotentialParamMap.put("siteId", site.getId());
        List<Article> marketPotentialList = articleMapper.selectArticleList(marketPotentialParamMap);
        Article marketPotentialArticle = marketPotentialList.isEmpty() ? null : marketPotentialList.get(0);
        model.addAttribute("marketPotentialArticle", marketPotentialArticle);

        // Expand Into New Markets article (subSection='purpose', sort=40) - Single article
        Map<String, Object> expandMarketsParamMap = new HashMap<>();
        expandMarketsParamMap.put("subSection", "purpose");
        expandMarketsParamMap.put("sort", 40);
        expandMarketsParamMap.put("status", status);
        expandMarketsParamMap.put("siteId", site.getId());
        List<Article> expandMarketsList = articleMapper.selectArticleList(expandMarketsParamMap);
        Article expandMarketsArticle = expandMarketsList.isEmpty() ? null : expandMarketsList.get(0);
        model.addAttribute("expandMarketsArticle", expandMarketsArticle);

        // Organizer articles
        Map<String, Object> organizerParamMap = new HashMap<>();
        organizerParamMap.put("subSection", "organizer");
        organizerParamMap.put("status", status);
        organizerParamMap.put("siteId", site.getId());
        List<Article> organizerList = articleMapper.selectArticleList(organizerParamMap);
        model.addAttribute("organizerArticleList", organizerList);

        return prefix + "/hospitality-exhibition";
    }
}