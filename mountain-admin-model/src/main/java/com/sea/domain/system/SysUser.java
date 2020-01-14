package com.sea.domain.system;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 */
@Entity
@Table(name = "sys_user")
@Getter
@Setter
public class SysUser implements Serializable {
    private static final long serialVersionUID = 4287454181093070302L;
    /**
     * ID
     */
    @Id
    private String id;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    /**
     * 盐
     */
    private String salt;

    /**
     * 公司id
     */
    private String companyId;

    /**
     * 是否可用 0.不可用 1.可用
     */
    private Integer isValid;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
