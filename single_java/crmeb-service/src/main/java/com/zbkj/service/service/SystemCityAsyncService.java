package com.zbkj.service.service;

/**
 * SystemCityAsyncService 接口
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
public interface SystemCityAsyncService {

    /**
     * 数据整体刷入redis
     */
    void async(Integer id);

    /**
     * 设置属性列表进入redis
     */
    void setListTree();
}
