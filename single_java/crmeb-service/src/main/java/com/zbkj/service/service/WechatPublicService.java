package com.zbkj.service.service;

/**
 * 微信公众号service
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
public interface WechatPublicService {

    /**
     * 获取公众号自定义菜单
     */
    Object getCustomizeMenus();

    /**
     * 保存自定义菜单
     * @param data 菜单json
     * @return Boolean
     */
    Boolean createMenus(String data);

    /**
     * 删除自定义菜单
     * @return Boolean
     */
    Boolean deleteMenus();
}
