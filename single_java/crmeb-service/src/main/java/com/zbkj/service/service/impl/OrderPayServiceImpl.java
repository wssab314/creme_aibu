package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.zbkj.common.constants.*;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.combination.StoreCombination;
import com.zbkj.common.model.combination.StorePink;
import com.zbkj.common.model.coupon.StoreCouponUser;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.order.StoreOrderInfo;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.product.StoreProductAttrValue;
import com.zbkj.common.model.product.StoreProductCoupon;
import com.zbkj.common.model.sms.SmsTemplate;
import com.zbkj.common.model.system.SystemAdmin;
import com.zbkj.common.model.system.SystemNotification;
import com.zbkj.common.model.user.*;
import com.zbkj.common.model.wechat.video.PayComponentOrder;
import com.zbkj.common.model.wechat.video.PayComponentProduct;
import com.zbkj.common.model.wechat.video.PayComponentProductSku;
import com.zbkj.common.request.OrderPayRequest;
import com.zbkj.common.response.OrderPayResultResponse;
import com.zbkj.common.response.PayConfigResponse;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.WxPayUtil;
import com.zbkj.common.vo.*;
import com.zbkj.service.delete.OrderUtils;
import com.zbkj.service.service.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * OrderPayService 实现类
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
@Service
public class OrderPayServiceImpl implements OrderPayService {
    private static final Logger logger = LoggerFactory.getLogger(OrderPayServiceImpl.class);

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderStatusService storeOrderStatusService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Lazy
    @Autowired
    private WeChatPayService weChatPayService;

    @Autowired
    private TemplateMessageService templateMessageService;

    @Autowired
    private UserBillService userBillService;

    @Lazy
    @Autowired
    private SmsService smsService;

    @Autowired
    private UserService userService;

    @Autowired
    private StoreProductCouponService storeProductCouponService;

    @Autowired
    private StoreCouponUserService storeCouponUserService;

    @Autowired
    private OrderUtils orderUtils;

    //订单类
    private StoreOrder order;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private StoreBargainService storeBargainService;

    @Autowired
    private StoreBargainUserService storeBargainUserService;

    @Autowired
    private StoreCombinationService storeCombinationService;

    @Autowired
    private StorePinkService storePinkService;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private StoreCouponService storeCouponService;

    @Autowired
    private SystemAdminService systemAdminService;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private UserIntegralRecordService userIntegralRecordService;

    @Autowired
    private StoreProductAttrValueService storeProductAttrValueService;

    @Autowired
    private PayComponentOrderService componentOrderService;

    @Autowired
    private PayComponentProductSkuService componentProductSkuService;

    @Autowired
    private PayComponentProductService componentProductService;

    @Autowired
    private WechatVideoOrderService wechatVideoOrderService;

    @Autowired
    private WechatNewService wechatNewService;

    @Autowired
    private UserExperienceRecordService userExperienceRecordService;

    @Autowired
    private YlyPrintService ylyPrintService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    @Autowired
    private SmsTemplateService smsTemplateService;

