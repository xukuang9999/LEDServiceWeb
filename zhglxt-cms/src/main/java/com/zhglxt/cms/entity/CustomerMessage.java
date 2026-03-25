package com.zhglxt.cms.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zhglxt.common.annotation.Excel;
import com.zhglxt.common.core.domain.BaseEntity;

/**
 * customer messages对象 cms_customer_message
 * 
 * @author Alan
 * @date 2025-08-11
 */
public class CustomerMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** customer message key */
    @Excel(name = "customer message key")
    private Long id;

    /** customer name */
    @Excel(name = "customer name")
    private String custName;

    /** customer email */
    @Excel(name = "customer email")
    private String email;

    /** customer phone */
    @Excel(name = "customer phone")
    private String phone;

    /** message subject */
    @Excel(name = "message subject")
    private String subject;

    /** customer messages */
    @Excel(name = "customer messages")
    private String messages;

    /** logic delete flag */
    private Integer isDel;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCustName(String custName) 
    {
        this.custName = custName;
    }

    public String getCustName() 
    {
        return custName;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getEmail() 
    {
        return email;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setSubject(String subject) 
    {
        this.subject = subject;
    }

    public String getSubject() 
    {
        return subject;
    }

    public void setMessages(String messages) 
    {
        this.messages = messages;
    }

    public String getMessages() 
    {
        return messages;
    }

    public void setIsDel(Integer isDel) 
    {
        this.isDel = isDel;
    }

    public Integer getIsDel() 
    {
        return isDel;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("custName", getCustName())
            .append("email", getEmail())
            .append("phone", getPhone())
            .append("subject", getSubject())
            .append("messages", getMessages())
            .append("isDel", getIsDel())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
