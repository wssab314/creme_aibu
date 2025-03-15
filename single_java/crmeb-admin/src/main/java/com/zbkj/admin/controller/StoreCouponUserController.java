package com.zbkj.admin.controller;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreCouponUserRequest;
import com.zbkj.common.request.StoreCouponUserSearchRequest;
import com.zbkj.common.response.StoreCouponUserResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.StoreCouponUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * 优惠券发放记录表 前端控制器
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
@Slf4j
@RestController
@RequestMapping("api/admin/marketing/coupon/user")
@Api(tags = "营销 -- 优惠券 -- 领取记录")
public class StoreCouponUserController {

    @Autowired
    private StoreCouponUserService storeCouponUserService;

    /**
     * 分页显示优惠券发放记录表
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:coupon:user:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreCouponUserResponse>> getList(@Validated StoreCouponUserSearchRequest request, @Validated PageParamRequest pageParamRequest) {
        CommonPage<StoreCouponUserResponse> storeCouponUserCommonPage = CommonPage.restPage(storeCouponUserService.getList(request, pageParamRequest));
        return CommonResult.success(storeCouponUserCommonPage);
    }

    /**
     * 领券
     * @param storeCouponUserRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:coupon:user:receive')")
    @ApiOperation(value = "领券")
    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    public CommonResult<String> receive(@Validated StoreCouponUserRequest storeCouponUserRequest) {
        if(storeCouponUserService.receive(storeCouponUserRequest)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }
}



