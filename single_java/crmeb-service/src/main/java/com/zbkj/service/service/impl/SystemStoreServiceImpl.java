package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.system.SystemStore;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreNearRequest;
import com.zbkj.common.request.SystemStoreRequest;
import com.zbkj.common.response.StoreNearResponse;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.vo.SystemStoreNearVo;
import com.zbkj.service.dao.SystemStoreDao;
import com.zbkj.service.service.SystemAttachmentService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.SystemStoreService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SystemStoreServiceImpl 接口实现
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
@Service
public class SystemStoreServiceImpl extends ServiceImpl<SystemStoreDao, SystemStore> implements SystemStoreService {

    @Resource
    private SystemStoreDao dao;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemAttachmentService systemAttachmentService;


    /**
     * 分页显示门店
     * @param keywords 搜索条件
     * @param pageParamRequest 分页参数
     */
    @Override
    public List<SystemStore> getList(String keywords, Integer status, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<SystemStore> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (status == 1) { // 显示中
            lambdaQueryWrapper.eq(SystemStore::getIsShow, true).eq(SystemStore::getIsDel, false);
        }else if (status == 2) { // 回收站中
            lambdaQueryWrapper.eq(SystemStore::getIsDel, true);
        } else { // 隐藏中的
            lambdaQueryWrapper.eq(SystemStore::getIsShow, false).eq(SystemStore::getIsDel, false);
        }
        if (!StringUtils.isBlank(keywords)) {
            lambdaQueryWrapper.and(i -> i.or().like(SystemStore::getName, keywords)
                    .or().like(SystemStore::getPhone, keywords));
        }
        lambdaQueryWrapper.orderByDesc(SystemStore::getUpdateTime).orderByDesc(SystemStore::getId);
        return dao.selectList(lambdaQueryWrapper);
    }

    /**
     * 根据基本参数查询
     * @param systemStore 基本参数
     * @return 门店结果
     */
    @Override
    public SystemStore getByCondition(SystemStore systemStore) {
        LambdaQueryWrapper<SystemStore> lqw = new LambdaQueryWrapper<>();
        lqw.setEntity(systemStore);
        return dao.selectOne(lqw);
    }

    /**
     * 修改门店显示状态
     * @param id integer id
     * @param status 状态
     * @return boolean
     */
    @Override
    public Boolean updateStatus(Integer id, Boolean status) {
        SystemStore systemStore = getById(id);
        if (ObjectUtil.isNull(systemStore)) {
            throw new CrmebException("门店自提点不存在");
        }
        if (systemStore.getIsShow().equals(status)) {
            return true;
        }
        systemStore.setIsShow(status);
        return updateById(systemStore);
    }

    /**
     * 删除门店自提
     * @param id Integer
     * @return boolean
     */
    @Override
    public Boolean delete(Integer id) {
        SystemStore systemStore = new SystemStore();
        systemStore.setId(id);
        systemStore.setIsDel(true);
        dao.updateById(systemStore);
        return true;
    }

