package com.zbkj.common.request.onepass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 一号通用户登录请求对象
 * +----------------------------------------------------------------------
 * | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 * +----------------------------------------------------------------------
 * | Author: CRMEB Team <admin@crmeb.com>
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "OnePassLoginRequest对象", description = "一号通用户登录请求对象")
public class OnePassLoginRequest {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "access_key一号通后台AccessKey管理获得", required = true)
    @NotBlank(message = "AccessKey 不能为空")
    private String accessKey;

    @ApiModelProperty(value = "secret_key一号通后台AccessKey管理获得", required = true)
    @NotBlank(message = "secretKey 不能为空")
    private String secretKey;
}