    /**
     * 获取支付配置
     */
    @Override
    public PayConfigResponse getPayConfig() {
        String payWxOpen = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_PAY_WEIXIN_OPEN);
        String yuePayStatus = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_YUE_PAY_STATUS);
        String aliPayStatus = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_ALI_PAY_STATUS);
        PayConfigResponse response = new PayConfigResponse();
        response.setYuePayStatus(Constants.CONFIG_FORM_SWITCH_OPEN.equals(yuePayStatus));
        response.setPayWechatOpen(Constants.CONFIG_FORM_SWITCH_OPEN.equals(payWxOpen));
        response.setAliPayStatus(Constants.CONFIG_FORM_SWITCH_OPEN.equals(aliPayStatus));
        if (Constants.CONFIG_FORM_SWITCH_OPEN.equals(yuePayStatus)) {
            User user = userService.getInfo();
            response.setUserBalance(user.getNowMoney());
        }
        return response;
    }

    /**
     * 支付成功处理
     * @param storeOrder 订单
     */
    @Override
    public Boolean paySuccess(StoreOrder storeOrder) {
        User user = userService.getById(storeOrder.getUid());

        List<UserBill> billList = CollUtil.newArrayList();
        List<UserIntegralRecord> integralList = CollUtil.newArrayList();

        // 订单支付记录
        UserBill userBill = userBillInit(storeOrder, user);
        billList.add(userBill);

        // 积分抵扣记录
        if (storeOrder.getUseIntegral() > 0) {
            UserIntegralRecord integralRecordSub = integralRecordSubInit(storeOrder, user);
            integralList.add(integralRecordSub);
        }

        // 经验处理：1.经验添加，2.等级计算
        Integer experience;
        experience = storeOrder.getPayPrice().setScale(0, BigDecimal.ROUND_DOWN).intValue();
        user.setExperience(user.getExperience() + experience);
        // 经验添加记录
        UserExperienceRecord experienceRecord = experienceRecordInit(storeOrder, user.getExperience(), experience);


        // 积分处理：1.下单赠送积分，2.商品赠送积分
        int integral;
        // 下单赠送积分
        //赠送积分比例
        String integralStr = systemConfigService.getValueByKey(Constants.CONFIG_KEY_INTEGRAL_RATE_ORDER_GIVE);
        if (StrUtil.isNotBlank(integralStr) && storeOrder.getPayPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal integralBig = new BigDecimal(integralStr);
            integral = integralBig.multiply(storeOrder.getPayPrice()).setScale(0, BigDecimal.ROUND_DOWN).intValue();
            if (integral > 0) {
                // 生成积分记录
                UserIntegralRecord integralRecord = integralRecordInit(storeOrder, user.getIntegral(), integral, "order");
                integralList.add(integralRecord);
            }
        }

        // 商品赠送积分
        // 查询订单详情
        // 获取商品额外赠送积分
        List<StoreOrderInfo> orderInfoList = storeOrderInfoService.getListByOrderNo(storeOrder.getOrderId());
        if (orderInfoList.get(0).getProductType().equals(0)) {
            int sumIntegral = 0;
            for (StoreOrderInfo orderInfo : orderInfoList) {
                StoreProduct product = storeProductService.getById(orderInfo.getProductId());
                sumIntegral += product.getGiveIntegral() * orderInfo.getPayNum();
            }
            if (sumIntegral > 0) {
                // 生成积分记录
                UserIntegralRecord integralRecord = integralRecordInit(storeOrder, user.getIntegral(), sumIntegral, "product");
                integralList.add(integralRecord);
            }
        }

        // 更新用户下单数量
        user.setPayCount(user.getPayCount() + 1);

        /**
         * 计算佣金，生成佣金记录
         */
        List<UserBrokerageRecord> recordList = assignCommission(storeOrder);

        // 分销员逻辑
        if (!user.getIsPromoter()) {
            String funcStatus = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_BROKERAGE_FUNC_STATUS);
            if (funcStatus.equals("1")) {
                String broQuota = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_STORE_BROKERAGE_QUOTA);
                if (!broQuota.equals("-1") && storeOrder.getPayPrice().compareTo(new BigDecimal(broQuota)) >= 0) {// -1 不成为分销员
                    user.setIsPromoter(true);
                    user.setPromoterTime(cn.hutool.core.date.DateUtil.date());
                }
            }
        }

        Boolean execute = transactionTemplate.execute(e -> {
            //订单日志
            storeOrderStatusService.createLog(storeOrder.getId(), Constants.ORDER_LOG_PAY_SUCCESS, Constants.ORDER_LOG_MESSAGE_PAY_SUCCESS);

            // 用户信息变更
            userService.updateById(user);

            //资金变动
            userBillService.saveBatch(billList);

            // 积分记录
            userIntegralRecordService.saveBatch(integralList);

            // 经验记录
            userExperienceRecordService.save(experienceRecord);

            //经验升级
            userLevelService.upLevel(user);

            // 佣金记录
            if (CollUtil.isNotEmpty(recordList)) {
                recordList.forEach(temp -> {
                    temp.setLinkId(storeOrder.getOrderId());
                });
                userBrokerageRecordService.saveBatch(recordList);
            }

            // 如果是拼团订单进行拼团后置处理
            if (storeOrder.getCombinationId() > 0) {
                pinkProcessing(storeOrder);
            }
            return Boolean.TRUE;
        });

        if (execute) {
            if (storeOrder.getType().equals(1)) {// 视频订单
                // 同步给微信订单支付结果
                PayComponentOrder componentOrder = componentOrderService.getByOrderNo(storeOrder.getOrderId());
                if (ObjectUtil.isNull(componentOrder)) {
                    throw new CrmebException("组件订单未找到,订单号 = " + storeOrder.getOrderId());
                }
                ShopOrderPayVo shopOrderPayVo = new ShopOrderPayVo();
                shopOrderPayVo.setOutOrderId(storeOrder.getOrderId());
                shopOrderPayVo.setOpenid(componentOrder.getOpenid());
                shopOrderPayVo.setActionType(1);
                shopOrderPayVo.setTransactionId(componentOrder.getTransactionId());
                DateTime dateTime = cn.hutool.core.date.DateUtil.parse(componentOrder.getTimeEnd(), "yyyyMMddHHmmss");
                shopOrderPayVo.setPayTime(dateTime.toString());
                Boolean shopOrderPay = wechatVideoOrderService.shopOrderPay(shopOrderPayVo);
                if (!shopOrderPay) {
                    return false;
                }
                componentOrder.setStatus(20);
                componentOrderService.updateById(componentOrder);
            }
            try {
                SystemNotification payNotification = systemNotificationService.getByMark(NotifyConstants.PAY_SUCCESS_MARK);
                // 发送短信
                if (StrUtil.isNotBlank(user.getPhone()) && payNotification.getIsSms().equals(1)) {
                    SmsTemplate smsTemplate = smsTemplateService.getDetail(payNotification.getSmsId());
                    smsService.sendPaySuccess(user.getPhone(), storeOrder.getOrderId(), storeOrder.getPayPrice(), Integer.valueOf(smsTemplate.getTempId()));
                }

                // 发送用户支付成功管理员提醒短信
                SystemNotification payAdminNotification = systemNotificationService.getByMark(NotifyConstants.PAY_SUCCESS_ADMIN_MARK);
                if (payAdminNotification.getIsSms().equals(1)) {
                    // 查询可已发送短信的管理员
                    List<SystemAdmin> systemAdminList = systemAdminService.findIsSmsList();
                    if (CollUtil.isNotEmpty(systemAdminList)) {
                        SmsTemplate smsTemplate = smsTemplateService.getDetail(payAdminNotification.getSmsId());
                        // 发送短信
                        systemAdminList.forEach(admin -> {
                            smsService.sendOrderPaySuccessNotice(admin.getPhone(), storeOrder.getOrderId(), admin.getRealName(), Integer.valueOf(smsTemplate.getTempId()));
                        });
                    }
                }

                if (payNotification.getIsWechat().equals(1) || payNotification.getIsRoutine().equals(1)) {
                    //下发模板通知
                    pushMessageOrder(storeOrder, user, payNotification);
                }

                // 购买成功后根据配置送优惠券
                autoSendCoupons(storeOrder);

                // 根据配置 打印小票
                ylyPrintService.YlyPrint(storeOrder.getOrderId(),true);

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("短信、模板通知、优惠券或打印小票异常");
            }
        }
        return execute;
    }

    // 支持成功拼团后置处理
    private Boolean pinkProcessing(StoreOrder storeOrder) {
        // 判断拼团是否成功
        StorePink storePink = storePinkService.getById(storeOrder.getPinkId());
        if (storePink.getKId() <= 0) {
            return true;
        }

        List<StorePink> pinkList = storePinkService.getListByCidAndKid(storePink.getCid(), storePink.getKId());
        StorePink tempPink = storePinkService.getById(storePink.getKId());
        pinkList.add(tempPink);
        if (pinkList.size() < storePink.getPeople()) {// 还未拼团成功
            return true;
        }
        // 1.修改拼团状态
        // 2.给所有拼团人员发送拼团成功通知
        pinkList.forEach(e -> {
            e.setStatus(2);
        });
        boolean update = storePinkService.updateBatchById(pinkList);
        if (!update) {
            logger.error("拼团订单支付成功后更新拼团状态失败,orderNo = " + storeOrder.getOrderId());
            return false;
        }
        SystemNotification notification = systemNotificationService.getByMark(NotifyConstants.GROUP_SUCCESS_MARK);
        if (notification.getIsWechat().equals(1) || notification.getIsRoutine().equals(1)) {
            pinkList.forEach(i -> {
                StoreOrder order = storeOrderService.getByOderId(i.getOrderId());
                StoreCombination storeCombination = storeCombinationService.getById(i.getCid());
                User tempUser = userService.getById(i.getUid());
                // 发送微信模板消息
                MyRecord record = new MyRecord();
                record.set("orderNo", order.getOrderId());
                record.set("proName", storeCombination.getTitle());
                record.set("payType", order.getPayType());
                record.set("isChannel", order.getIsChannel());
                pushMessagePink(record, tempUser, notification);
            });
        }
        return true;
    }

    /**
     * 发送拼团成功通知
     * @param record 信息参数
     * @param user 用户
     */
    private void pushMessagePink(MyRecord record, User user, SystemNotification notification) {
        if (!record.getStr("payType").equals(Constants.PAY_TYPE_WE_CHAT)) {
            return ;
        }
        if (record.getInt("isChannel").equals(2)) {
            return ;
        }

        UserToken userToken;
        HashMap<String, String> temMap = new HashMap<>();
        // 公众号
        if (record.getInt("isChannel").equals(Constants.ORDER_PAY_CHANNEL_PUBLIC) && notification.getIsWechat().equals(1)) {
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_WECHAT);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 发送微信模板消息
            temMap.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "恭喜您拼团成功！我们将尽快为您发货。");
            temMap.put("keyword1", record.getStr("orderNo"));
            temMap.put("keyword2", record.getStr("proName"));
            temMap.put(Constants.WE_CHAT_TEMP_KEY_END, "感谢你的使用！");
            templateMessageService.pushTemplateMessage(notification.getWechatId(), temMap, userToken.getToken());
        } else if (notification.getIsRoutine().equals(1)) {
            // 小程序发送订阅消息
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 组装数据
//        temMap.put("character_string1",  record.getStr("orderNo"));
//        temMap.put("thing2", record.getStr("proName"));
//        temMap.put("thing5", "恭喜您拼团成功！我们将尽快为您发货。");
            temMap.put("character_string10",  record.getStr("orderNo"));
            temMap.put("thing7", record.getStr("proName"));
            temMap.put("thing9", "恭喜您拼团成功！我们将尽快为您发货。");
            templateMessageService.pushMiniTemplateMessage(notification.getRoutineId(), temMap, userToken.getToken());
        }

    }

    /**
     * 分配佣金
     * @param storeOrder 订单
     * @return List<UserBrokerageRecord>
     */
    private List<UserBrokerageRecord> assignCommission(StoreOrder storeOrder) {
        // 检测商城是否开启分销功能
        String isOpen = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_IS_OPEN);
        if(StrUtil.isBlank(isOpen) || isOpen.equals("0")){
            return CollUtil.newArrayList();
        }
        // 营销产品不参与
        if(storeOrder.getCombinationId() > 0 || storeOrder.getSeckillId() > 0 || storeOrder.getBargainId() > 0){
            return CollUtil.newArrayList();
        }
        // 查找订单所属人信息
        User user = userService.getById(storeOrder.getUid());
        // 当前用户不存在 没有上级 或者 当用用户上级时自己  直接返回
        if(null == user.getSpreadUid() || user.getSpreadUid() < 1 || user.getSpreadUid().equals(storeOrder.getUid())){
            return CollUtil.newArrayList();
        }
        // 获取参与分佣的人（两级）
        List<MyRecord> spreadRecordList = getSpreadRecordList(user.getSpreadUid());
        if (CollUtil.isEmpty(spreadRecordList)) {
            return CollUtil.newArrayList();
        }
        // 获取佣金冻结期
        String fronzenTime = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_EXTRACT_TIME);

        // 生成佣金记录
        List<UserBrokerageRecord> brokerageRecordList = spreadRecordList.stream().map(record -> {
            BigDecimal brokerage = calculateCommission(record, storeOrder.getId());
            UserBrokerageRecord brokerageRecord = new UserBrokerageRecord();
            brokerageRecord.setUid(record.getInt("spreadUid"));
            brokerageRecord.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
            brokerageRecord.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
            brokerageRecord.setTitle(BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_ORDER);
            brokerageRecord.setPrice(brokerage);
            brokerageRecord.setMark(StrUtil.format("获得推广佣金，分佣{}", brokerage));
            brokerageRecord.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
            brokerageRecord.setFrozenTime(Integer.valueOf(Optional.ofNullable(fronzenTime).orElse("0")));
            brokerageRecord.setCreateTime(CrmebDateUtil.nowDateTime());
            brokerageRecord.setBrokerageLevel(record.getInt("index"));
            return brokerageRecord;
        }).collect(Collectors.toList());

        return brokerageRecordList;
    }

    /**
     * 计算佣金
     * @param record index-分销级数，spreadUid-分销人
     * @param orderId 订单id
     * @return BigDecimal
     */
    private BigDecimal calculateCommission(MyRecord record, Integer orderId) {
        BigDecimal brokeragePrice = BigDecimal.ZERO;
        // 查询订单详情
        List<StoreOrderInfoOldVo> orderInfoVoList = storeOrderInfoService.getOrderListByOrderId(orderId);
        if (CollUtil.isEmpty(orderInfoVoList)) {
            return brokeragePrice;
        }
        BigDecimal totalBrokerPrice = BigDecimal.ZERO;
        //查询对应等级的分销比例
        Integer index = record.getInt("index");
        String key = "";
        if (index == 1) {
            key = Constants.CONFIG_KEY_STORE_BROKERAGE_RATE_ONE;
        }
        if (index == 2) {
            key = Constants.CONFIG_KEY_STORE_BROKERAGE_RATE_TWO;
        }
        String rate = systemConfigService.getValueByKey(key);
        if(StringUtils.isBlank(rate)){
            rate = "1";
        }
        //佣金比例整数存储， 例如80， 所以计算的时候要除以 10*10
        BigDecimal rateBigDecimal = brokeragePrice;
        if(StringUtils.isNotBlank(rate)){
            rateBigDecimal = new BigDecimal(rate).divide(BigDecimal.TEN.multiply(BigDecimal.TEN));
        }

        for (StoreOrderInfoOldVo orderInfoVo : orderInfoVoList) {
            // 先看商品是否有固定分佣
            StoreProductAttrValue attrValue = storeProductAttrValueService.getById(orderInfoVo.getInfo().getAttrValueId());
            if (orderInfoVo.getInfo().getIsSub()) {// 有固定分佣
                if(index == 1){
                    brokeragePrice = Optional.ofNullable(attrValue.getBrokerage()).orElse(BigDecimal.ZERO);
                }
                if(index == 2){
                    brokeragePrice = Optional.ofNullable(attrValue.getBrokerageTwo()).orElse(BigDecimal.ZERO);
                }
            } else {// 系统分佣
                if(!rateBigDecimal.equals(BigDecimal.ZERO)){
                    // 商品没有分销金额, 并且有设置对应等级的分佣比例
                    // 舍入模式向零舍入。
                    if (ObjectUtil.isNotNull(orderInfoVo.getInfo().getVipPrice())) {
                        brokeragePrice = orderInfoVo.getInfo().getVipPrice().multiply(rateBigDecimal).setScale(2, BigDecimal.ROUND_DOWN);
                    } else {
                        brokeragePrice = orderInfoVo.getInfo().getPrice().multiply(rateBigDecimal).setScale(2, BigDecimal.ROUND_DOWN);
                    }
                } else {
                    brokeragePrice = BigDecimal.ZERO;
                }
            }
            // 同规格商品可能有多件
            if (brokeragePrice.compareTo(BigDecimal.ZERO) > 0 && orderInfoVo.getInfo().getPayNum() > 1) {
                brokeragePrice = brokeragePrice.multiply(new BigDecimal(orderInfoVo.getInfo().getPayNum()));
            }
            totalBrokerPrice = totalBrokerPrice.add(brokeragePrice);
        }

        return totalBrokerPrice;
    }

    /**
     * 获取参与分佣人员（两级）
     * @param spreadUid 一级分佣人Uid
     * @return List<MyRecord>
     */
    private List<MyRecord> getSpreadRecordList(Integer spreadUid) {
        List<MyRecord> recordList = CollUtil.newArrayList();

        // 第一级
        User spreadUser = userService.getById(spreadUid);
        if (ObjectUtil.isNull(spreadUser)) {
            return recordList;
        }
        // 判断分销模式
        String model = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_MODEL);
        if (StrUtil.isNotBlank(model) && model.equals("1") && !spreadUser.getIsPromoter()) {
            // 指定分销模式下：不是推广员不参与分销
            return recordList;
        }
        MyRecord firstRecord = new MyRecord();
        firstRecord.set("index", 1);
        firstRecord.set("spreadUid", spreadUid);
        recordList.add(firstRecord);

        // 第二级
        User spreadSpreadUser = userService.getById(spreadUser.getSpreadUid());
        if (ObjectUtil.isNull(spreadSpreadUser)) {
            return recordList;
        }
        if (StrUtil.isNotBlank(model) && model.equals("1") && !spreadSpreadUser.getIsPromoter()) {
            // 指定分销模式下：不是推广员不参与分销
            return recordList;
        }
        MyRecord secondRecord = new MyRecord();
        secondRecord.set("index", 2);
        secondRecord.set("spreadUid", spreadSpreadUser.getUid());
        recordList.add(secondRecord);
        return recordList;
    }

    /**
     * 余额支付
     * @param storeOrder 订单
     * @return Boolean Boolean
     */
    private Boolean yuePay(StoreOrder storeOrder) {

        // 用户余额扣除
        User user = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(user)) throw new CrmebException("用户不存在");
        if (user.getNowMoney().compareTo(storeOrder.getPayPrice()) < 0) {
            throw new CrmebException("用户余额不足");
        }
        if (user.getIntegral() < storeOrder.getUseIntegral()) {
            throw new CrmebException("用户积分不足");
        }
        storeOrder.setPaid(true);
        storeOrder.setPayTime(CrmebDateUtil.nowDateTime());
        Boolean execute = transactionTemplate.execute(e -> {
            // 订单修改
            storeOrderService.updateById(storeOrder);
            // 这里只扣除金额，账单记录在task中处理
            userService.updateNowMoney(user, storeOrder.getPayPrice(), "sub");
            // 扣除积分
            if (storeOrder.getUseIntegral() > 0) {
                userService.updateIntegral(user, storeOrder.getUseIntegral(), "sub");
            }
            // 添加支付成功redis队列
            redisUtil.lPush(TaskConstants.ORDER_TASK_PAY_SUCCESS_AFTER, storeOrder.getOrderId());

            // 处理拼团
            if (storeOrder.getCombinationId() > 0) {
                // 判断拼团团长是否存在
                StorePink headPink = new StorePink();
                Integer pinkId = storeOrder.getPinkId();
                if (pinkId > 0) {
                    headPink = storePinkService.getById(pinkId);
                    if (ObjectUtil.isNull(headPink) || headPink.getIsRefund().equals(true) || headPink.getStatus() == 3) {
                        pinkId = 0;
                    }
                }
                StoreCombination storeCombination = storeCombinationService.getById(storeOrder.getCombinationId());
                // 如果拼团人数已满，重新开团
                if (pinkId > 0) {
                    Integer count = storePinkService.getCountByKid(pinkId);
                    if (count >= storeCombination.getPeople()) {
                        pinkId = 0;
                    }
                }
                // 生成拼团表数据
                StorePink storePink = new StorePink();
                storePink.setUid(user.getUid());
                storePink.setAvatar(user.getAvatar());
                storePink.setNickname(user.getNickname());
                storePink.setOrderId(storeOrder.getOrderId());
                storePink.setOrderIdKey(storeOrder.getId());
                storePink.setTotalNum(storeOrder.getTotalNum());
                storePink.setTotalPrice(storeOrder.getTotalPrice());
                storePink.setCid(storeCombination.getId());
                storePink.setPid(storeCombination.getProductId());
                storePink.setPeople(storeCombination.getPeople());
                storePink.setPrice(storeCombination.getPrice());
                Integer effectiveTime = storeCombination.getEffectiveTime();// 有效小时数
                DateTime dateTime = cn.hutool.core.date.DateUtil.date();
                storePink.setAddTime(dateTime.getTime());
                if (pinkId > 0) {
                    storePink.setStopTime(headPink.getStopTime());
                } else {
                    DateTime hourTime = cn.hutool.core.date.DateUtil.offsetHour(dateTime, effectiveTime);
                    long stopTime =  hourTime.getTime();
                    if (stopTime > storeCombination.getStopTime()) {
                        stopTime = storeCombination.getStopTime();
                    }
                    storePink.setStopTime(stopTime);
                }
                storePink.setKId(pinkId);
                storePink.setIsTpl(false);
                storePink.setIsRefund(false);
                storePink.setStatus(1);
                storePinkService.save(storePink);
                // 如果是开团，需要更新订单数据
                storeOrder.setPinkId(storePink.getId());
                storeOrderService.updateById(storeOrder);
            }

            return Boolean.TRUE;
        });
        if (!execute) throw new CrmebException("余额支付订单失败");
        return execute;
    }

    /**
     * 订单支付
     * @param orderPayRequest   支付参数
     * @param ip                ip
     * @return OrderPayResultResponse
     * 1.微信支付拉起微信预支付，返回前端调用微信支付参数，在之后需要调用微信支付查询接口
     * 2.余额支付，更改对应信息后，加入支付成功处理task
     */
    @Override
    public OrderPayResultResponse payment(OrderPayRequest orderPayRequest, String ip) {
        StoreOrder storeOrder = storeOrderService.getByOderId(orderPayRequest.getOrderNo());
        if (ObjectUtil.isNull(storeOrder)) {
            throw new CrmebException("订单不存在");
        }
        if (storeOrder.getIsDel()) {
            throw new CrmebException("订单已被删除");
        }
        if (storeOrder.getPaid()) {
            throw new CrmebException("订单已支付");
        }
        User user = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(user)) throw new CrmebException("用户不存在");

        // 判断订单是否还是之前的支付类型
