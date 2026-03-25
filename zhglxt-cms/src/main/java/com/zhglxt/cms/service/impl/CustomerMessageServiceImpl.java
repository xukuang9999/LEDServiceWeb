package com.zhglxt.cms.service.impl;

import java.util.List;
import com.zhglxt.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zhglxt.cms.mapper.CustomerMessageMapper;
import com.zhglxt.cms.entity.CustomerMessage;
import com.zhglxt.cms.service.ICustomerMessageService;
import com.zhglxt.common.core.text.Convert;

/**
 * customer messagesService业务层处理
 * 
 * @author Alan
 * @date 2025-08-11
 */
@Service
public class CustomerMessageServiceImpl implements ICustomerMessageService 
{
    @Autowired
    private CustomerMessageMapper customerMessageMapper;

    /**
     * 查询customer messages
     * 
     * @param id customer messages主键
     * @return customer messages
     */
    @Override
    public CustomerMessage selectCustomerMessageById(Long id)
    {
        return customerMessageMapper.selectCustomerMessageById(id);
    }

    /**
     * 查询customer messages列表
     * 
     * @param customerMessage customer messages
     * @return customer messages
     */
    @Override
    public List<CustomerMessage> selectCustomerMessageList(CustomerMessage customerMessage)
    {
        return customerMessageMapper.selectCustomerMessageList(customerMessage);
    }

    /**
     * 新增customer messages
     * 
     * @param customerMessage customer messages
     * @return 结果
     */
    @Override
    public int insertCustomerMessage(CustomerMessage customerMessage)
    {
        customerMessage.setCreateTime(DateUtils.getNowDate());
        return customerMessageMapper.insertCustomerMessage(customerMessage);
    }

    /**
     * 修改customer messages
     * 
     * @param customerMessage customer messages
     * @return 结果
     */
    @Override
    public int updateCustomerMessage(CustomerMessage customerMessage)
    {
        customerMessage.setUpdateTime(DateUtils.getNowDate());
        return customerMessageMapper.updateCustomerMessage(customerMessage);
    }

    /**
     * 批量删除customer messages
     * 
     * @param ids 需要删除的customer messages主键
     * @return 结果
     */
    @Override
    public int deleteCustomerMessageByIds(String ids)
    {
        return customerMessageMapper.deleteCustomerMessageByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除customer messages信息
     * 
     * @param id customer messages主键
     * @return 结果
     */
    @Override
    public int deleteCustomerMessageById(Long id)
    {
        return customerMessageMapper.deleteCustomerMessageById(id);
    }
}
