package com.zhglxt.web.controller.cms;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.zhglxt.common.annotation.Log;
import com.zhglxt.common.enums.BusinessType;
import com.zhglxt.cms.entity.CustomerMessage;
import com.zhglxt.cms.service.ICustomerMessageService;
import com.zhglxt.common.core.controller.BaseController;
import com.zhglxt.common.core.domain.AjaxResult;
import com.zhglxt.common.utils.poi.ExcelUtil;
import com.zhglxt.common.core.page.TableDataInfo;

/**
 * customer messagesController
 * 
 * @author Alan
 * @date 2025-08-11
 */
@Controller
@RequestMapping("/zhglxt-cms/message")
public class CustomerMessageController extends BaseController
{
    private String prefix = "zhglxt-cms/message";

    @Autowired
    private ICustomerMessageService customerMessageService;

    @RequiresPermissions("zhglxt-cms:message:view")
    @GetMapping()
    public String message()
    {
        return prefix + "/message";
    }

    /**
     * 查询customer messages列表
     */
    @RequiresPermissions("zhglxt-cms:message:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(CustomerMessage customerMessage)
    {
        startPage();
        List<CustomerMessage> list = customerMessageService.selectCustomerMessageList(customerMessage);
        return getDataTable(list);
    }

    /**
     * 导出customer messages列表
     */
    @RequiresPermissions("zhglxt-cms:message:export")
    @Log(title = "customer messages", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(CustomerMessage customerMessage)
    {
        List<CustomerMessage> list = customerMessageService.selectCustomerMessageList(customerMessage);
        ExcelUtil<CustomerMessage> util = new ExcelUtil<CustomerMessage>(CustomerMessage.class);
        return util.exportExcel(list, "customer messages数据");
    }

    /**
     * 新增customer messages
     */
    @RequiresPermissions("zhglxt-cms:message:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存customer messages
     */
    @RequiresPermissions("zhglxt-cms:message:add")
    @Log(title = "customer messages", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(CustomerMessage customerMessage)
    {
        return toAjax(customerMessageService.insertCustomerMessage(customerMessage));
    }

    /**
     * 修改customer messages
     */
    @RequiresPermissions("zhglxt-cms:message:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        CustomerMessage customerMessage = customerMessageService.selectCustomerMessageById(id);
        mmap.put("customerMessage", customerMessage);
        return prefix + "/edit";
    }

    /**
     * 修改保存customer messages
     */
    @RequiresPermissions("zhglxt-cms:message:edit")
    @Log(title = "customer messages", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(CustomerMessage customerMessage)
    {
        return toAjax(customerMessageService.updateCustomerMessage(customerMessage));
    }

    /**
     * 删除customer messages
     */
    @RequiresPermissions("zhglxt-cms:message:remove")
    @Log(title = "customer messages", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(customerMessageService.deleteCustomerMessageByIds(ids));
    }
}