//        if (!storeOrder.getPayType().equals(orderPayRequest.getPayType())) {
//        }
        // 根据支付类型进行校验,更换支付类型
        storeOrder.setPayType(orderPayRequest.getPayType());
        // 余额支付
        if (orderPayRequest.getPayType().equals(PayConstants.PAY_TYPE_YUE)) {
            if (user.getNowMoney().compareTo(storeOrder.getPayPrice()) < 0) {
                throw new CrmebException("用户余额不足");
            }
            storeOrder.setPayType(PayConstants.PAY_TYPE_YUE);
            storeOrder.setIsChannel(3);
        }
        if (orderPayRequest.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            switch (orderPayRequest.getPayChannel()){
                case PayConstants.PAY_CHANNEL_WE_CHAT_H5:// H5
                    storeOrder.setIsChannel(2);
                    break;
                case PayConstants.PAY_CHANNEL_WE_CHAT_PUBLIC:// 公众号
                    storeOrder.setIsChannel(0);
                    break;
                case PayConstants.PAY_CHANNEL_WE_CHAT_PROGRAM:// 小程序
                    storeOrder.setIsChannel(1);
                    break;
                case PayConstants.PAY_CHANNEL_WE_CHAT_APP_IOS:// AppIos
                    storeOrder.setIsChannel(4);
                    break;
                case PayConstants.PAY_CHANNEL_WE_CHAT_APP_ANDROID:// AppAndroid
                    storeOrder.setIsChannel(5);
                    break;
            }
            storeOrder.setPayType(PayConstants.PAY_TYPE_WE_CHAT);
        }
        if (orderPayRequest.getPayType().equals(PayConstants.PAY_TYPE_ALI_PAY)) {
            storeOrder.setIsChannel(6);
            if (orderPayRequest.getPayChannel().equals(PayConstants.PAY_CHANNEL_ALI_APP_PAY)) {
                storeOrder.setIsChannel(7);
            }
            storeOrder.setPayType(PayConstants.PAY_TYPE_ALI_PAY);
        }

        boolean changePayType = storeOrderService.updateById(storeOrder);
        if (!changePayType) {
            throw new CrmebException("变更订单支付类型失败!");
        }

        if (user.getIntegral() < storeOrder.getUseIntegral()) {
            throw new CrmebException("用户积分不足");
        }

        OrderPayResultResponse response = new OrderPayResultResponse();
        response.setOrderNo(storeOrder.getOrderId());
        response.setPayType(storeOrder.getPayType());
        // 0元付
        if (storeOrder.getPayPrice().compareTo(BigDecimal.ZERO) <= 0) {
            Boolean aBoolean = yuePay(storeOrder);
            response.setPayType(PayConstants.PAY_TYPE_YUE);
            response.setStatus(aBoolean);
            return response;
        }

        // 微信支付，调用微信预下单，返回拉起微信支付需要的信息
        if (storeOrder.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            // 预下单
            Map<String, String> unifiedorder = unifiedorder(storeOrder, ip);
            response.setStatus(true);
            if (storeOrder.getType().equals(1)) {
                // 自定义组件生产订单处理
                unifiedorder.put("scene", orderPayRequest.getScene().toString());
                String ticket = componentCreateOrder(storeOrder, user, unifiedorder);
                WxPayJsResultVo vo = new WxPayJsResultVo();
                vo.setAppId(unifiedorder.get("appId"));
                vo.setNonceStr(unifiedorder.get("nonceStr"));
                vo.setPackages(unifiedorder.get("package"));
                vo.setSignType(unifiedorder.get("signType"));
                vo.setTimeStamp(unifiedorder.get("timeStamp"));
                vo.setPaySign(unifiedorder.get("paySign"));
                vo.setTicket(ticket);
                // 更新商户订单号
                storeOrder.setOutTradeNo(unifiedorder.get("outTradeNo"));
                storeOrderService.updateById(storeOrder);
                response.setJsConfig(vo);
            } else {
                WxPayJsResultVo vo = new WxPayJsResultVo();
                vo.setAppId(unifiedorder.get("appId"));
                vo.setNonceStr(unifiedorder.get("nonceStr"));
                vo.setPackages(unifiedorder.get("package"));
                vo.setSignType(unifiedorder.get("signType"));
                vo.setTimeStamp(unifiedorder.get("timeStamp"));
                vo.setPaySign(unifiedorder.get("paySign"));
                if (storeOrder.getIsChannel() == 2) {
                    vo.setMwebUrl(unifiedorder.get("mweb_url"));
                    response.setPayType(PayConstants.PAY_CHANNEL_WE_CHAT_H5);
                }
                if (storeOrder.getIsChannel() == 4 || storeOrder.getIsChannel() == 5) {
                    vo.setPartnerid(unifiedorder.get("partnerid"));
                }
                // 更新商户订单号
                storeOrder.setOutTradeNo(unifiedorder.get("outTradeNo"));
                storeOrderService.updateById(storeOrder);
                response.setJsConfig(vo);
            }
            return response;
        }
        // 余额支付
        if (storeOrder.getPayType().equals(PayConstants.PAY_TYPE_YUE)) {
            Boolean yueBoolean = yuePay(storeOrder);
            response.setStatus(yueBoolean);
            return response;
        }
        if (storeOrder.getPayType().equals(PayConstants.PAY_TYPE_ALI_PAY)) {

            //商户订单号，商户网站订单系统中唯一订单号，必填
            String out_trade_no = storeOrder.getOrderId();
            //付款金额，必填
            String total_amount = storeOrder.getPayPrice().toString();
            //订单名称，必填
            String siteName = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_SITE_NAME);
            String subject = siteName;
            //商品描述，可空
