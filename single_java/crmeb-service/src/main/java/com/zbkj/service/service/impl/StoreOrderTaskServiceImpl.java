package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.*;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.sms.SmsTemplate;
import com.zbkj.common.model.system.SystemNotification;
import com.zbkj.common.model.user.*;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.model.bargain.StoreBargain;
import com.zbkj.common.model.combination.StoreCombination;
import com.zbkj.common.model.combination.StorePink;
import com.zbkj.common.model.coupon.StoreCouponUser;
import com.zbkj.common.model.wechat.video.PayComponentOrder;
import com.zbkj.common.model.wechat.video.PayComponentProduct;
import com.zbkj.common.model.wechat.video.PayComponentProductSku;
import com.zbkj.common.model.seckill.StoreSeckill;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.order.StoreOrderInfo;
import com.zbkj.common.model.product.StoreProductAttrValue;
import com.zbkj.common.model.system.SystemAdmin;
import com.zbkj.common.vo.ShopOrderPayVo;
import com.zbkj.service.delete.OrderUtils;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StoreOrderTaskService实现类
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
public class StoreOrderTaskServiceImpl implements StoreOrderTaskService {
    //日志
    private static final Logger logger = LoggerFactory.getLogger(StoreOrderTaskServiceImpl.class);

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreOrderStatusService storeOrderStatusService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private StoreSeckillService storeSeckillService;

    @Autowired
    private StoreBargainService storeBargainService;

    @Autowired
    private StoreCombinationService storeCombinationService;

    @Autowired
    private UserBillService userBillService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SystemAdminService systemAdminService;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private TemplateMessageService templateMessageService;

    @Autowired
    private OrderUtils orderUtils;

    @Autowired
    private StorePinkService storePinkService;

    @Autowired
    private UserIntegralRecordService userIntegralRecordService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private PayComponentOrderService componentOrderService;

    @Autowired
    private WechatVideoOrderService wechatVideoOrderService;

    @Autowired
    private PayComponentOrderProductService componentOrderProductService;

    @Autowired
    private PayComponentProductService componentProductService;

    @Autowired
    private PayComponentProductSkuService componentProductSkuService;

    @Autowired
    private StoreProductAttrValueService attrValueService;

    @Autowired
    private StoreCouponUserService couponUserService;

    @Autowired
    private UserExperienceRecordService userExperienceRecordService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    @Autowired
    private SmsTemplateService smsTemplateService;

