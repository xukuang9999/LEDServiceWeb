package com.zhglxt.cms.service;

import java.util.List;
import com.zhglxt.cms.entity.CustomerMessage;

/**
 * customer messagesService接口
 * 
 * @author Alan
 * @date 2025-08-11
 */
public interface ICustomerMessageService 
{
    /**
     * 查询customer messages
     * 
     * @param id customer messages主键
     * @return customer messages
     */
    public CustomerMessage selectCustomerMessageById(Long id);

    /**
     * 查询customer messages列表
     * 
     * @param customerMessage customer messages
     * @return customer messages集合
     */
    public List<CustomerMessage> selectCustomerMessageList(CustomerMessage customerMessage);

    /**
     * 新增customer messages
     * 
     * @param customerMessage customer messages
     * @return 结果
     */
    public int insertCustomerMessage(CustomerMessage customerMessage);

    /**
     * 修改customer messages
     * 
     * @param customerMessage customer messages
     * @return 结果
     */
    public int updateCustomerMessage(CustomerMessage customerMessage);

    /**
     * 批量删除customer messages
     * 
     * @param ids 需要删除的customer messages主键集合
     * @return 结果
     */
    public int deleteCustomerMessageByIds(String ids);

    /**
     * 删除customer messages信息
     * 
     * @param id customer messages主键
     * @return 结果
     */
    public int deleteCustomerMessageById(Long id);
}