//            String body = "用户订购商品个数：1";

            // 该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m。
            String timeout_express = "30m";

            if (storeOrder.getIsChannel() == 7) {// APP 支付
                //获得初始化的AlipayClient
                String aliPayAppid = systemConfigService.getValueByKey(AlipayConfig.APPID);
                String aliPayPrivateKey = systemConfigService.getValueByKey(AlipayConfig.RSA_PRIVATE_KEY);
                String aliPayPublicKey = systemConfigService.getValueByKey(AlipayConfig.ALIPAY_PUBLIC_KEY);
                AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, aliPayAppid, aliPayPrivateKey, AlipayConfig.FORMAT, AlipayConfig.CHARSET, aliPayPublicKey, AlipayConfig.SIGNTYPE);
                //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
                AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
                //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
                AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
//                model.setBody("我是测试数据");
                model.setSubject(subject);
                model.setOutTradeNo(out_trade_no);
                model.setTimeoutExpress(timeout_express);
                model.setTotalAmount(total_amount);
                model.setProductCode("QUICK_MSECURITY_PAY");

//                HashMap<String, String> map = CollUtil.newHashMap();
//                map.put("type", Constants.SERVICE_PAY_TYPE_ORDER);
//                String jsonString = JSONObject.toJSONString(map);
//                String encode;
                String encode = "type=" + Constants.SERVICE_PAY_TYPE_ORDER;
                try {
                    encode = URLEncoder.encode(encode, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    throw new CrmebException("支付宝参数UrlEncode异常");
                }
                model.setPassbackParams(encode);

                request.setBizModel(model);
                request.setNotifyUrl(systemConfigService.getValueByKey(AlipayConfig.notify_url));

                //请求
                String result;
                try {
                    //这里和普通的接口调用不同，使用的是sdkExecute
                    AlipayTradeAppPayResponse aaa = alipayClient.sdkExecute(request);
                    result = aaa.getBody();
                } catch (AlipayApiException e) {
                    logger.error("生成支付宝app支付请求异常," + e.getErrMsg());
                    throw new CrmebException(e.getErrMsg());
                }
                logger.info("支付宝app result = " + result);
                response.setStatus(true);
                response.setAlipayRequest(result);
                return response;
            }

            //获得初始化的AlipayClient
            String aliPayAppid = systemConfigService.getValueByKey(AlipayConfig.APPID);
            String aliPayPrivateKey = systemConfigService.getValueByKey(AlipayConfig.RSA_PRIVATE_KEY);
            String aliPayPublicKey = systemConfigService.getValueByKey(AlipayConfig.ALIPAY_PUBLIC_KEY);
            AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, aliPayAppid, aliPayPrivateKey, AlipayConfig.FORMAT, AlipayConfig.CHARSET, aliPayPublicKey, AlipayConfig.SIGNTYPE);
            //设置请求参数
            AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
            alipayRequest.setReturnUrl(systemConfigService.getValueByKey(AlipayConfig.return_url));
            alipayRequest.setNotifyUrl(systemConfigService.getValueByKey(AlipayConfig.notify_url));

            AlipayTradeWapPayModel model=new AlipayTradeWapPayModel();
            model.setOutTradeNo(out_trade_no);
            model.setSubject(subject);
            model.setTotalAmount(total_amount);