    /**
     * 数量
     * @return HashMap<String, Integer>
     */
    @Override
    public HashMap<String, Integer> getCount() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("show", getCountByStatus(1));// 显示中
        map.put("hide", getCountByStatus(0));// 隐藏
        map.put("recycle", getCountByStatus(2));// 回收站
        return map;
    }

    /**
     * 根据状态获取总数
     * @return HashMap<String, Integer>
     */
    private Integer getCountByStatus(Integer status) {
        LambdaQueryWrapper<SystemStore> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (status == 2) {
            lambdaQueryWrapper.eq(SystemStore::getIsDel, true);
        } else {
            lambdaQueryWrapper.eq(SystemStore::getIsShow, status).eq(SystemStore::getIsDel, false);
        }
        lambdaQueryWrapper.orderByDesc(SystemStore::getUpdateTime).orderByDesc(SystemStore::getId);
        return dao.selectCount(lambdaQueryWrapper);
    }

    /**
     * 根据id集合查询数据，返回 map
     * @param storeIdList List<Integer> id集合
     * @return HashMap<Integer, SystemStore>
     */
    @Override
    public HashMap<Integer, SystemStore> getMapInId(List<Integer> storeIdList) {
        HashMap<Integer, SystemStore> map = new HashMap<>();
        if (storeIdList.size() < 1) {
            return map;
        }
        LambdaQueryWrapper<SystemStore> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SystemStore::getId, storeIdList);
        List<SystemStore> systemStoreList = dao.selectList(lambdaQueryWrapper);
        if (systemStoreList.size() < 1) {
            return map;
        }
        for (SystemStore systemStore : systemStoreList) {
            map.put(systemStore.getId(), systemStore);
        }
        return map;
    }

    /**
     * 附近的提货点
     * @param request StoreNearRequest 经纬度参数
     * @param pageParamRequest PageParamRequest 分页参数
     * @return StoreNearResponse
     */
    @Override
    public StoreNearResponse getNearList(StoreNearRequest request, PageParamRequest pageParamRequest) {
        StoreNearResponse storeNearResponse = new StoreNearResponse();

        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        List<SystemStoreNearVo> storeNearVoArrayList = new ArrayList<>();

        if (StringUtils.isNotBlank(request.getLatitude()) && StringUtils.isNotBlank(request.getLongitude())) {
            if (!request.getLatitude().matches("^(90(\\.0+)?|([1-8]?\\d)(\\.\\d+)?)$")
                    || !request.getLongitude().matches("^(180(\\.0+)?|(1[0-7]?\\d|[1-9]?\\d)(\\.\\d+)?)$")) {
                throw new CrmebException("经纬度坐标输入有误");
            }
            storeNearVoArrayList = dao.getNearList(request);
        } else {
            List<SystemStore> list = getList(null, 1, pageParamRequest);
            for (SystemStore systemStore : list) {
                SystemStoreNearVo systemStoreNearVo = new SystemStoreNearVo();
                BeanUtils.copyProperties(systemStore, systemStoreNearVo);
                storeNearVoArrayList.add(systemStoreNearVo);
            }
        }

        storeNearResponse.setList(storeNearVoArrayList);
        return storeNearResponse;
    }

    /**
     * 新增门店自提
     * @param request SystemStoreRequest 新增参数
     * @return Boolean
     */
    @Override
    public Boolean create(SystemStoreRequest request) {
        SystemStore systemStore = new SystemStore();
        BeanUtils.copyProperties(request, systemStore);
        clearPrefix(systemStore);
        splitLat(systemStore);
        return save(systemStore);
    }

    /**
     * 修改门店自提
     * @param id integer id
     * @param request 修改参数
     * @return Boolean
     */
    @Override
    public Boolean update(Integer id, SystemStoreRequest request) {
        SystemStore systemStore = new SystemStore();
        BeanUtils.copyProperties(request, systemStore);
        systemStore.setId(id);
        splitLat(systemStore);
        clearPrefix(systemStore);
        return updateById(systemStore);
    }

    /**
     * 彻底删除
     * @param id 提货点编号
     * @return Boolean
     */
    @Override
    public Boolean completeLyDelete(Integer id) {
        SystemStore systemStore = getById(id);
        if (ObjectUtil.isNull(systemStore)) throw new CrmebException("提货点不存在!");
        int delete = dao.deleteById(id);
        return delete > 0;
    }

    /**
     * 提货点恢复
     * @param id 提货点编号
     * @return Boolean
     */
    @Override
    public Boolean recovery(Integer id) {
        SystemStore systemStore = getById(id);
        if (ObjectUtil.isNull(systemStore)) throw new CrmebException("提货点不存在!");
        if (!systemStore.getIsDel()) return Boolean.TRUE;
        systemStore.setIsDel(false);
        return updateById(systemStore);
    }

    /**
     * 门店自提详情
     * @param id Integer
     * @return SystemStore
     */
    @Override
    public SystemStore getInfo(Integer id) {
        SystemStore systemStore = getById(id);
        if (ObjectUtil.isNull(systemStore)) {
            throw new CrmebException("门店自提点不存在");
        }
        systemStore.setLatitude(systemStore.getLatitude() + "," + systemStore.getLongitude());
        return systemStore;
    }

    /**
     * 去掉图片前缀
     * @param systemStore SystemStore 新增参数
     */
    private void clearPrefix(SystemStore systemStore) {
        systemStore.setImage(systemAttachmentService.clearPrefix(systemStore.getImage()));
    }

    /**
     * 分解经纬度
     * @param systemStore SystemStore 新增参数
     */
    private void splitLat(SystemStore systemStore) {
        if (!StringUtils.isBlank(systemStore.getLatitude())) {
            List<String> list = CrmebUtil.stringToArrayStr(systemStore.getLatitude());
            systemStore.setLongitude(list.get(0));
            systemStore.setLatitude(list.get(1));
        }
    }

}

