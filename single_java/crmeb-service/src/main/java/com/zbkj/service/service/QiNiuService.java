package com.zbkj.service.service;

import com.zbkj.common.vo.CloudVo;
import com.qiniu.storage.UploadManager;

import java.io.File;

/**
 * QiNiuService 接口
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
public interface QiNiuService {
    void upload(UploadManager uploadManager, CloudVo cloudVo, String upToken, String webPth, String localFile, Integer id);

    void uploadFile(UploadManager uploadManager, String upToken, String webPth, String localFile, File file);
}