    /**
     * 用户取消订单
     * @author Mr.Zhang
     * @since 2020-07-09
     */
    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Error.class, CrmebException.class})
    public Boolean cancelByUser(StoreOrder storeOrder) {
        try{
            /*
            * 1、修改订单状态 （用户操作的时候已处理）
            * 2、写订单日志
            * 3、回滚库存
            * 4、回滚优惠券
            * 5、回滚积分
            * */

            Boolean execute = transactionTemplate.execute(e -> {
                //写订单日志
                storeOrderStatusService.createLog(storeOrder.getId(), "cancel_order", "取消订单");
                // 退优惠券
                if (storeOrder.getCouponId() > 0) {
                    StoreCouponUser couponUser = couponUserService.getById(storeOrder.getCouponId());
                    couponUser.setStatus(CouponConstants.STORE_COUPON_USER_STATUS_USABLE);
                    couponUserService.updateById(couponUser);
                }
                Boolean rollbackStock = rollbackStock(storeOrder);
                if (!rollbackStock) {
                    throw new CrmebException("回滚库存失败");
                }
                return Boolean.TRUE;
            });
            return execute;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 完成订单
     * @author Mr.Zhang
     * @since 2020-07-09
     */
    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Error.class, CrmebException.class})
    public Boolean complete(StoreOrder storeOrder) {
        /*
         * 1、修改订单状态 （用户操作的时候已处理）
         * 2、写订单日志
         * */
        try{
            storeOrderStatusService.createLog(storeOrder.getId(), "check_order_over", "用户评价");
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 回滚库存
     * @param storeOrder 订单信息
     */
    private Boolean rollbackStock(StoreOrder storeOrder) {
        try{
            // 查找出商品详情
            List<StoreOrderInfo> orderInfoList = storeOrderInfoService.getListByOrderNo(storeOrder.getOrderId());
            if(null == orderInfoList || orderInfoList.size() < 1){
                return true;
            }

            // 兼容处理秒杀数据退款
            // 秒杀商品回滚库存和销量
            if(null != storeOrder.getSeckillId() && storeOrder.getSeckillId() > 0){
                // 秒杀只会有一个商品
                StoreOrderInfo orderInfo = orderInfoList.get(0);
                StoreSeckill storeSeckill = storeSeckillService.getByIdException(storeOrder.getSeckillId());
                storeSeckillService.operationStock(storeOrder.getSeckillId(), orderInfo.getPayNum(), "add");
                StoreProductAttrValue seckillAttrValue = attrValueService.getByIdAndProductIdAndType(orderInfo.getAttrValueId(), storeSeckill.getId(), Constants.PRODUCT_TYPE_SECKILL);
                attrValueService.operationStock(seckillAttrValue.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_SECKILL, seckillAttrValue.getVersion());
                StoreProduct storeProduct = storeProductService.getById(storeSeckill.getProductId());
                storeProductService.operationStock(storeProduct.getId(), orderInfo.getPayNum(), "add", storeProduct.getVersion());
                List<StoreProductAttrValue> attrValueList = attrValueService.getListByProductIdAndType(storeSeckill.getProductId(), Constants.PRODUCT_TYPE_NORMAL);
                attrValueList.forEach(e -> {
                    if (e.getSuk().equals(orderInfo.getSku())) {
                        attrValueService.operationStock(e.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_NORMAL, e.getVersion());
                    }
                });
            } else if (ObjectUtil.isNotNull(storeOrder.getBargainId()) && storeOrder.getBargainId() > 0) { // 砍价商品回滚销量库存
                StoreOrderInfo orderInfo = orderInfoList.get(0);
                StoreBargain storeBargain = storeBargainService.getByIdException(storeOrder.getBargainId());
                storeBargainService.operationStock(storeBargain.getId(), orderInfo.getPayNum(), "add");
                StoreProductAttrValue bargainAttrValue = attrValueService.getByIdAndProductIdAndType(orderInfo.getAttrValueId(), storeBargain.getId(), Constants.PRODUCT_TYPE_BARGAIN);
                attrValueService.operationStock(bargainAttrValue.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_BARGAIN, bargainAttrValue.getVersion());
                StoreProduct storeProduct = storeProductService.getById(storeBargain.getProductId());
                storeProductService.operationStock(storeProduct.getId(), orderInfo.getPayNum(), "add", storeProduct.getVersion());
                List<StoreProductAttrValue> attrValueList = attrValueService.getListByProductIdAndType(storeBargain.getProductId(), Constants.PRODUCT_TYPE_NORMAL);
                attrValueList.forEach(e -> {
                    if (e.getSuk().equals(orderInfo.getSku())) {
                        attrValueService.operationStock(e.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_NORMAL, e.getVersion());
                    }
                });
            } else if (ObjectUtil.isNotNull(storeOrder.getCombinationId()) && storeOrder.getCombinationId() > 0) { // 拼团商品回滚销量库存
                StoreOrderInfo orderInfo = orderInfoList.get(0);
                StoreCombination storeCombination = storeCombinationService.getByIdException(storeOrder.getCombinationId());
                storeCombinationService.operationStock(storeCombination.getId(), orderInfo.getPayNum(), "add");
                StoreProductAttrValue combinationAttrValue = attrValueService.getByIdAndProductIdAndType(orderInfo.getAttrValueId(), storeCombination.getId(), Constants.PRODUCT_TYPE_PINGTUAN);
                attrValueService.operationStock(combinationAttrValue.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_PINGTUAN, combinationAttrValue.getVersion());
                StoreProduct storeProduct = storeProductService.getById(storeCombination.getProductId());
                storeProductService.operationStock(storeProduct.getId(), orderInfo.getPayNum(), "add", storeProduct.getVersion());
                List<StoreProductAttrValue> attrValueList = attrValueService.getListByProductIdAndType(storeCombination.getProductId(), Constants.PRODUCT_TYPE_NORMAL);
                attrValueList.forEach(e -> {
                    if (e.getSuk().equals(orderInfo.getSku())) {
                        attrValueService.operationStock(e.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_NORMAL, e.getVersion());
                    }
                });
            } else if (storeOrder.getType().equals(1)) {// 视频订单自动取消
                // 获取视频订单信息
                for (StoreOrderInfo orderInfoVo : orderInfoList) {
                    Integer payNum = orderInfoVo.getPayNum();
                    Integer attrValueId = orderInfoVo.getAttrValueId();
                    PayComponentProduct componentProduct = componentProductService.getById(orderInfoVo.getProductId());
//                    componentProduct.setSales(componentProduct.getSales() - payNum);
                    PayComponentProductSku productSku = componentProductSkuService.getByProIdAndAttrValueId(orderInfoVo.getProductId(), attrValueId);
//                    productSku.setStockNum(productSku.getStockNum() + payNum);
                    StoreProductAttrValue productAttrValue = attrValueService.getById(attrValueId);
//                    productAttrValue.setStock(productAttrValue.getStock() + payNum);
//                    productAttrValue.setSales(productAttrValue.getSales() - payNum);
//                    componentProductService.updateById(componentProduct);
//                    componentProductSkuService.updateById(productSku);
//                    attrValueService.updateById(productAttrValue);

                    componentProductSkuService.operationStock(productSku.getId(), payNum, "add", productSku.getVersion());
                    componentProductService.operationStock(componentProduct.getId(), payNum, "add");
                    attrValueService.operationStock(productAttrValue.getId(), payNum, "add", Constants.PRODUCT_TYPE_COMPONENT, productAttrValue.getVersion());
                }
                return true;

            } else { // 正常商品回滚销量库存
                for (StoreOrderInfo orderInfoVo : orderInfoList) {
                    StoreProduct storeProduct = storeProductService.getById(orderInfoVo.getProductId());
                    storeProductService.operationStock(storeProduct.getId(), orderInfoVo.getPayNum(), "add", storeProduct.getVersion());
                    StoreProductAttrValue productAttrValue = attrValueService.getById(orderInfoVo.getAttrValueId());
                    attrValueService.operationStock(productAttrValue.getId(), orderInfoVo.getPayNum(), "add", Constants.PRODUCT_TYPE_NORMAL, productAttrValue.getVersion());
                }
            }
        } catch (Exception e) {
            logger.error("回滚库存失败，error = " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 订单退款处理
     * 退款得时候根据userBill 来进行回滚
     */
    @Override
    public Boolean refundOrder(StoreOrder storeOrder) {
        /**
         * 1、写订单日志
         * 2、回滚消耗积分
         * 3、回滚获得积分
         * 4、回滚冻结期佣金
         * 5、回滚经验
         * 6、回滚库存
         * 7、发送通知
         * 实际上2-5就是user数据的处理+userBill的记录
         */
        // 获取用户对象
        User user = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(user)) {
            logger.error("订单退款处理，对应的用户不存在,storeOrder===>" + storeOrder);
            return Boolean.FALSE;
        }

        // 回滚经验
        UserExperienceRecord userExperienceRecord = userExperienceRecordService.getByOrderNoAndUid(storeOrder.getOrderId(), storeOrder.getUid());
        user.setExperience(user.getExperience() - userExperienceRecord.getExperience());

        UserExperienceRecord experienceRecord = new UserExperienceRecord();
        BeanUtils.copyProperties(userExperienceRecord, experienceRecord);
        experienceRecord.setId(null);
        experienceRecord.setTitle(ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_REFUND);
        experienceRecord.setType(ExperienceRecordConstants.EXPERIENCE_RECORD_TYPE_SUB);
        experienceRecord.setBalance(user.getExperience());
        experienceRecord.setMark(StrUtil.format("订单退款，扣除{}赠送经验", userExperienceRecord.getExperience()));
        experienceRecord.setCreateTime(cn.hutool.core.date.DateUtil.date());

        // 回滚积分
        List<UserIntegralRecord> integralRecordList = userIntegralRecordService.findListByOrderIdAndUid(storeOrder.getOrderId(), storeOrder.getUid());
        integralRecordList.forEach(record -> {
            if (record.getType().equals(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_SUB)) {// 订单抵扣部分
                user.setIntegral(user.getIntegral() + record.getIntegral());
                record.setId(null);
                record.setTitle(IntegralRecordConstants.BROKERAGE_RECORD_TITLE_REFUND);
                record.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD);
                record.setBalance(user.getIntegral());
                record.setMark(StrUtil.format("订单退款，返还支付扣除得{}积分", record.getIntegral()));
                record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
                record.setUpdateTime(cn.hutool.core.date.DateUtil.date());
            } else if (record.getType().equals(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD)) {// 冻结积分部分
                record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_INVALIDATION);
                record.setUpdateTime(cn.hutool.core.date.DateUtil.date());
            }
        });
        List<UserIntegralRecord> addIntegralList = integralRecordList.stream().filter(e -> ObjectUtil.isNull(e.getId())).collect(Collectors.toList());
        List<UserIntegralRecord> updateIntegralList = integralRecordList.stream().filter(e -> ObjectUtil.isNotNull(e.getId())).collect(Collectors.toList());

        StoreOrder tempOrder = new StoreOrder();
        tempOrder.setId(storeOrder.getId());
        tempOrder.setRefundStatus(2);
        // 佣金处理：只处理冻结期佣金
        // 查询佣金记录
        List<UserBrokerageRecord> brokerageRecordList = CollUtil.newArrayList();
        List<UserBrokerageRecord> recordList = userBrokerageRecordService.findListByLinkIdAndLinkType(storeOrder.getOrderId(), BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        if (CollUtil.isNotEmpty(recordList)) {
            recordList.forEach(r -> {
                //创建、冻结期佣金置为失效状态
                if (r.getStatus() < BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE) {
                    r.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_INVALIDATION);
                    brokerageRecordList.add(r);
                }
            });
        }

        Boolean execute = transactionTemplate.execute(e -> {
            //写订单日志
            storeOrderStatusService.saveRefund(storeOrder.getId(), storeOrder.getRefundPrice(), "成功");

            // 更新用户数据
            userService.updateById(user);

            // 积分部分
            if (CollUtil.isNotEmpty(addIntegralList)) {
                userIntegralRecordService.saveBatch(addIntegralList);
            }
            if (CollUtil.isNotEmpty(updateIntegralList)) {
                userIntegralRecordService.updateBatchById(updateIntegralList);
            }

            // 佣金处理
            if (CollUtil.isNotEmpty(brokerageRecordList)) {
                userBrokerageRecordService.updateBatchById(brokerageRecordList);
            }

            // 经验处理
            userExperienceRecordService.save(experienceRecord);
            userLevelService.downLevel(user);

            // 回滚库存
            Boolean rollbackStock = rollbackStock(storeOrder);
            if (!rollbackStock) {
                throw new CrmebException("回滚库存失败");
            }

            storeOrderService.updateById(tempOrder);

            // 拼团状态处理
            if (storeOrder.getCombinationId() > 0) {
                StorePink storePink = storePinkService.getByOrderId(storeOrder.getOrderId());
                storePink.setStatus(3);
                storePink.setIsRefund(true);
                storePinkService.updateById(storePink);
            }

            // 退优惠券
            if (storeOrder.getCouponId() > 0 ) {
                StoreCouponUser couponUser = couponUserService.getById(storeOrder.getCouponId());
                couponUser.setStatus(CouponConstants.STORE_COUPON_USER_STATUS_USABLE);
                couponUserService.updateById(couponUser);
            }
            return Boolean.TRUE;
        });

        // 视频号订单创建售后
        try {
            componentOrderService.createAfterSale(storeOrder.getOrderId());
        } catch (Exception e) {
            e.printStackTrace();
            // 视频号订单售后部分失败
            logger.error("视频号订单售后部分处理失败，message = " + e.getMessage());
        }
        return execute;
    }

    /**
     * 超时未支付系统自动取消
     */
    @Override
    public Boolean autoCancel(StoreOrder storeOrder) {
        // 判断订单是否支付
        if (storeOrder.getPaid()) {
            return Boolean.TRUE;
        }
        if (storeOrder.getIsDel() || storeOrder.getIsSystemDel()) {
            return Boolean.TRUE;
        }
        // 获取过期时间
        String cancelStr;
        DateTime cancelTime;
        if (storeOrder.getType().equals(1)) {
            cancelStr = "3";
            cancelTime = cn.hutool.core.date.DateUtil.offset(storeOrder.getCreateTime(), DateField.MINUTE, Integer.parseInt(cancelStr));
        } else {
            if (storeOrder.getBargainId() > 0 || storeOrder.getSeckillId() > 0 || storeOrder.getCombinationId() > 0) {
                cancelStr = systemConfigService.getValueByKey("order_activity_time");
            } else {
                cancelStr = systemConfigService.getValueByKey("order_cancel_time");
            }
            if (StrUtil.isBlank(cancelStr)) {
                cancelStr = "1";
            }
            cancelTime = cn.hutool.core.date.DateUtil.offset(storeOrder.getCreateTime(), DateField.HOUR_OF_DAY, Integer.parseInt(cancelStr));
        }
        long between = cn.hutool.core.date.DateUtil.between(cancelTime, cn.hutool.core.date.DateUtil.date(), DateUnit.SECOND, false);
        if (between < 0) {// 未到过期时间继续循环
            return Boolean.FALSE;
        }
        storeOrder.setIsDel(true).setIsSystemDel(true);
        Boolean execute = false;

        if (storeOrder.getType().equals(1)) {
            // 视频订单修改状态
            PayComponentOrder componentOrder = componentOrderService.getByOrderNo(storeOrder.getOrderId());
            ShopOrderPayVo shopOrderPayVo = new ShopOrderPayVo();
            shopOrderPayVo.setOutOrderId(componentOrder.getOrderNo());
            shopOrderPayVo.setOpenid(componentOrder.getOpenid());
            shopOrderPayVo.setActionType(4);// 超时未支付
            Boolean shopOrderPay = wechatVideoOrderService.shopOrderPay(shopOrderPayVo);
            if (!shopOrderPay) {
                logger.error("视频号订单自动取消失败，订单号 = " + storeOrder.getOrderId());
                return Boolean.FALSE;
            }
            componentOrder.setStatus(250);
            execute = transactionTemplate.execute(e -> {
                storeOrderService.updateById(storeOrder);
                componentOrderService.updateById(componentOrder);
                //写订单日志
                storeOrderStatusService.createLog(storeOrder.getId(), "cancel", "到期未支付系统自动取消");
                // 回滚库存
                Boolean rollbackStock = rollbackStock(storeOrder);
                if (!rollbackStock) {
                    throw new CrmebException("回滚库存失败");
                }
                return Boolean.TRUE;
            });
        } else {
            execute = transactionTemplate.execute(e -> {
                storeOrderService.updateById(storeOrder);
                //写订单日志
                storeOrderStatusService.createLog(storeOrder.getId(), "cancel", "到期未支付系统自动取消");
                // 退优惠券
                if (storeOrder.getCouponId() > 0 ) {
                    StoreCouponUser couponUser = couponUserService.getById(storeOrder.getCouponId());
                    couponUser.setStatus(CouponConstants.STORE_COUPON_USER_STATUS_USABLE);
                    couponUserService.updateById(couponUser);
                }
                // 回滚库存
                Boolean rollbackStock = rollbackStock(storeOrder);
                if (!rollbackStock) {
                    throw new CrmebException("回滚库存失败");
                }
                return Boolean.TRUE;
            });
        }
        return execute;
    }

    /**
     * 订单收货task处理
     * @param orderId 订单id
     * @return Boolean
     * 1.写订单日志
     * 2.分佣-佣金进入冻结期
     */
    @Override
    public Boolean orderReceiving(Integer orderId) {
        StoreOrder storeOrder = storeOrderService.getById(orderId);
        if (ObjectUtil.isNull(storeOrder)) {
            throw new CrmebException(StrUtil.format("订单收货task处理，未找到订单，id={}", orderId));
        }
        User user = userService.getById(storeOrder.getUid());

        // 获取佣金记录
        List<UserBrokerageRecord> recordList = userBrokerageRecordService.findListByLinkIdAndLinkType(storeOrder.getOrderId(), BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        logger.info("收货处理佣金条数：" + recordList.size());
        for (UserBrokerageRecord record : recordList) {
            if (!record.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE)) {
                throw new CrmebException(StrUtil.format("订单收货task处理，订单佣金记录不是创建状态，id={}", orderId));
            }
            // 佣金进入冻结期
            record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_FROZEN);
            // 计算解冻时间
            Long thawTime = cn.hutool.core.date.DateUtil.current(false);
            if (record.getFrozenTime() > 0) {
                DateTime dateTime = cn.hutool.core.date.DateUtil.offsetDay(new Date(), record.getFrozenTime());
                thawTime = dateTime.getTime();
            }
            record.setThawTime(thawTime);
        }

        // 获取积分记录
        List<UserIntegralRecord> integralRecordList = userIntegralRecordService.findListByOrderIdAndUid(storeOrder.getOrderId(), storeOrder.getUid());
        logger.info("收货处理积分条数：" + integralRecordList.size());
        List<UserIntegralRecord> userIntegralRecordList = integralRecordList.stream().filter(e -> e.getType().equals(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD)).collect(Collectors.toList());
        for (UserIntegralRecord record : userIntegralRecordList) {
//            if (!record.getStatus().equals(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_CREATE)) {
//                throw new CrmebException(StrUtil.format("订单收货task处理，订单积分记录不是创建状态，id={}", orderId));
//            }
            // 佣金进入冻结期
            record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_FROZEN);
            // 计算解冻时间
            Long thawTime = cn.hutool.core.date.DateUtil.current(false);
            if (record.getFrozenTime() > 0) {
                DateTime dateTime = cn.hutool.core.date.DateUtil.offsetDay(new Date(), record.getFrozenTime());
                thawTime = dateTime.getTime();
            }
            record.setThawTime(thawTime);
        }

        Boolean execute = transactionTemplate.execute(e -> {
            // 日志
            storeOrderStatusService.createLog(storeOrder.getId(), "user_take_delivery", Constants.ORDER_STATUS_STR_TAKE);
            // 分佣-佣金进入冻结期
            if (CollUtil.isNotEmpty(recordList)) {
                userBrokerageRecordService.updateBatchById(recordList);
            }
            // 积分进入冻结期
            if (CollUtil.isNotEmpty(userIntegralRecordList)) {
                userIntegralRecordService.updateBatchById(userIntegralRecordList);
            }
            return Boolean.TRUE;
        });
        if (execute) {
            // 发送用户确认收货管理员提醒短信
            SystemNotification notification = systemNotificationService.getByMark(NotifyConstants.RECEIPT_GOODS_ADMIN_MARK);
            if (notification.getIsSms().equals(1)) {
                // 查询可已发送短信的管理员
                List<SystemAdmin> systemAdminList = systemAdminService.findIsSmsList();
                if (CollUtil.isNotEmpty(systemAdminList)) {
                    SmsTemplate smsTemplate = smsTemplateService.getDetail(notification.getSmsId());
                    Integer tempId = Integer.valueOf(smsTemplate.getTempId());
                    // 发送短信
                    systemAdminList.forEach(admin -> {
                        smsService.sendOrderReceiptNotice(admin.getPhone(), storeOrder.getOrderId(), admin.getRealName(), tempId);
                    });
                }
            }

            // 发送消息通知
            pushMessageOrder(storeOrder, user);
        }

        return execute;
    }

    /**
     * 发送消息通知
     * 根据用户类型发送
     * 公众号模板消息
     * 小程序订阅消息
     */
    private void pushMessageOrder(StoreOrder storeOrder, User user) {
        SystemNotification notification = systemNotificationService.getByMark(NotifyConstants.RECEIPT_GOODS_MARK);
        if (storeOrder.getIsChannel().equals(2)) {
            return;
        }
        if (!storeOrder.getPayType().equals(Constants.PAY_TYPE_WE_CHAT)) {
            return;
        }
        UserToken userToken;
        HashMap<String, String> temMap = new HashMap<>();
        // 公众号
        if (storeOrder.getIsChannel().equals(Constants.ORDER_PAY_CHANNEL_PUBLIC) && notification.getIsWechat().equals(1)) {
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_WECHAT);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 发送微信模板消息
            temMap.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "您购买的商品已确认收货！");
            temMap.put("keyword1", storeOrder.getOrderId());
            temMap.put("keyword2", "已收货");
            temMap.put("keyword3", CrmebDateUtil.nowDateTimeStr());
            temMap.put("keyword4", "详情请进入订单查看");
            temMap.put(Constants.WE_CHAT_TEMP_KEY_END, "感谢你的使用。");
            templateMessageService.pushTemplateMessage(notification.getWechatId(), temMap, userToken.getToken());
        } else if (notification.getIsRoutine().equals(1)) {
            // 小程序发送订阅消息
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 组装数据
            // 获取商品名称
            String storeNameAndCarNumString = orderUtils.getStoreNameAndCarNumString(storeOrder.getId());
            if (StrUtil.isBlank(storeNameAndCarNumString)) {
                return ;
            }
            if (storeNameAndCarNumString.length() > 20) {
                storeNameAndCarNumString = storeNameAndCarNumString.substring(0, 15) + "***";
            }
//        temMap.put("character_string6", storeOrder.getOrderId());
//        temMap.put("phrase4", "已收货");
//        temMap.put("time7", DateUtil.nowDateTimeStr());
//        temMap.put("thing1", storeNameAndCarNumString);
//        temMap.put("thing5", "您购买的商品已确认收货！");
            temMap.put("character_string6", storeOrder.getOrderId());
            temMap.put("date5", CrmebDateUtil.nowDateTimeStr());
            temMap.put("thing2", storeNameAndCarNumString);
            templateMessageService.pushMiniTemplateMessage(notification.getRoutineId(), temMap, userToken.getToken());
        }
    }
}