//            model.setBody(body);
            model.setTimeoutExpress(timeout_express);
            model.setProductCode("QUICK_WAP_PAY");
            model.setQuitUrl(systemConfigService.getValueByKey(AlipayConfig.quit_url));

            String encode = "type=" + Constants.SERVICE_PAY_TYPE_ORDER;
            try {
                encode = URLEncoder.encode(encode, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new CrmebException("支付宝参数UrlEncode异常");
            }
            model.setPassbackParams(encode);

            alipayRequest.setBizModel(model);
            logger.info("alipayRequest = " + alipayRequest);
            //请求
            String result;
            try {
                result = alipayClient.pageExecute(alipayRequest).getBody();
            } catch (AlipayApiException e) {
                logger.error("支付宝订单生成失败," + e.getErrMsg());
                throw new CrmebException(e.getErrMsg());
            }
            logger.info("result = " + result);
            response.setStatus(true);
            response.setAlipayRequest(result);
            return response;
        }
        if (storeOrder.getPayType().equals(PayConstants.PAY_TYPE_OFFLINE)) {
            throw new CrmebException("暂时不支持线下支付");
        }
        response.setStatus(false);
        return response;
    }

    /**
     * 预下单
     * @param storeOrder 订单
     * @param ip ip
     * @return 预下单返回对象
     */
    private Map<String, String> unifiedorder(StoreOrder storeOrder, String ip) {
        // 获取用户openId
        // 根据订单支付类型来判断获取公众号openId还是小程序openId
        UserToken userToken = new UserToken();
        if (storeOrder.getIsChannel() == 0) {// 公众号
            userToken = userTokenService.getTokenByUserId(storeOrder.getUid(), 1);
        }
        if (storeOrder.getIsChannel() == 1) {// 小程序
            userToken = userTokenService.getTokenByUserId(storeOrder.getUid(), 2);
        }
        // H5,app ios,app android
        if (storeOrder.getIsChannel() == 2 || storeOrder.getIsChannel() == 4 || storeOrder.getIsChannel() == 5) {
            userToken.setToken("");
        }

        if (ObjectUtil.isNull(userToken)) {
            throw new CrmebException("该用户没有openId");
        }

        // 获取appid、mch_id
        // 微信签名key
        String appId = "";
        String mchId = "";
        String signKey = "";
        if (storeOrder.getIsChannel() == 0) {// 公众号
            appId = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_ID);
            mchId = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_MCH_ID);
            signKey = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_KEY);
        }
        if (storeOrder.getIsChannel() == 1) {// 小程序
            appId = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_ROUTINE_APP_ID);
            mchId = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_ROUTINE_MCH_ID);
            signKey = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_ROUTINE_APP_KEY);
        }
        if (storeOrder.getIsChannel() == 2) {// H5,使用公众号的
            appId = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_ID);
            mchId = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_MCH_ID);
            signKey = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_KEY);
        }
        if (storeOrder.getIsChannel() == 4 || storeOrder.getIsChannel() == 5) {// App
            appId = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_APP_ID);
            mchId = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_MCH_ID);
            signKey = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_APP_KEY);
        }
        // 获取微信预下单对象
        CreateOrderRequestVo unifiedorderVo = getUnifiedorderVo(storeOrder, userToken.getToken(), ip, appId, mchId, signKey);
        // 预下单（统一下单）
        CreateOrderResponseVo responseVo = wechatNewService.payUnifiedorder(unifiedorderVo);
        // 组装前端预下单参数
        Map<String, String> map = new HashMap<>();
        map.put("appId", unifiedorderVo.getAppid());
        map.put("nonceStr", unifiedorderVo.getAppid());
        map.put("package", "prepay_id=".concat(responseVo.getPrepayId()));
        map.put("signType", unifiedorderVo.getSign_type());
        Long currentTimestamp = WxPayUtil.getCurrentTimestamp();
        map.put("timeStamp", Long.toString(currentTimestamp));
        String paySign = WxPayUtil.getSign(map, signKey);
        map.put("paySign", paySign);
        map.put("prepayId", responseVo.getPrepayId());
        map.put("prepayTime", CrmebDateUtil.nowDateTimeStr());
        map.put("outTradeNo", unifiedorderVo.getOut_trade_no());
        if (storeOrder.getIsChannel() == 2) {
            map.put("mweb_url", responseVo.getMWebUrl());
        }
        if (storeOrder.getIsChannel() == 4 || storeOrder.getIsChannel() == 5) {// App
            map.put("partnerid", mchId);
            map.put("package", responseVo.getPrepayId());
            Map<String, Object> appMap = new HashMap<>();
            appMap.put("appid", unifiedorderVo.getAppid());
            appMap.put("partnerid", mchId);
            appMap.put("prepayid", responseVo.getPrepayId());
            appMap.put("package", "Sign=WXPay");
            appMap.put("noncestr", unifiedorderVo.getAppid());
            appMap.put("timestamp", currentTimestamp);
            logger.info("================================================app支付签名，map = " + appMap);
            String sign = WxPayUtil.getSignObject(appMap, signKey);
            logger.info("================================================app支付签名，sign = " + sign);
            map.put("paySign", sign);
        }
        return map;
    }

    /**
     * 获取微信预下单对象
     * @return 微信预下单对象
     */
    private CreateOrderRequestVo getUnifiedorderVo(StoreOrder storeOrder, String openid, String ip, String appId, String mchId, String signKey) {
        // 获取域名
        String domain = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_SITE_URL);
        String apiDomain = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_API_URL);

        AttachVo attachVo = new AttachVo(Constants.SERVICE_PAY_TYPE_ORDER, storeOrder.getUid());
        CreateOrderRequestVo vo = new CreateOrderRequestVo();

        vo.setAppid(appId);
        vo.setMch_id(mchId);
        vo.setNonce_str(WxPayUtil.getNonceStr());
        vo.setSign_type(PayConstants.WX_PAY_SIGN_TYPE_MD5);
        String siteName = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_SITE_NAME);
        // 因商品名称在微信侧超长更换为网站名称
        vo.setBody(siteName);
        vo.setAttach(JSONObject.toJSONString(attachVo));
        vo.setOut_trade_no(CrmebUtil.getOrderNo("wxNo"));
        // 订单中使用的是BigDecimal,这里要转为Integer类型
        vo.setTotal_fee(storeOrder.getPayPrice().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).intValue());
        vo.setSpbill_create_ip(ip);
        vo.setNotify_url(apiDomain + PayConstants.WX_PAY_NOTIFY_API_URI);
        vo.setTrade_type(PayConstants.WX_PAY_TRADE_TYPE_JS);
        vo.setOpenid(openid);
        if (storeOrder.getIsChannel() == 2){// H5
            vo.setTrade_type(PayConstants.WX_PAY_TRADE_TYPE_H5);
            vo.setOpenid(null);
        }
        if (storeOrder.getIsChannel() == 4 || storeOrder.getIsChannel() == 5) {
            vo.setTrade_type("APP");
            vo.setOpenid(null);
        }
        CreateOrderH5SceneInfoVo createOrderH5SceneInfoVo = new CreateOrderH5SceneInfoVo(
                new CreateOrderH5SceneInfoDetailVo(
                        domain,
                        systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_SITE_NAME)
                )
        );
        vo.setScene_info(JSONObject.toJSONString(createOrderH5SceneInfoVo));
        String sign = WxPayUtil.getSign(vo, signKey);
        vo.setSign(sign);
        return vo;
    }

    /**
     * 自定义组件生成订单
     * @param storeOrder 订单
     * @param user 用户
     * @param unifiedorder 微信统一下单返回数组
     * @return ticket 微信ticket
     */
    private String componentCreateOrder(StoreOrder storeOrder, User user, Map<String, String> unifiedorder) {
        ShopOrderAddVo shopOrderAddVo = new ShopOrderAddVo();
        shopOrderAddVo.setCreateTime(CrmebDateUtil.nowDateTimeStr());
        shopOrderAddVo.setOutOrderId(storeOrder.getOrderId());
        UserToken userToken = userTokenService.getTokenByUserId(user.getUid(), 2);
        if (ObjectUtil.isNull(userToken)) {
            throw new CrmebException("用户小程序openid不存在");
        }
        shopOrderAddVo.setOpenid(userToken.getToken());
        shopOrderAddVo.setPath(StrUtil.format("/pages/order_details/index?order_id={}&type={}", storeOrder.getOrderId(), "video"));
        shopOrderAddVo.setOutUserId(user.getUid());
        shopOrderAddVo.setScene(Integer.valueOf(unifiedorder.get("scene")));
        // 订单详情
        ShopOrderDetailAddVo detailAddVo = new ShopOrderDetailAddVo();
        // 商品详情数组
        List<ShopOrderProductInfoAddVo> productInfos = CollUtil.newArrayList();
        List<StoreOrderInfoOldVo> orderInfoList = storeOrderInfoService.getOrderListByOrderId(storeOrder.getId());
        orderInfoList.forEach(orderInfo -> {
            ShopOrderProductInfoAddVo productInfoAddVo = new ShopOrderProductInfoAddVo();
            PayComponentProduct product = componentProductService.getById(orderInfo.getProductId());
            if (ObjectUtil.isNull(product)) {
                throw new CrmebException("订单商品未找到");
            }
            Integer attrValueId = orderInfo.getInfo().getAttrValueId();
            PayComponentProductSku productSku = componentProductSkuService.getByProIdAndAttrValueId(orderInfo.getProductId(), attrValueId);
            if (ObjectUtil.isNull(productSku)) {
                throw new CrmebException("订单商品sku未找到");
            }
            productInfoAddVo.setOutProductId(orderInfo.getProductId().toString());
            productInfoAddVo.setOutSkuId(productSku.getId().toString());
            productInfoAddVo.setProductCnt(orderInfo.getInfo().getPayNum());
            long salePrice = orderInfo.getInfo().getPrice().multiply(new BigDecimal("100")).longValue();
            productInfoAddVo.setSalePrice(salePrice);
            productInfoAddVo.setHeadImg(orderInfo.getInfo().getImage());
            productInfoAddVo.setTitle(product.getTitle());
            productInfoAddVo.setPath(product.getPath());
            productInfos.add(productInfoAddVo);
        });
        detailAddVo.setProductInfos(productInfos);
        // 支付详情
        ShopOrderPayInfoAddVo payInfoAddVo = new ShopOrderPayInfoAddVo();
        payInfoAddVo.setPayMethod("微信支付");
        payInfoAddVo.setPayMethodType(0);
        payInfoAddVo.setPrepayId(unifiedorder.get("prepayId"));
        payInfoAddVo.setPrepayTime(unifiedorder.get("prepayTime"));
        detailAddVo.setPayInfo(payInfoAddVo);
        // 价格详情
        ShopOrderPriceInfoVo priceInfoVo = new ShopOrderPriceInfoVo();
        priceInfoVo.setOrderPrice(storeOrder.getPayPrice().multiply(new BigDecimal("100")).longValue());
        priceInfoVo.setFreight(storeOrder.getFreightPrice().multiply(new BigDecimal("100")).longValue());
        BigDecimal discountedPrice = storeOrder.getDeductionPrice().add(storeOrder.getCouponPrice());
        if (discountedPrice.compareTo(BigDecimal.ZERO) > 0) {
            priceInfoVo.setDiscountedPrice(discountedPrice.multiply(new BigDecimal("100")).longValue());
        }
        detailAddVo.setPriceInfo(priceInfoVo);

        shopOrderAddVo.setOrderDetail(detailAddVo);
        // 交付详情
        ShopOrderDeliveryDetailAddVo deliveryDetailAddVo = new ShopOrderDeliveryDetailAddVo();
        deliveryDetailAddVo.setDeliveryType(1);// 正常快递
        shopOrderAddVo.setDeliveryDetail(deliveryDetailAddVo);
        // 地址详情
        ShopOrderAddressInfoAddVo addressInfoAddVo = new ShopOrderAddressInfoAddVo();
        addressInfoAddVo.setReceiverName(storeOrder.getRealName());
        addressInfoAddVo.setTelNumber(storeOrder.getUserPhone());
        addressInfoAddVo.setDetailedAddress(storeOrder.getUserAddress());
        shopOrderAddVo.setAddressInfo(addressInfoAddVo);

        String ticket = componentOrderService.create(shopOrderAddVo);
        return ticket;
    }

    private UserIntegralRecord integralRecordSubInit(StoreOrder storeOrder, User user) {
        UserIntegralRecord integralRecord = new UserIntegralRecord();
        integralRecord.setUid(storeOrder.getUid());
        integralRecord.setLinkId(storeOrder.getOrderId());
        integralRecord.setLinkType(IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_ORDER);
        integralRecord.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_SUB);
        integralRecord.setTitle(IntegralRecordConstants.BROKERAGE_RECORD_TITLE_ORDER);
        integralRecord.setIntegral(storeOrder.getUseIntegral());
        integralRecord.setBalance(user.getIntegral());
        integralRecord.setMark(StrUtil.format("订单支付抵扣{}积分购买商品", storeOrder.getUseIntegral()));
        integralRecord.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
        return integralRecord;
    }

    private UserBill userBillInit(StoreOrder order, User user) {
        UserBill userBill = new UserBill();
        userBill.setPm(0);
        userBill.setUid(order.getUid());
        userBill.setLinkId(order.getId().toString());
        userBill.setTitle("购买商品");
        userBill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
        userBill.setType(Constants.USER_BILL_TYPE_PAY_ORDER);
        userBill.setNumber(order.getPayPrice());
        userBill.setBalance(user.getNowMoney());
        userBill.setMark("支付" + order.getPayPrice() + "元购买商品");
        return userBill;
    }

    /**
     * 经验添加记录
     */
    private UserExperienceRecord experienceRecordInit(StoreOrder storeOrder, Integer balance, Integer experience) {
        UserExperienceRecord record = new UserExperienceRecord();
        record.setUid(storeOrder.getUid());
        record.setLinkId(storeOrder.getOrderId());
        record.setLinkType(ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_ORDER);
        record.setType(ExperienceRecordConstants.EXPERIENCE_RECORD_TYPE_ADD);
        record.setTitle(ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_ORDER);
        record.setExperience(experience);
        record.setBalance(balance);
        record.setMark("用户付款成功增加" + experience + "经验");
        record.setCreateTime(cn.hutool.core.date.DateUtil.date());
        return record;
    }

    /**
     * 积分添加记录
     * @return UserIntegralRecord
     */
    private UserIntegralRecord integralRecordInit(StoreOrder storeOrder, Integer balance, Integer integral, String type) {
        UserIntegralRecord integralRecord = new UserIntegralRecord();
        integralRecord.setUid(storeOrder.getUid());
        integralRecord.setLinkId(storeOrder.getOrderId());
        integralRecord.setLinkType(IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_ORDER);
        integralRecord.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD);
        integralRecord.setTitle(IntegralRecordConstants.BROKERAGE_RECORD_TITLE_ORDER);
        integralRecord.setIntegral(integral);
        integralRecord.setBalance(balance);
        if (type.equals("order")){
            integralRecord.setMark(StrUtil.format("用户付款成功,订单增加{}积分", integral));
        }
        if (type.equals("product")) {
            integralRecord.setMark(StrUtil.format("用户付款成功,商品增加{}积分", integral));
        }
        integralRecord.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_CREATE);
        // 获取积分冻结期
        String fronzenTime = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_INTEGRAL_EXTRACT_TIME);
        integralRecord.setFrozenTime(Integer.valueOf(Optional.ofNullable(fronzenTime).orElse("0")));
        integralRecord.setCreateTime(CrmebDateUtil.nowDateTime());
        return integralRecord;
    }

    /**
     * 发送消息通知
     * 根据用户类型发送
     * 公众号模板消息
     * 小程序订阅消息
     */
    private void pushMessageOrder(StoreOrder storeOrder, User user, SystemNotification payNotification) {
        if (storeOrder.getIsChannel().equals(2)) {// H5
            return;
        }
        UserToken userToken;
        HashMap<String, String> temMap = new HashMap<>();
        if (!storeOrder.getPayType().equals(Constants.PAY_TYPE_WE_CHAT)) {
            return;
        }
        // 公众号
        if (storeOrder.getIsChannel().equals(Constants.ORDER_PAY_CHANNEL_PUBLIC) && payNotification.getIsWechat().equals(1)) {
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_WECHAT);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 发送微信模板消息
            temMap.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "您的订单已支付成功！");
            temMap.put("keyword1", storeOrder.getPayPrice().toString());
            temMap.put("keyword2", storeOrder.getOrderId());
            temMap.put(Constants.WE_CHAT_TEMP_KEY_END, "欢迎下次再来！");
            templateMessageService.pushTemplateMessage(payNotification.getWechatId(), temMap, userToken.getToken());
            return;
        }
        if (storeOrder.getIsChannel().equals(Constants.ORDER_PAY_CHANNEL_PROGRAM) && payNotification.getIsRoutine().equals(1)) {
            // 小程序发送订阅消息
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 组装数据
//            temMap.put("character_string1", storeOrder.getOrderId());
//            temMap.put("amount2", storeOrder.getPayPrice().toString() + "元");
//            temMap.put("thing7", "您的订单已支付成功");
            temMap.put("character_string3", storeOrder.getOrderId());
            temMap.put("amount9", storeOrder.getPayPrice().toString() + "元");
            temMap.put("thing6", "您的订单已支付成功");
            templateMessageService.pushMiniTemplateMessage(payNotification.getRoutineId(), temMap, userToken.getToken());
        }
    }

    /**
     * 商品购买后根据配置送券
     */
    private void autoSendCoupons(StoreOrder storeOrder){
        // 根据订单详情获取商品信息
        List<StoreOrderInfoOldVo> orders = storeOrderInfoService.getOrderListByOrderId(storeOrder.getId());
        if(null == orders){
            return;
        }
        List<StoreCouponUser> couponUserList = CollUtil.newArrayList();
        Map<Integer, Boolean> couponMap = CollUtil.newHashMap();
        for (StoreOrderInfoOldVo order : orders) {
            List<StoreProductCoupon> couponsForGiveUser = storeProductCouponService.getListByProductId(order.getProductId());
            for (int i = 0; i < couponsForGiveUser.size();) {
                StoreProductCoupon storeProductCoupon = couponsForGiveUser.get(i);
                MyRecord record = storeCouponUserService.paySuccessGiveAway(storeProductCoupon.getIssueCouponId(), storeOrder.getUid());
                if (record.getStr("status").equals("fail")) {
                    logger.error(StrUtil.format("支付成功领取优惠券失败，失败原因：{}", record.getStr("errMsg")));
                    couponsForGiveUser.remove(i);
                    continue;
                }

                StoreCouponUser storeCouponUser = record.get("storeCouponUser");
                couponUserList.add(storeCouponUser);
                couponMap.put(storeCouponUser.getCouponId(), record.getBoolean("isLimited"));
                i++;
            }
        }

        Boolean execute = transactionTemplate.execute(e -> {
            if (CollUtil.isNotEmpty(couponUserList)) {
                storeCouponUserService.saveBatch(couponUserList);
                couponUserList.forEach(i -> storeCouponService.deduction(i.getCouponId(), 1, couponMap.get(i.getCouponId())));
            }
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error(StrUtil.format("支付成功领取优惠券，更新数据库失败，订单编号：{}", storeOrder.getOrderId()));
        }
    }
}
