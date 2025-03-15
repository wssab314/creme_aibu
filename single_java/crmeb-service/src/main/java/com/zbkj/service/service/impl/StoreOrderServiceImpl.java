package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.*;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.combination.StorePink;
import com.zbkj.common.model.express.Express;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.order.StoreOrderInfo;
import com.zbkj.common.model.sms.SmsTemplate;
import com.zbkj.common.model.system.SystemAdmin;
import com.zbkj.common.model.system.SystemNotification;
import com.zbkj.common.model.system.SystemStore;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.model.user.UserToken;
import com.zbkj.common.model.wechat.video.PayComponentDeliveryCompany;
import com.zbkj.common.model.wechat.video.PayComponentOrder;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.*;
import com.zbkj.common.request.onepass.OnePassShipmentCreateOrderRequest;
import com.zbkj.common.response.*;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.ValidateFormUtil;
import com.zbkj.common.vo.*;
import com.zbkj.service.dao.StoreOrderDao;
import com.zbkj.service.delete.OrderUtils;
import com.zbkj.service.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * StoreOrderServiceImpl 接口实现
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
public class StoreOrderServiceImpl extends ServiceImpl<StoreOrderDao, StoreOrder> implements StoreOrderService {

    private final Logger logger = LoggerFactory.getLogger(StoreOrderServiceImpl.class);

    @Resource
    private StoreOrderDao dao;

    @Autowired
    private SystemStoreService systemStoreService;

    @Autowired
    private StoreOrderInfoService StoreOrderInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBillService userBillService;

    @Autowired
    private StoreOrderStatusService storeOrderStatusService;

    @Autowired
    private StoreOrderRefundService storeOrderRefundService;

    @Autowired
    private ExpressService expressService;

    @Autowired
    private TemplateMessageService templateMessageService;

    @Autowired
    private LogisticService logisticService;

    @Autowired
    private OrderUtils orderUtils;

    @Autowired
    private SystemAdminService systemAdminService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private StorePinkService storePinkService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OnePassService onePassService;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private PayComponentOrderService componentOrderService;

    @Autowired
    private WechatVideoDeliveryService wechatVideoDeliveryService;

    @Autowired
    private PayComponentDeliveryCompanyService componentDeliveryCompanyService;

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    @Autowired
    private SmsTemplateService smsTemplateService;

    @Autowired
    private CrmebUtil crmebUtil;

    @Autowired
    private WechatOrderShippingService wechatOrderShippingService;

    /**
    * 列表
    * @param request 请求参数
    * @param pageParamRequest 分页类参数
    * @return CommonPage<StoreOrderDetailResponse>
    */
    @Override
    public CommonPage<StoreOrderDetailResponse> getAdminList(StoreOrderSearchRequest request, PageParamRequest pageParamRequest) {
        Page<Object> startPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        QueryWrapper<StoreOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "order_id", "uid", "real_name", "pay_price", "pay_type", "create_time", "status", "refund_status"
                , "refund_reason_wap_img", "refund_reason_wap_explain", "refund_reason_wap", "refund_reason", "refund_reason_time"
                , "is_del", "combination_id", "pink_id", "seckill_id", "bargain_id", "verify_code", "remark", "paid", "is_system_del"
                , "shipping_type", "type", "is_alter_price", "pro_total_price", "is_alter_price", "coupon_price");
        if (StrUtil.isNotBlank(request.getOrderNo())) {
            queryWrapper.eq("order_id", request.getOrderNo());
        }
        getRequestTimeWhere(queryWrapper, request);
        getStatusWhere(queryWrapper, request.getStatus());
        if (!request.getType().equals(2)) {
            queryWrapper.eq("type", request.getType());
        }
        queryWrapper.orderByDesc("id");
        List<StoreOrder> orderList = dao.selectList(queryWrapper);
        List<StoreOrderDetailResponse> detailResponseList = new ArrayList<>();
        if (CollUtil.isNotEmpty(orderList)) {
            detailResponseList = formatOrder1(orderList);
        }
        return CommonPage.restPage(CommonPage.copyPageInfo(startPage, detailResponseList));
    }


    /**
     * H5订单列表
     * @param uid 用户uid
     * @param status 评价等级|0=未支付,1=待发货,2=待收货,3=待评价,4=已完成,-3=售后/退款
     * @param pageParamRequest 分页参数
     * @return 订单结果列表
     */
    @Override
    public List<StoreOrder> getUserOrderList(Integer uid, Integer status, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<StoreOrder> lqw = new LambdaQueryWrapper<>();
        orderUtils.statusApiByWhere(lqw, status);
        lqw.eq(StoreOrder::getUid, uid);
        lqw.orderByDesc(StoreOrder::getId);
        return dao.selectList(lqw);
    }

    /**
     * 创建订单
     * @param storeOrder 订单参数
     * @return 结果标识
     */
    @Override
    public boolean create(StoreOrder storeOrder) {
        return dao.insert(storeOrder) > 0;
    }

    /**
     * 订单基本查询一条
     * @param storeOrder 参数
     * @return 查询结果
     */
    @Override
    public StoreOrder getByEntityOne(StoreOrder storeOrder) {
        LambdaQueryWrapper<StoreOrder> lqw = new LambdaQueryWrapper<>();
        lqw.setEntity(storeOrder);
        return dao.selectOne(lqw);
    }

    /**
     * 核销列表
     * @param request 请求参数
     * @param pageParamRequest 分页类参数
     * @return List<StoreOrder>
     */
    @Override
    public SystemWriteOffOrderResponse getWriteOffList(SystemWriteOffOrderSearchRequest request, PageParamRequest pageParamRequest) {
        LambdaQueryWrapper<StoreOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        String where = " is_del = 0 and shipping_type = 2";
        //时间
        if (!StringUtils.isBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(request.getDateLimit());
            where += " and (create_time between '" + dateLimit.getStartTime() + "' and '" + dateLimit.getEndTime() + "' )";
        }

        if (!StringUtils.isBlank(request.getKeywords())) {
            where += " and (real_name like '%"+ request.getKeywords() +"%' or user_phone = '"+ request.getKeywords() +"' or order_id = '" + request.getKeywords() + "' or id = '" + request.getKeywords() + "' )";
        }

        if (request.getStoreId() != null && request.getStoreId() > 0) {
            where += " and store_id = " + request.getStoreId();
        }

        SystemWriteOffOrderResponse systemWriteOffOrderResponse = new SystemWriteOffOrderResponse();
        BigDecimal totalPrice = dao.getTotalPrice(where);
        if (ObjectUtil.isNull(totalPrice)) {
            totalPrice = BigDecimal.ZERO;
        }
        systemWriteOffOrderResponse.setOrderTotalPrice(totalPrice);   //订单总金额

        BigDecimal refundPrice = dao.getRefundPrice(where);
        if (ObjectUtil.isNull(refundPrice)) {
            refundPrice = BigDecimal.ZERO;
        }
        systemWriteOffOrderResponse.setRefundTotalPrice(refundPrice); //退款总金额
        systemWriteOffOrderResponse.setRefundTotal(dao.getRefundTotal(where));  //退款总单数

        Page<StoreOrder> storeOrderPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        lambdaQueryWrapper.apply(where);
        lambdaQueryWrapper.orderByDesc(StoreOrder::getId);
        List<StoreOrder> storeOrderList = dao.selectList(lambdaQueryWrapper);

        if (storeOrderList.size() < 1) {
            systemWriteOffOrderResponse.setList(CommonPage.restPage(new PageInfo<>()));
            return systemWriteOffOrderResponse;
        }

        List<StoreOrderItemResponse> storeOrderItemResponseArrayList = formatOrder(storeOrderList);

        systemWriteOffOrderResponse.setTotal(storeOrderPage.getTotal()); //总单数
        systemWriteOffOrderResponse.setList(CommonPage.restPage(CommonPage.copyPageInfo(storeOrderPage, storeOrderItemResponseArrayList)));

        return systemWriteOffOrderResponse;
    }

    /**
     * 格式化订单信息，对外输出一致
     * @param orderList List<StoreOrder> 订单列表
     * @return List<StoreOrderItemResponse>
     */
    private List<StoreOrderDetailResponse> formatOrder1(List<StoreOrder> orderList) {
        List<StoreOrderDetailResponse> detailResponseList  = new ArrayList<>();
        if (CollUtil.isEmpty(orderList)) {
            return detailResponseList;
        }

        //订单id集合
        List<Integer> orderIdList = orderList.stream().map(StoreOrder::getId).distinct().collect(Collectors.toList());

        //获取订单详情map
        HashMap<Integer, List<StoreOrderInfoOldVo>> orderInfoList = StoreOrderInfoService.getMapInId(orderIdList);
//
//        //根据用户获取信息
//        List<Integer> userIdList = orderList.stream().map(StoreOrder::getUid).distinct().collect(Collectors.toList());
//        //订单用户信息
//        HashMap<Integer, User> userList = userService.getMapListInUid(userIdList);

        for (StoreOrder storeOrder : orderList) {
            StoreOrderDetailResponse storeOrderItemResponse = new StoreOrderDetailResponse();
            BeanUtils.copyProperties(storeOrder, storeOrderItemResponse);

            storeOrderItemResponse.setProductList(orderInfoList.get(storeOrder.getId()));

            //订单状态
            storeOrderItemResponse.setStatusStr(getStatus(storeOrder));
            storeOrderItemResponse.setStatus(storeOrder.getStatus());
            //支付方式
            storeOrderItemResponse.setPayTypeStr(getPayType(storeOrder.getPayType()));

            // 添加订单类型信息
            storeOrderItemResponse.setOrderType(getOrderTypeStr(storeOrder));
            detailResponseList.add(storeOrderItemResponse);
        }
        return detailResponseList;
    }

    /**
     * 获取订单类型（前端展示）
     * @param storeOrder 订单
     * @return String
     */
    private String getOrderTypeStr(StoreOrder storeOrder) {
        String orderTypeFormat = "[{}订单]{}";
        String orderType = StrUtil.format(orderTypeFormat, "普通", "");
        // 核销
        if (StrUtil.isNotBlank(storeOrder.getVerifyCode())) {
            orderType = StrUtil.format(orderTypeFormat, "核销", "");
        }
        // 秒杀
        if (ObjectUtil.isNotNull(storeOrder.getSeckillId()) && storeOrder.getSeckillId() > 0) {
            orderType = StrUtil.format(orderTypeFormat, "秒杀", "");
        }
        // 砍价
        if (ObjectUtil.isNotNull(storeOrder.getBargainId()) && storeOrder.getBargainId() > 0) {
            orderType = StrUtil.format(orderTypeFormat, "砍价", "");
        }
        // 拼团
        if (ObjectUtil.isNotNull(storeOrder.getCombinationId()) && storeOrder.getCombinationId() > 0) {
            StorePink storePink = storePinkService.getById(storeOrder.getPinkId());
            if (ObjectUtil.isNotNull(storePink)) {
                String pinkstatus = "";
                if (storePink.getStatus() == 2) {
                    pinkstatus = "已完成";
                } else if (storePink.getStatus() == 3) {
                    pinkstatus = "未完成";
                } else {
                    pinkstatus = "正在进行中";
                }
                orderType = StrUtil.format(orderTypeFormat, "拼团", pinkstatus);
            }
        }
        if (storeOrder.getType().equals(1)) {// 视频订单
            orderType = StrUtil.format(orderTypeFormat, "视频号", "");
        }
        return orderType;
    }

    /**
     * 格式化订单信息，对外输出一致
     * @param storeOrderList List<StoreOrder> 订单列表
     * @author Mr.Zhang
     * @since 2020-05-28
     * @return List<StoreOrderItemResponse>
     */
    private List<StoreOrderItemResponse> formatOrder(List<StoreOrder> storeOrderList) {
        List<StoreOrderItemResponse> storeOrderItemResponseArrayList  = new ArrayList<>();
        if (null == storeOrderList || storeOrderList.size() < 1) {
            return storeOrderItemResponseArrayList;
        }
        //门店id
        List<Integer> storeIdList = storeOrderList.stream().map(StoreOrder::getStoreId).distinct().collect(Collectors.toList());
        //店员id / 核销员id
        List<Integer> clerkIdList = storeOrderList.stream().map(StoreOrder::getClerkId).distinct().collect(Collectors.toList());

        //订单id集合
        List<Integer> orderIdList = storeOrderList.stream().map(StoreOrder::getId).distinct().collect(Collectors.toList());

        //获取门店map
        HashMap<Integer, SystemStore> systemStoreList = systemStoreService.getMapInId(storeIdList);
        //获取店员map
//        HashMap<Integer, SystemStoreStaff> systemStoreStaffList = systemStoreStaffService.getMapInId(clerkIdList);
        HashMap<Integer, SystemAdmin> systemStoreStaffList = systemAdminService.getMapInId(clerkIdList);
        //获取订单详情map
        HashMap<Integer, List<StoreOrderInfoOldVo>> orderInfoList = StoreOrderInfoService.getMapInId(orderIdList);

        //根据用户获取信息
        List<Integer> userIdList = storeOrderList.stream().map(StoreOrder::getUid).distinct().collect(Collectors.toList());
        //订单用户信息
        HashMap<Integer, User> userList = userService.getMapListInUid(userIdList);

        //获取推广人id集合
        List<Integer> spreadPeopleUidList = new ArrayList<>();
        for(Map.Entry<Integer, User> entry : userList.entrySet()) {
            spreadPeopleUidList.add(entry.getValue().getSpreadUid());
        }

        //推广信息
        HashMap<Integer, User> mapListInUid = new HashMap<>();
        if (userIdList.size() > 0 && spreadPeopleUidList.size() > 0) {
            //推广人信息
            mapListInUid = userService.getMapListInUid(spreadPeopleUidList);
        }

        for (StoreOrder storeOrder : storeOrderList) {
            StoreOrderItemResponse storeOrderItemResponse = new StoreOrderItemResponse();
            BeanUtils.copyProperties(storeOrder, storeOrderItemResponse);
            String storeName = "";
            if (systemStoreList.containsKey(storeOrder.getStoreId())) {
                storeName = systemStoreList.get(storeOrder.getStoreId()).getName();
            }
            storeOrderItemResponse.setStoreName(storeName);

            // 添加核销人信息
            String clerkName = "";
            if (systemStoreStaffList.containsKey(storeOrder.getClerkId())) {
                clerkName = systemStoreStaffList.get(storeOrder.getClerkId()).getRealName();
            }
            storeOrderItemResponse.setProductList(orderInfoList.get(storeOrder.getId()));
            storeOrderItemResponse.setTotalNum(storeOrder.getTotalNum());

            //订单状态
            storeOrderItemResponse.setStatusStr(getStatus(storeOrder));
            storeOrderItemResponse.setStatus(storeOrder.getStatus());
            //支付方式
            storeOrderItemResponse.setPayTypeStr(getPayType(storeOrder.getPayType()));

            //推广人信息
            if (!userList.isEmpty()  && null != userList.get(storeOrder.getUid()) && mapListInUid.containsKey(userList.get(storeOrder.getUid()).getSpreadUid())) {
                storeOrderItemResponse.getSpreadInfo().setId(mapListInUid.get(userList.get(storeOrder.getUid()).getSpreadUid()).getUid());
                storeOrderItemResponse.getSpreadInfo().setName(mapListInUid.get(userList.get(storeOrder.getUid()).getSpreadUid()).getNickname());
            }
            storeOrderItemResponse.setRefundStatus(storeOrder.getRefundStatus());

            storeOrderItemResponse.setClerkName(clerkName);

            // 添加订单类型信息
            String orderTypeFormat = "[{}订单]{}";
            String orderType = "";
            // 核销
            if (StrUtil.isNotBlank(storeOrder.getVerifyCode())) {
                orderType = StrUtil.format(orderTypeFormat, "核销", "");
            }
            // 秒杀
            if (ObjectUtil.isNotNull(storeOrder.getSeckillId()) && storeOrder.getSeckillId() > 0) {
                orderType = StrUtil.format(orderTypeFormat, "秒杀", "");
            }
            // 砍价
            if (ObjectUtil.isNotNull(storeOrder.getBargainId()) && storeOrder.getBargainId() > 0) {
                orderType = StrUtil.format(orderTypeFormat, "砍价", "");
            }
            // 拼团
            if (ObjectUtil.isNotNull(storeOrder.getPinkId()) && storeOrder.getPinkId() > 0) {
                StorePink storePink = storePinkService.getById(storeOrder.getPinkId());
                if (ObjectUtil.isNotNull(storePink)) {
                    String pinkstatus = "";
                    if (storePink.getStatus() == 2) {
                        pinkstatus = "已完成";
                    } else if (storePink.getStatus() == 3) {
                        pinkstatus = "未完成";
                    } else {
                        pinkstatus = "正在进行中";
                    }
                    orderType = StrUtil.format(orderTypeFormat, "拼团", pinkstatus);
                }
            }
            if (StrUtil.isBlank(orderType)) {
                orderType = StrUtil.format(orderTypeFormat, "普通", "");
            }
            storeOrderItemResponse.setOrderType(orderType);
            storeOrderItemResponseArrayList.add(storeOrderItemResponse);
        }
        return storeOrderItemResponseArrayList;
    }

    /**
     * 累计消费
     * @param userId Integer 用户id
     * @author Mr.Zhang
     * @since 2020-06-10
     * @return UserBalanceResponse
     */
    @Override
    public BigDecimal getSumBigDecimal(Integer userId, String date) {
        QueryWrapper<StoreOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(pay_price) as pay_price").
                eq("paid", 1).
                eq("is_del", 0);
        if (null != userId) {
            queryWrapper.eq("uid", userId);
        }
        if (null != date) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(date);
            queryWrapper.between("create_time", dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        StoreOrder storeOrder = dao.selectOne(queryWrapper);
        if (null == storeOrder || null == storeOrder.getPayPrice()) {
            return BigDecimal.ZERO;
        }
        return storeOrder.getPayPrice();
    }

    /**
     * 按开始结束时间分组订单
     * @param date String 时间范围
     * @param lefTime int 截取创建时间长度
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return HashMap<String, Object>
     */
    public List<StoreOrder> getOrderGroupByDate(String date, int lefTime) {
        QueryWrapper<StoreOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(pay_price) as pay_price", "left(create_time, "+lefTime+") as orderId", "count(id) as id");
        if (StringUtils.isNotBlank(date)) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(date);
            queryWrapper.between("create_time", dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        queryWrapper.groupBy("orderId").orderByAsc("orderId");
        return dao.selectList(queryWrapper);
    }

    /** 退款
     * @param request StoreOrderRefundRequest 退款参数
     * @return boolean
     * 这里只处理订单状态
     * 余额支付需要把余额给用户加回去
     * 其余处理放入redis中处理
     */
    @Override
    public boolean refund(StoreOrderRefundRequest request) {
        StoreOrder storeOrder = getInfoException(request.getOrderNo());
        if (!storeOrder.getPaid()) {
            throw new CrmebException("未支付无法退款");
        }
        if (storeOrder.getRefundPrice().add(request.getAmount()).compareTo(storeOrder.getPayPrice()) > 0) {
            throw new CrmebException("退款金额大于支付金额，请修改退款金额");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            if (storeOrder.getPayPrice().compareTo(BigDecimal.ZERO) != 0) {
                throw new CrmebException("退款金额不能为0，请修改退款金额");
            }
        }
        request.setOrderId(storeOrder.getId());
        //用户
        User user = userService.getById(storeOrder.getUid());

        //退款
        if (storeOrder.getPayType().equals(Constants.PAY_TYPE_WE_CHAT) && request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            try {
                storeOrderRefundService.refund(request, storeOrder);
            } catch (Exception e) {
                e.printStackTrace();
                throw new CrmebException("微信申请退款失败！");
            }
        }
        if (storeOrder.getPayType().equals(Constants.PAY_TYPE_ALI_PAY) && request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            try {
                aliPayService.refund(request, storeOrder);
            } catch (Exception e) {
                e.printStackTrace();
                throw new CrmebException("支付宝申请退款失败！");
            }
        }

        //修改订单退款状态
        storeOrder.setRefundStatus(3);
        storeOrder.setRefundPrice(request.getAmount());

        Boolean execute = transactionTemplate.execute(e -> {
            updateById(storeOrder);
            if (storeOrder.getPayType().equals(Constants.PAY_TYPE_YUE)) {
                //新增日志
                request.setOrderId(storeOrder.getId());
                userBillService.saveRefundBill(request, user);

                // 更新用户金额
                userService.operationNowMoney(user.getUid(), request.getAmount(), user.getNowMoney(), "add");

                // 退款task
                redisUtil.lPush(Constants.ORDER_TASK_REDIS_KEY_AFTER_REFUND_BY_USER, storeOrder.getId());
            }
            if (storeOrder.getPayType().equals(Constants.PAY_TYPE_WE_CHAT) && request.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                //新增日志
                userBillService.saveRefundBill(request, user);

                // 退款task
                redisUtil.lPush(Constants.ORDER_TASK_REDIS_KEY_AFTER_REFUND_BY_USER, storeOrder.getId());
            }
            if (storeOrder.getPayType().equals(Constants.PAY_TYPE_ALI_PAY) && request.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                //新增日志
                userBillService.saveRefundBill(request, user);

                // 退款task
                redisUtil.lPush(Constants.ORDER_TASK_REDIS_KEY_AFTER_REFUND_BY_USER, storeOrder.getId());
            }
            return Boolean.TRUE;
        });
        if (!execute) {
            storeOrderStatusService.saveRefund(storeOrder.getId(), request.getAmount(), "失败");
            throw new CrmebException("订单更新失败");
        }

        // 发送消息通知
//        HashMap<String, String> temMap = new HashMap<>();
//        temMap.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "您的订单退款申请被通过，钱款将退还至您的支付账户，请耐心等待。");
//        temMap.put("keyword1", storeOrder.getOrderId());
//        temMap.put("keyword2", storeOrder.getPayPrice().toString());
//        temMap.put("keyword3", DateUtil.dateToStr(storeOrder.getCreateTime(), Constants.DATE_FORMAT));
//        temMap.put(Constants.WE_CHAT_TEMP_KEY_END, "感谢你的使用。");
//        pushMessageRefundOrder(storeOrder, user, temMap);
        return execute;
    }

    /**
     * 发送消息通知
     * 根据用户类型发送
     * 公众号模板消息
     * 小程序订阅消息
     */
//    private void pushMessageRefundOrder(StoreOrder storeOrder, User user, HashMap<String, String> temMap) {
//        if (user.getUserType().equals(UserConstants.USER_TYPE_H5)) {
//            return;
//        }
//        UserToken userToken;
//        // 公众号
//        if (user.getUserType().equals(UserConstants.USER_TYPE_WECHAT)) {
//            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_WECHAT);
//            if (ObjectUtil.isNull(userToken)) {
//                return ;
//            }
//            // 发送微信模板消息
//            templateMessageService.pushTemplateMessage(Constants.WE_CHAT_TEMP_KEY_ORDER_REFUND, temMap, userToken.getToken());
//            return;
//        }
//        // 小程序发送订阅消息
//        String storeNameAndCarNumString = orderUtils.getStoreNameAndCarNumString(storeOrder.getId());
//        if (StringUtils.isNotBlank(storeNameAndCarNumString)) {
//            WechatSendMessageForPaySuccess paySuccess = new WechatSendMessageForPaySuccess(
//                    storeOrder.getId()+"",storeOrder.getPayPrice()+"",storeOrder.getPayTime()+"","暂无",
//                    storeOrder.getTotalPrice()+"",storeNameAndCarNumString);
//            orderUtils.sendWeiChatMiniMessageForPaySuccess(paySuccess, userService.getById(storeOrder).getUid());
//        }
//    }

    /**
     * 订单详情（PC）
     * @param orderNo 订单编号
     * @return StoreOrderInfoResponse
     */
    @Override
    public StoreOrderInfoResponse info(String orderNo) {
        StoreOrder storeOrder = getInfoException(orderNo);
        if (storeOrder.getIsSystemDel()) {
            throw new CrmebException("未找到对应订单信息");
        }
        StoreOrderInfoResponse storeOrderInfoResponse = new StoreOrderInfoResponse();
        BeanUtils.copyProperties(storeOrder, storeOrderInfoResponse);
        List<StoreOrderInfoOldVo> orderInfos = StoreOrderInfoService.getOrderListByOrderId(storeOrder.getId());
        storeOrderInfoResponse.setOrderInfo(orderInfos);
        storeOrderInfoResponse.setPayTypeStr(getPayType(storeOrder.getPayType()));
        storeOrderInfoResponse.setStatusStr(getStatus(storeOrder));
        storeOrderInfoResponse.setRefundReasonWapImg(storeOrder.getRefundReasonWapImg());
        if (ObjectUtil.isNotNull(storeOrder.getStoreId()) && storeOrder.getStoreId() > 0) {
            SystemStore systemStorePram = new SystemStore();
            systemStorePram.setId(storeOrder.getStoreId());
            storeOrderInfoResponse.setSystemStore(systemStoreService.getByCondition(systemStorePram));
        }

        //用户信息
        User user = userService.getById(storeOrder.getUid());
        storeOrderInfoResponse.setNikeName(user.getNickname());
        storeOrderInfoResponse.setPhone(user.getPhone());

        UserBrokerageRecord brokerageRecord = userBrokerageRecordService.getByLinkIdAndLinkType(orderNo, "order");
        if (ObjectUtil.isNotNull(brokerageRecord)) {
            User spread = userService.getById(brokerageRecord.getUid());
            storeOrderInfoResponse.setSpreadName(spread.getNickname());
        }

        storeOrderInfoResponse.setProTotalPrice(storeOrder.getTotalPrice().subtract(storeOrder.getTotalPostage()));

        // 手机号脱敏处理
        storeOrderInfoResponse.setUserPhone(crmebUtil.maskMobile(storeOrderInfoResponse.getUserPhone()));
        storeOrderInfoResponse.setPhone(crmebUtil.maskMobile(storeOrderInfoResponse.getPhone()));
        return storeOrderInfoResponse;
    }

    /** 发送货物
     * @param request StoreOrderSendRequest 发货参数
     * @author Mr.Zhang
     * @since 2020-06-10
     * @return boolean
     */
    @Override
    public String send(StoreOrderSendRequest request) {
        String mianDanResult = "";
        //订单信息
        StoreOrder storeOrder = getInfoException(request.getOrderNo());
        if (storeOrder.getIsDel()) throw new CrmebException("订单已删除,不能发货!");
        if (storeOrder.getStatus() > 0) throw new CrmebException("订单已发货请勿重复操作!");
        if (ObjectUtil.isNotNull(storeOrder.getCombinationId()) && storeOrder.getCombinationId() > 0) {
            StorePink storePink = storePinkService.getById(storeOrder.getPinkId());
            if (storePink.getStatus() != 2) throw new CrmebException("当前订单正在拼团中不能发货！");
        }
        request.setId(storeOrder.getId());
        switch (request.getDeliveryType()) {
            case "express":// 发货
                mianDanResult = express(request, storeOrder);
                break;
            case "send":// 送货
                delivery(request, storeOrder);
                break;
            case "fictitious":// 虚拟
                virtual(request, storeOrder);
                break;
            default:
                throw new CrmebException("类型错误");
        }
        return mianDanResult;
    }

    /**
     * 订单备注
     * @param orderNo 订单编号
     * @param mark 备注
     * @return Boolean
     */
    @Override
    public Boolean mark(String orderNo, String mark) {
        StoreOrder storeOrder = getInfoException(orderNo);
        storeOrder.setRemark(mark);
        return updateById(storeOrder);
    }

    /**
     * 拒绝退款
     * @param orderNo 订单编号
     * @param reason String 原因
     * @return Boolean
     */
    @Override
    public Boolean refundRefuse(String orderNo, String reason) {
        if (StrUtil.isBlank(reason)) {
            throw new CrmebException("请填写拒绝退款原因");
        }
        StoreOrder storeOrder = getInfoException(orderNo);
        storeOrder.setRefundReason(reason);
        storeOrder.setRefundStatus(0);

        User user = userService.getById(storeOrder.getUid());

        Boolean execute = transactionTemplate.execute(e -> {
            updateById(storeOrder);
            storeOrderStatusService.createLog(storeOrder.getId(), Constants.ORDER_LOG_REFUND_REFUSE, Constants.ORDER_LOG_MESSAGE_REFUND_REFUSE.replace("{reason}", reason));
            return Boolean.TRUE;
        });
        if (execute) {
            // 如果是拼团订单要将拼团状态改回去
            if (ObjectUtil.isNotNull(storeOrder) && storeOrder.getPinkId() > 0) {
                StorePink storePink = storePinkService.getById(storeOrder.getPinkId());
                if (storePink.getStatus().equals(3)) {
                    storePink.setStatus(1);
                    storePinkService.updateById(storePink);
                }
            }
        }
        return execute;
    }

    /**
     * 查询单条
     * @param storeOrder StoreOrder 订单参数
     * @author Mr.Zhang
     * @since 2020-05-28
     * @return StoreOrder
     */
    @Override
    public StoreOrder getInfoByEntity(StoreOrder storeOrder) {
        LambdaQueryWrapper<StoreOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.setEntity(storeOrder);
        return dao.selectOne(lambdaQueryWrapper);
    }

    /**
     * 获取订单快递信息
     * @param orderNo 订单编号
     * @return LogisticsResultVo
     */
    @Override
    public LogisticsResultVo getLogisticsInfo(String orderNo) {
        StoreOrder info = getInfoException(orderNo);
        if (info.getType().equals(1)) {// 视频号订单
            Express express = expressService.getByName(info.getDeliveryName());
            if (ObjectUtil.isNotNull(express)) {
                info.setDeliveryCode(express.getCode());
            } else {
                info.setDeliveryCode("");
            }
        }
        return logisticService.info(info.getDeliveryId(), null, Optional.ofNullable(info.getDeliveryCode()).orElse(""), info.getUserPhone());
    }

    /**
     * 订单 top 查询参数
     * @param status 状态参数
     * @return 订单查询结果
     */
    @Override
    public Integer getTopDataUtil(Integer status, Integer userId) {
        LambdaQueryWrapper<StoreOrder> lqw = new LambdaQueryWrapper<>();
        orderUtils.statusApiByWhere(lqw, status);
        lqw.eq(StoreOrder::getUid,userId);
        return dao.selectCount(lqw);
    }

    /**
     * 改价
     * @param orderNo 订单编号
     * @param price 修改后的价格
     * @param oldPrice 原支付金额
     */
    private Boolean orderEditPrice(String orderNo, BigDecimal price, BigDecimal oldPrice) {
        LambdaUpdateWrapper<StoreOrder> luw = new LambdaUpdateWrapper<>();
        luw.set(StoreOrder::getPayPrice, price);
        luw.set(StoreOrder::getBeforePayPrice, oldPrice);
        luw.set(StoreOrder::getIsAlterPrice, 1);
        luw.eq(StoreOrder::getOrderId, orderNo);
        luw.eq(StoreOrder::getPaid, false);
        return update(luw);
    }

    /**
     * 根据时间参数统计订单销售额
     *
     * @param dateLimit 时间区间
     * @param type 类型
     * @return 统计订单信息
     */
    @Override
    public StoreOrderStatisticsResponse orderStatisticsByTime(String dateLimit, Integer type) {
        StoreOrderStatisticsResponse response = new StoreOrderStatisticsResponse();
        // 根据开始时间和结束时间获取时间差 再根据时间差获取上一个时间段 查询当前和上一个时间段的数据 进行比较且返回
        DateLimitUtilVo dateRange = CrmebDateUtil.getDateLimit(dateLimit);
        String dateStartD = dateRange.getStartTime();
        String dateEndD = dateRange.getEndTime();
        int days = CrmebDateUtil.daysBetween(
                CrmebDateUtil.strToDate(dateStartD,Constants.DATE_FORMAT_DATE),
                CrmebDateUtil.strToDate(dateEndD,Constants.DATE_FORMAT_DATE)
        );
        // 同时间区间的上一个时间起点
        String perDateStart = CrmebDateUtil.addDay(
                CrmebDateUtil.strToDate(dateStartD,Constants.DATE_FORMAT_DATE), -days, Constants.DATE_FORMAT_START);
        // 当前时间区间
        String dateStart = CrmebDateUtil.addDay(
                CrmebDateUtil.strToDate(dateStartD,Constants.DATE_FORMAT_DATE),0,Constants.DATE_FORMAT_START);
        String dateEnd = CrmebDateUtil.addDay(
                CrmebDateUtil.strToDate(dateEndD,Constants.DATE_FORMAT_DATE),0,Constants.DATE_FORMAT_END);

        // 上一个时间段查询
        List<StoreOrder> orderPerList = getOrderPayedByDateLimit(perDateStart,dateStart);

        // 当前时间段
        List<StoreOrder> orderCurrentList = getOrderPayedByDateLimit(dateStart, dateEnd);
        double increasePrice = 0;
        if (type == 1) {
            double perSumPrice = orderPerList.stream().mapToDouble(e -> e.getPayPrice().doubleValue()).sum();
            double currentSumPrice = orderCurrentList.stream().mapToDouble(e -> e.getPayPrice().doubleValue()).sum();

            response.setChart(dao.getOrderStatisticsPriceDetail(new StoreDateRangeSqlPram(dateStart,dateEnd)));
            response.setTime(BigDecimal.valueOf(currentSumPrice).setScale(2,BigDecimal.ROUND_HALF_UP));
            // 当前营业额和上一个同比营业额增长区间
            increasePrice = currentSumPrice - perSumPrice;
            if (increasePrice <= 0) response.setGrowthRate(0);
            else if (perSumPrice == 0) response.setGrowthRate((int) increasePrice * 100);
            else response.setGrowthRate((int)((increasePrice * perSumPrice) * 100));
        }else if (type ==2) {
            response.setChart(dao.getOrderStatisticsOrderCountDetail(new StoreDateRangeSqlPram(dateStart,dateEnd)));
            response.setTime(BigDecimal.valueOf(orderCurrentList.size()));
            increasePrice = orderCurrentList.size() - orderPerList.size();
            if (increasePrice <= 0) response.setGrowthRate(0);
            else if (orderPerList.size() == 0) response.setGrowthRate((int) increasePrice);
            else response.setGrowthRate((int)((increasePrice / orderPerList.size()) * 100));
        }
        response.setIncreaseTime(increasePrice+"");
        response.setIncreaseTimeStatus(increasePrice >= 0 ? 1:2);
        return response;
    }

    /**
     * 获取用户当天的秒杀数量
     *
     * @param uid 用户uid
     * @param seckillId 秒杀商品id
     * @return 用户当天的秒杀商品订单数量
     */
    @Override
    public List<StoreOrder> getUserCurrentDaySecKillOrders(Integer uid, Integer seckillId) {
        String dayStart = CrmebDateUtil.nowDateTime(Constants.DATE_FORMAT_START);
        String dayEnd = CrmebDateUtil.nowDateTime(Constants.DATE_FORMAT_END);
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getUid, uid);
        lqw.eq(StoreOrder::getSeckillId, seckillId);
        lqw.between(StoreOrder::getCreateTime, dayStart, dayEnd);
        lqw.eq(StoreOrder::getIsDel, false);
        return dao.selectList(lqw);
    }

    /**
     * 获取用户当前的砍价订单
     * @param uid    用户uid
     * @return  用户当前的砍价订单
     */
    @Override
    public List<StoreOrder> getUserCurrentBargainOrders(Integer uid, Integer bargainId) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getUid, uid);
        lqw.eq(StoreOrder::getBargainId, bargainId);
        lqw.eq(StoreOrder::getIsDel, false);
        return dao.selectList(lqw);
    }

    /**
     * 获取用户当前的拼团订单
     * @param uid    用户uid
     * @return  用户当前的拼团订单
     */
    @Override
    public List<StoreOrder> getUserCurrentCombinationOrders(Integer uid, Integer combinationId) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getUid, uid);
        lqw.eq(StoreOrder::getCombinationId, combinationId);
        lqw.eq(StoreOrder::getIsDel, false);
        return dao.selectList(lqw);
    }

    @Override
    public StoreOrder getByOderId(String orderId) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getOrderId, orderId);
        return dao.selectOne(lqw);
    }

    /**
     * 获取面单默认配置信息
     * @return ExpressSheetVo
     */
    @Override
    public ExpressSheetVo getDeliveryInfo() {
        return systemConfigService.getDeliveryInfo();
    }

    /**
     * 更新支付结果
     * @param orderNo 订单编号
     * @return Boolean
     */
    @Override
    public Boolean updatePaid(String orderNo) {
        LambdaUpdateWrapper<StoreOrder> lqw = new LambdaUpdateWrapper<>();
        lqw.set(StoreOrder::getPaid, true);
        lqw.set(StoreOrder::getPayTime, CrmebDateUtil.nowDateTime());
        lqw.eq(StoreOrder::getOrderId, orderNo);
        lqw.eq(StoreOrder::getPaid,false);
        return update(lqw);
    }

    /**
     * 跟据订单号列表获取订单列表Map
     * @param orderNoList 订单号列表
     * @return Map
     */
    @Override
    public Map<String, StoreOrder> getMapInOrderNo(List<String> orderNoList) {
        Map<String, StoreOrder> map = CollUtil.newHashMap();
        LambdaUpdateWrapper<StoreOrder> lqw = new LambdaUpdateWrapper<>();
        lqw.in(StoreOrder::getOrderId, orderNoList);
        List<StoreOrder> orderList = dao.selectList(lqw);
        orderList.forEach(order -> {
            map.put(order.getOrderId(), order);
        });
        return map;
    }

    /**
     * 获取推广订单总金额
     * @param orderNoList 订单编号列表
     * @return BigDecimal
     */
    @Override
    public BigDecimal getSpreadOrderTotalPriceByOrderList(List<String> orderNoList) {
        LambdaQueryWrapper<StoreOrder> lqw = new LambdaQueryWrapper<>();
        lqw.select(StoreOrder::getPayPrice);
        lqw.in(StoreOrder::getOrderId, orderNoList);
        List<StoreOrder> orderList = dao.selectList(lqw);
        return orderList.stream().map(StoreOrder::getPayPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取所有收货订单id集合
     * @return List<StoreOrder>
     */
    @Override
    public List<StoreOrder> findIdAndUidListByReceipt() {
        LambdaQueryWrapper<StoreOrder> lqw = new LambdaQueryWrapper<>();
        lqw.select(StoreOrder::getId, StoreOrder::getUid);
        lqw.eq(StoreOrder::getStatus, 2);
        lqw.eq(StoreOrder::getRefundStatus, 0);
        lqw.eq(StoreOrder::getIsDel, false);
        List<StoreOrder> orderList = dao.selectList(lqw);
        if (CollUtil.isEmpty(orderList)) {
            return CollUtil.newArrayList();
        }
        return orderList;
    }

    /**
     *
     * @param userId 用户uid
     * @param pageParamRequest 分页参数
     * @return List
     */
    @Override
    public List<StoreOrder> findPaidListByUid(Integer userId, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<StoreOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StoreOrder::getUid, userId);
        lqw.eq(StoreOrder::getPaid, true);
        lqw.eq(StoreOrder::getIsDel, false);
        lqw.lt(StoreOrder::getRefundStatus, 2);
        lqw.orderByDesc(StoreOrder::getId);
        return dao.selectList(lqw);
    }

    /**
     * 订单改价
     * @param request 改价请求对象
     * @return 改价结果
     */
    @Override
    public Boolean updatePrice(StoreOrderUpdatePriceRequest request) {
        StoreOrder existOrder = getInfoException(request.getOrderNo());
        // 订单已支付
        if (existOrder.getPaid()) {
            throw new CrmebException(StrUtil.format("订单号为 {} 的订单已支付", existOrder.getOrderId()));
        }
        if (existOrder.getIsAlterPrice()) {
            throw new CrmebException("系统只支持一次改价");
        }
        // 修改价格和原来价格相同
        if (existOrder.getPayPrice().compareTo(request.getPayPrice()) ==0) {
            throw new CrmebException(StrUtil.format("修改价格不能和原支付价格相同 原价 {} 修改价 {}", existOrder.getPayPrice(), request.getPayPrice()));
        }
        String oldPrice = existOrder.getPayPrice().toString();

        Boolean execute = transactionTemplate.execute(e -> {
            // 修改订单价格
            orderEditPrice(existOrder.getOrderId(), request.getPayPrice(), existOrder.getPayPrice());
            // 订单修改状态操作
            storeOrderStatusService.createLog(existOrder.getId(), Constants.ORDER_LOG_EDIT,
                    Constants.RESULT_ORDER_EDIT_PRICE_LOGS.replace("${orderPrice}", oldPrice)
                            .replace("${price}", request.getPayPrice().toString()));
            return Boolean.TRUE;
        });
        if (!execute) {
            throw new CrmebException(Constants.RESULT_ORDER_EDIT_PRICE_SUCCESS
                    .replace("${orderNo}", existOrder.getOrderId()).replace("${price}", request.getPayPrice().toString()));
        }
        // 发送改价短信提醒
        SystemNotification notification = systemNotificationService.getByMark(NotifyConstants.MODIFY_ORDER_PRICE_MARK);
        if (notification.getIsSms().equals(1)) {
            User user = userService.getById(existOrder.getUid());
            if (StrUtil.isNotBlank(user.getPhone())) {
                SmsTemplate smsTemplate = smsTemplateService.getDetail(notification.getSmsId());
                // 发送改价短信提醒
                smsService.sendOrderEditPriceNotice(user.getPhone(), existOrder.getOrderId(), request.getPayPrice(), Integer.valueOf(smsTemplate.getTempId()));
            }
        }

        return execute;
    }

    /**
     * 获取订单总数量
     * @param uid 用户uid
     * @return Integer
     */
    @Override
    public Integer getOrderCountByUid(Integer uid) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getPaid, true);
        lqw.eq(StoreOrder::getIsDel, false);
        lqw.eq(StoreOrder::getUid, uid);
        lqw.lt(StoreOrder::getRefundStatus, 2);
        return dao.selectCount(lqw);
    }

    /**
     * 获取用户总消费金额
     * @param userId 用户uid
     * @return BigDecimal
     */
    @Override
    public BigDecimal getSumPayPriceByUid(Integer userId) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.select(StoreOrder::getPayPrice);
        lqw.eq(StoreOrder::getPaid, true);
        lqw.eq(StoreOrder::getIsDel, false);
        lqw.eq(StoreOrder::getUid, userId);
        lqw.lt(StoreOrder::getRefundStatus, 2);
        List<StoreOrder> orderList = dao.selectList(lqw);
        return orderList.stream().map(StoreOrder::getPayPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取订单数量(时间)
     * @param uid 用户uid
     * @return Integer
     */
    @Override
    public Integer getOrderCountByUidAndDate(Integer uid, String date) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getPaid, true);
        lqw.eq(StoreOrder::getIsDel, false);
        lqw.eq(StoreOrder::getUid, uid);
        lqw.lt(StoreOrder::getRefundStatus, 2);
        if (StrUtil.isNotBlank(date)) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(date);
            lqw.between(StoreOrder::getCreateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        return dao.selectCount(lqw);
    }

    /**
     * 获取用户消费金额(时间)
     * @param userId 用户uid
     * @return BigDecimal
     */
    @Override
    public BigDecimal getSumPayPriceByUidAndDate(Integer userId, String date) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.select(StoreOrder::getPayPrice);
        lqw.eq(StoreOrder::getPaid, true);
        lqw.eq(StoreOrder::getIsDel, false);
        lqw.eq(StoreOrder::getUid, userId);
        lqw.lt(StoreOrder::getRefundStatus, 2);
        if (StrUtil.isNotBlank(date)) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(date);
            lqw.between(StoreOrder::getCreateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        List<StoreOrder> orderList = dao.selectList(lqw);
        return orderList.stream().map(StoreOrder::getPayPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取砍价订单
     * @param bargainId 砍价商品id
     * @param bargainUserId 用户砍价活动id
     * @return StoreOrder
     */
    @Override
    public StoreOrder getByBargainOrder(Integer bargainId, Integer bargainUserId) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getBargainId, bargainId);
        lqw.eq(StoreOrder::getBargainUserId, bargainUserId);
        lqw.orderByDesc(StoreOrder::getId);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }

    /**
     * 获取订单状态数量
     * @return StoreOrderCountItemResponse
     */
    @Override
    public StoreOrderCountItemResponse getOrderStatusNum(String dateLimit, Integer type) {
        StoreOrderCountItemResponse response = new StoreOrderCountItemResponse();
        if (type.equals(2)) {
            type = null;
        }
        // 全部订单
        response.setAll(getCount(dateLimit, Constants.ORDER_STATUS_ALL, type));
        // 未支付订单
        response.setUnPaid(getCount(dateLimit, Constants.ORDER_STATUS_UNPAID, type));
        // 未发货订单
        response.setNotShipped(getCount(dateLimit, Constants.ORDER_STATUS_NOT_SHIPPED, type));
        // 待收货订单
        response.setSpike(getCount(dateLimit, Constants.ORDER_STATUS_SPIKE, type));
        // 待评价订单
        response.setBargain(getCount(dateLimit, Constants.ORDER_STATUS_BARGAIN, type));
        // 交易完成订单
        response.setComplete(getCount(dateLimit, Constants.ORDER_STATUS_COMPLETE, type));
        // 待核销订单
        response.setToBeWrittenOff(getCount(dateLimit, Constants.ORDER_STATUS_TOBE_WRITTEN_OFF, type));
        // 退款中订单
        response.setRefunding(getCount(dateLimit, Constants.ORDER_STATUS_REFUNDING, type));
        // 已退款订单
        response.setRefunded(getCount(dateLimit, Constants.ORDER_STATUS_REFUNDED, type));
        // 已删除订单
        response.setDeleted(getCount(dateLimit, Constants.ORDER_STATUS_DELETED, type));
        return response;
    }

    /**
     * 获取订单统计数据
     * @param dateLimit 时间端
     * @return StoreOrderTopItemResponse
     */
    @Override
    public StoreOrderTopItemResponse getOrderData(String dateLimit) {
        StoreOrderTopItemResponse itemResponse = new StoreOrderTopItemResponse();
        // 订单数量
        itemResponse.setCount(getCount(dateLimit, Constants.ORDER_STATUS_ALL));
        // 订单金额
        itemResponse.setAmount(getAmount(dateLimit, ""));
        // 微信支付金额
        itemResponse.setWeChatAmount(getAmount(dateLimit, Constants.PAY_TYPE_WE_CHAT));
        // 余额支付金额
        itemResponse.setYueAmount(getAmount(dateLimit, Constants.PAY_TYPE_YUE));
        return itemResponse;
    }

    /**
     * 订单删除
     * @param orderNo 订单编号
     * @return Boolean
     */
    @Override
    public Boolean delete(String orderNo) {
        StoreOrder storeOrder = getInfoException(orderNo);
        if (!storeOrder.getIsDel()) {
            throw new CrmebException("您选择的的订单存在用户未删除的订单，无法删除用户未删除的订单！");
        }
        if (storeOrder.getIsSystemDel()) {
            throw new CrmebException("此订单已经被删除了!");
        }
        storeOrder.setIsSystemDel(true);
        return updateById(storeOrder);
    }

    /**
     * 视频订单发货
     * @param request 发货请求参数
     * @return Boolean
     */
    @Override
    public Boolean videoSend(VideoOrderSendRequest request) {
        //订单信息
        StoreOrder storeOrder = getInfoException(request.getOrderNo());
        if (storeOrder.getIsDel()) throw new CrmebException("订单已删除,不能发货!");
        if (storeOrder.getStatus() > 0) throw new CrmebException("订单已发货请勿重复操作!");
        PayComponentOrder componentOrder = componentOrderService.getByOrderNo(request.getOrderNo());
        if (ObjectUtil.isNull(componentOrder)) {
            throw new CrmebException("为找到对应视频订单");
        }
        if (!componentOrder.getStatus().equals(20)) {
            throw new CrmebException("视频订单不处于可发货状态");
        }
        PayComponentDeliveryCompany deliveryCompany = componentDeliveryCompanyService.getByDeliveryId(request.getDeliveryId());
        if (ObjectUtil.isNull(deliveryCompany)) {
            throw new CrmebException("快递公司不存在");
        }
        DeliverySendVo deliverySendVo = new DeliverySendVo();
        deliverySendVo.setOutOrderId(componentOrder.getOrderNo());
        deliverySendVo.setOpenid(componentOrder.getOpenid());
        deliverySendVo.setFinishSllDelivery(1);
        DeliveryInfoVo deliveryInfoVo = new DeliveryInfoVo();
        deliveryInfoVo.setDeliveryId(request.getDeliveryId());
        deliveryInfoVo.setWaybillId(request.getWaybillId());
        List<DeliveryInfoVo> deliveryInfoVoList = CollUtil.newArrayList();
        deliveryInfoVoList.add(deliveryInfoVo);
        deliverySendVo.setDeliveryList(deliveryInfoVoList);
        Boolean deliverySend = wechatVideoDeliveryService.shopDeliverySend(deliverySendVo);
        if (!deliverySend) {
            throw new CrmebException("发货失败");
        }
        storeOrder.setDeliveryCode(deliveryCompany.getDeliveryId());
        storeOrder.setDeliveryName(deliveryCompany.getDeliveryName());
        storeOrder.setDeliveryId(request.getWaybillId());
        storeOrder.setStatus(1);
        storeOrder.setDeliveryType("express");

        componentOrder.setStatus(30);
        componentOrder.setFinishAllDelivery(deliverySendVo.getFinishSllDelivery());
        componentOrder.setDeliveryList(JSONArray.toJSONString(deliverySendVo.getDeliveryList()));

        String message = Constants.ORDER_LOG_MESSAGE_EXPRESS.replace("{deliveryName}", deliveryCompany.getDeliveryName()).replace("{deliveryCode}", request.getWaybillId());

        Boolean execute = transactionTemplate.execute(i -> {
            updateById(storeOrder);
            componentOrderService.updateById(componentOrder);
            //订单记录增加
            storeOrderStatusService.createLog(storeOrder.getId(), Constants.ORDER_LOG_EXPRESS, message);
            return Boolean.TRUE;
        });

        if (!execute) throw new CrmebException("快递发货失败！");

        sendGoodsNotify(storeOrder);
        return true;
    }

    /**
     * 通过日期获取商品交易件数
     * @param date 日期，yyyy-MM-dd格式
     * @return Integer
     */
    @Override
    public Integer getOrderProductNumByDate(String date) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(total_num), 0) as total_num");
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        StoreOrder storeOrder = dao.selectOne(wrapper);
        return storeOrder.getTotalNum();
    }

    /**
     * 通过日期获取商品交易成功件数
     * @param date 日期，yyyy-MM-dd格式
     * @return Integer
     */
    @Override
    public Integer getOrderSuccessProductNumByDate(String date) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(total_num), 0) as total_num");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        StoreOrder storeOrder = dao.selectOne(wrapper);
        return storeOrder.getTotalNum();
    }

    /**
     * 通过日期获取订单数量
     * @param date 日期，yyyy-MM-dd格式
     * @return Integer
     */
    @Override
    public Integer getOrderNumByDate(String date) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        return dao.selectCount(wrapper);
    }

    /**
     * 通过日期获取支付订单数量
     * @param date 日期，yyyy-MM-dd格式
     * @return Integer
     */
    @Override
    public Integer getPayOrderNumByDate(String date) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        return dao.selectCount(wrapper);
    }

    /**
     * 通过日期获取支付订单金额
     * @param date 日期，yyyy-MM-dd格式
     * @return BigDecimal
     */
    @Override
    public BigDecimal getPayOrderAmountByDate(String date) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(pay_price), 0) as pay_price");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        StoreOrder storeOrder = dao.selectOne(wrapper);
        return storeOrder.getPayPrice();
    }

    /**
     * 通过日期获取支付订单金额
     * @param startDate 日期
     * @param endDate 日期
     * @return BigDecimal
     */
    @Override
    public BigDecimal getPayOrderAmountByPeriod(String startDate, String endDate) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(pay_price), 0) as pay_price");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') between {0} and {1}", startDate, endDate);
        StoreOrder storeOrder = dao.selectOne(wrapper);
        return storeOrder.getPayPrice();
    }

    /**
     * 通过日期获取余额支付订单支付金额
     * @param date 日期，yyyy-MM-dd格式
     * @return BigDecimal
     */
    @Override
    public BigDecimal getYuePayOrderAmountByDate(String date) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(pay_price), 0) as pay_price");
        wrapper.eq("paid", 1);
        wrapper.eq("pay_type", "yue");
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        StoreOrder storeOrder = dao.selectOne(wrapper);
        return storeOrder.getPayPrice();
    }

    /**
     * 获取累计消费金额
     * @return BigDecimal
     */
    @Override
    public BigDecimal getTotalPrice() {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(pay_price), 0) as pay_price");
        wrapper.eq("paid", 1);
        StoreOrder storeOrder = dao.selectOne(wrapper);
        return storeOrder.getPayPrice();
    }

    /**
     * 根据日期获取下单用户数量
     * @param date 日期
     * @return Integer
     */
    @Override
    public Integer getOrderUserNumByDate(String date) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        wrapper.groupBy("uid");
        List<StoreOrder> orderList = dao.selectList(wrapper);
        if (CollUtil.isEmpty(orderList)) {
            return 0;
        }
        return orderList.size();
    }

    /**
     * 根据日期获取下单用户数量
     * @param startDate 日期
     * @param endDate 日期
     * @return Integer
     */
    @Override
    public Integer getOrderUserNumByPeriod(String startDate, String endDate) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.apply("date_format(create_time, '%Y-%m-%d') between {0} and {1}", startDate, endDate);
        wrapper.groupBy("uid");
        List<StoreOrder> orderList = dao.selectList(wrapper);
        if (CollUtil.isEmpty(orderList)) {
            return 0;
        }
        return orderList.size();
    }

    /**
     * 根据日期获取成交用户数量
     * @param date 日期
     * @return Integer
     */
    @Override
    public Integer getOrderPayUserNumByDate(String date) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        wrapper.groupBy("uid");
        List<StoreOrder> orderList = dao.selectList(wrapper);
        if (CollUtil.isEmpty(orderList)) {
            return 0;
        }
        return orderList.size();
    }

    /**
     * 根据日期获取成交用户数量
     * @param startDate 日期
     * @param endDate 日期
     * @return Integer
     */
    @Override
    public Integer getOrderPayUserNumByPeriod(String startDate, String endDate) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') between {0} and {1}", startDate, endDate);
        wrapper.groupBy("uid");
        List<StoreOrder> orderList = dao.selectList(wrapper);
        if (CollUtil.isEmpty(orderList)) {
            return 0;
        }
        return orderList.size();
    }

    /**
     * 根据用户uid列表获取成交用户数量
     * @param uidList 用户列表
     * @return Integer
     */
    @Override
    public Integer getOrderPayUserNumByUidList(List<Integer> uidList) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("paid", 1);
        wrapper.in("uid", uidList);
        wrapper.groupBy("uid");
        List<StoreOrder> orderList = dao.selectList(wrapper);
        if (CollUtil.isEmpty(orderList)) {
            return 0;
        }
        return orderList.size();
    }

    /**
     * 根据用户uid列表获取支付金额
     * @param uidList 用户列表
     * @return BigDecimal
     */
    @Override
    public BigDecimal getPayOrderAmountByUidList(List<Integer> uidList) {
        QueryWrapper<StoreOrder> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(pay_price), 0.00) as pay_price");
//        wrapper.select("ifnull(if(sum(pay_price) = 0.00, 0, sum(pay_price)), 0) as pay_price");
        wrapper.eq("paid", 1);
        wrapper.in("uid", uidList);
        List<StoreOrder> orderList = dao.selectList(wrapper);
        if (CollUtil.isEmpty(orderList)) {
            return BigDecimal.ZERO;
        }
        return orderList.stream().map(StoreOrder::getPayPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取待发货订单数量
     * @return Integer
     */
    @Override
    public Integer getNotShippingNum() {
        return getCount("", Constants.ORDER_STATUS_NOT_SHIPPED);
    }

    /**
     * 获取退款中订单数量
     */
    @Override
    public Integer getRefundingNum() {
        return getCount("", Constants.ORDER_STATUS_REFUNDING);
    }

    /**
     * 获取待核销订单数量
     */
    @Override
    public Integer getNotWriteOffNum() {
        return getCount("", Constants.ORDER_STATUS_TOBE_WRITTEN_OFF);
    }

    /**
     * 获取佣金相关数据
     * @param uid 用户uid
     * @param spreadId 推广人uid
     */
    @Override
    public OrderBrokerageData getBrokerageData(Integer uid, Integer spreadId) {
        return dao.getBrokerageData(uid, spreadId);
    }

    /**
     * 获取待收货订单
     * @param sendTime 发货时间
     * @return List
     */
    @Override
    public List<StoreOrder> findAwaitTakeDeliveryOrderList(String sendTime) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getStatus, Constants.ORDER_STATUS_INT_SPIKE);
        lqw.ne(StoreOrder::getRefundStatus, 3);
        lqw.le(StoreOrder::getUpdateTime, sendTime);
        lqw.eq(StoreOrder::getIsDel, false);
        return dao.selectList(lqw);
    }

    /**
     * 更改订单运单号
     */
    @Override
    public Boolean updateTrackingNumber(StoreOrderSendRequest request) {
        StoreOrder storeOrder = getInfoException(request.getOrderNo());
        if (storeOrder.getIsDel()) throw new CrmebException("订单已删除,不能修改运单号!");
        if (storeOrder.getStatus() != 1) throw new CrmebException("待收货订单才能修改运单号");

        switch (storeOrder.getDeliveryType()) {
            case "express":// 发货
                express(request, storeOrder);
                break;
            case "send":// 送货
                delivery(request, storeOrder);
                break;
            case "fictitious":// 虚拟
                virtual(request, storeOrder);
                break;
            default:
                throw new CrmebException("类型错误");
        }

        return transactionTemplate.execute(e -> {
            updateById(storeOrder);
            storeOrderStatusService.createLog(storeOrder.getId(), Constants.ORDER_LOG_EXPRESS, StrUtil.format("变更运单号,快递公司:{},运单号:{}", storeOrder.getDeliveryName(), storeOrder.getDeliveryId()));
            return Boolean.TRUE;
        });
    }



    /**
     *  一号通商家寄件
     * @param request 订单发货请求对象
     * @param storeOrder 主订单请求对象
     */
    @Override
    public void expressForOnePassShipment(StoreOrderSendRequest request, StoreOrder storeOrder) {
        // 校验快递发货参数
//        validateExpressSend(request);
        OnePassShipmentCreateOrderRequest shipment = request.getShipment();
        shipment.setManName(storeOrder.getRealName());
        shipment.setPhone(storeOrder.getUserPhone());
        shipment.setAddress(storeOrder.getUserAddress());

        if(ObjectUtil.isNotNull(shipment.getKuaidicom()) &&
                (shipment.getKuaidicom().equalsIgnoreCase("jd") || shipment.getKuaidicom().equalsIgnoreCase("yuantong"))){
            List<StoreOrderInfo> listByOrderNo = storeOrderInfoService.getListByOrderNo(storeOrder.getOrderId());
            shipment.setCargo(listByOrderNo.get(0).getProductName().substring(0,10));
        }
        JSONObject jsonObject = onePassService.shipmentCreateOrder(shipment);
        // 任务订单号（需要在系统回调中使用
        String orderId = jsonObject.getString("order_id");
        // 任务ID（需要在系统回调中使用）
        String taskId = jsonObject.getString("task_id");
        // 物流单号
        String kuaidinum = jsonObject.getString("kuaidinum");

        logger.info("一号通-商家寄件结果:{}", jsonObject);
        storeOrder.setShipmentTaskId(taskId);
        storeOrder.setShipmentOrderId(orderId);
        storeOrder.setShipmentPic(kuaidinum);
        storeOrder.setDeliveryCode(request.getShipment().getKuaidicom());
        storeOrder.setDeliveryName(request.getShipment().getSendRealName());
        // 更新商家发货结果到数据库，再根据任务订单id在回调中更新最终的发货状态
        updateById(storeOrder);
    }

    /**
     * 商家寄件 取件 回调方法
     * @param jsonObject 回调数据
     */
    @Override
    public void expressForOnePassShipmentTakeCallBack(JSONObject jsonObject){
        String orderId = jsonObject.getString("id");
        StoreOrder currentStoreOrder = getByOderId(orderId);
        currentStoreOrder.setStatus(1);
        currentStoreOrder.setDeliveryType(Constants.ORDER_LOG_SHIPMENT);

        String message = Constants.ORDER_LOG_MESSAGE_EXPRESS.replace("{deliveryName}", currentStoreOrder.getShipmentNum()).replace("{deliveryCode}", currentStoreOrder.getShipmentNum());

        Boolean execute = transactionTemplate.execute(i -> {
            updateById(currentStoreOrder);
            //订单记录增加
            storeOrderStatusService.createLog(currentStoreOrder.getId(), Constants.ORDER_LOG_SHIPMENT, message);
            return Boolean.TRUE;
        });

        if (!execute) throw new CrmebException("一号通-商家寄件 发货失败！");

        sendGoodsNotify(currentStoreOrder);
    }

    /**
     * 一号通商家寄件 取消寄件回调
     *
     * @param jsonObject 回调结果
     */
    @Override
    public void expressForOnePassShipmentCancelCallBack(JSONObject jsonObject) {
        String orderId = jsonObject.getString("id");
        StoreOrder currentStoreOrder = getByOderId(orderId);
        currentStoreOrder.setStatus(0);
        currentStoreOrder.setDeliveryType("");
        currentStoreOrder.setShipmentPic("");
        currentStoreOrder.setShipmentOrderId("");
        currentStoreOrder.setShipmentNum("");
        currentStoreOrder.setShipmentTaskId("");

        Boolean execute = transactionTemplate.execute(i -> {
            updateById(currentStoreOrder);
            //订单记录增加
            storeOrderStatusService.createLog(currentStoreOrder.getId(), Constants.ORDER_LOG_SHIPMENT, "一号通 商家寄件 取消寄件");
            return Boolean.TRUE;
        });

        if (!execute) throw new CrmebException("一号通-商家寄件 取消失败！");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////// 以下为自定义方法

    /**
     * 根据时间参数获取有效订单
     * @return 有效订单列表
     */
    private List<StoreOrder> getOrderPayedByDateLimit(String startTime, String endTime) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getIsDel, false).eq(StoreOrder::getPaid, true).eq(StoreOrder::getRefundStatus,0)
                .between(StoreOrder::getCreateTime, startTime, endTime);
     return dao.selectList(lqw);
    }

    private StoreOrder getInfoException(String orderNo) {
        LambdaQueryWrapper<StoreOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreOrder::getOrderId, orderNo);
        StoreOrder storeOrder = dao.selectOne(lqw);
        if (ObjectUtil.isNull(storeOrder)) {
            throw new CrmebException("没有找到订单信息");
        }
        return storeOrder;
    }

    /** 快递
     * @param request StoreOrderSendRequest 发货参数
     * @param storeOrder StoreOrder 订单信息
     */
    private String express(StoreOrderSendRequest request, StoreOrder storeOrder) {
        String mianDianResult = "";
        //快递公司信息
        Express express = expressService.getByCode(request.getExpressCode());
        if (request.getExpressRecordType().equals("1")) { // 正常发货
            validateExpressSend(request); // 校验快递发货参数
            deliverGoods(request, storeOrder);
        }
        if (request.getExpressRecordType().equals("2")) { // 电子面单
            request.setExpressName(express.getName());
            validateExpressSend(request); // 校验快递发货参数
            mianDianResult = expressDump(request, storeOrder, express);
        }
        if(request.getExpressRecordType().equals("3")){ // 一号通-商家发货
            expressForOnePassShipment(request, storeOrder);
        }

        storeOrder.setDeliveryCode(express.getCode());
        storeOrder.setDeliveryName(express.getName());
        storeOrder.setStatus(1);
        storeOrder.setDeliveryType("express");
        storeOrder.setExpressRecordType(request.getExpressRecordType());

        String message = Constants.ORDER_LOG_MESSAGE_EXPRESS.replace("{deliveryName}", express.getName()).replace("{deliveryCode}", storeOrder.getDeliveryId());

        Boolean execute = transactionTemplate.execute(i -> {
            updateById(storeOrder);
            //订单记录增加
            storeOrderStatusService.createLog(storeOrder.getId(), Constants.ORDER_LOG_EXPRESS, message);
            return Boolean.TRUE;
        });

        if (!execute) throw new CrmebException("快递发货失败！");

        sendGoodsNotify(storeOrder);

        // 小程序发货管理
        if (storeOrder.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT) && storeOrder.getIsChannel().equals(1)) {
            String shippingSwitch = systemConfigService.getValueByKey(WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH);
            if (StrUtil.isNotBlank(shippingSwitch) && shippingSwitch.equals("1")) {
                wechatOrderShippingService.uploadShippingInfo(storeOrder.getOrderId());
            }
        }
        return mianDianResult;
    }

    /**
     * 发货通知
     * @param storeOrder 订单
     */
    private void sendGoodsNotify(StoreOrder storeOrder) {
        User user = userService.getById(storeOrder.getUid());
        SystemNotification notification = systemNotificationService.getByMark(NotifyConstants.DELIVER_GOODS_MARK);
        if (notification.getIsSms().equals(1)) {
            // 发货短信提醒
            if (StrUtil.isNotBlank(user.getPhone())) {
                SmsTemplate smsTemplate = smsTemplateService.getDetail(notification.getSmsId());
                String proName = "";
                List<StoreOrderInfoOldVo> voList = storeOrderInfoService.getOrderListByOrderId(storeOrder.getId());
                proName = voList.get(0).getInfo().getProductName();
                if (voList.size() > 1) {
                    proName = proName.concat("等");
                }
                smsService.sendOrderDeliverNotice(user.getPhone(), user.getNickname(), proName, storeOrder.getOrderId(), Integer.valueOf(smsTemplate.getTempId()));
            }
        }

        // 发送消息通知
        pushMessageOrder(storeOrder, user, notification);
    }

    /**
     * 发送消息通知
     * 根据用户类型发送
     * 公众号模板消息
     * 小程序订阅消息
     */
    private void pushMessageOrder(StoreOrder storeOrder, User user, SystemNotification notification) {
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
            temMap.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "订单发货提醒");
            temMap.put("keyword1", storeOrder.getOrderId());
            temMap.put("keyword2", DateUtil.now());
            temMap.put("keyword3", storeOrder.getDeliveryName());
            temMap.put("keyword4", storeOrder.getDeliveryId());
            temMap.put(Constants.WE_CHAT_TEMP_KEY_END, "欢迎再次购买！");
            templateMessageService.pushTemplateMessage(notification.getWechatId(), temMap, userToken.getToken());
            return;
        } else if (notification.getIsRoutine().equals(1)) {
            // 小程序发送订阅消息
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 组装数据
            // 注释部分为丰享汇小程序
//        temMap.put("character_string1", storeOrder.getOrderId());
//        temMap.put("name3", storeOrder.getDeliveryName());
//        temMap.put("character_string4", storeOrder.getDeliveryId());
//        temMap.put("thing7", "您的订单已发货");
            // 放开部分为一码秦川小程序
            temMap.put("character_string1", storeOrder.getOrderId());
            temMap.put("name6", storeOrder.getDeliveryName());
            temMap.put("character_string7", storeOrder.getDeliveryId());
            temMap.put("thing11", "您的订单已发货");
            templateMessageService.pushMiniTemplateMessage(notification.getRoutineId(), temMap, userToken.getToken());
        }
    }

    /**
     * 电子面单
     * @param request
     * @param storeOrder
     * @param express
     */
    private String expressDump(StoreOrderSendRequest request, StoreOrder storeOrder, Express express) {
        String configExportOpen = systemConfigService.getValueByKeyException("config_export_open");
        if (!configExportOpen.equals("1")) {// 电子面单未开启
            throw new CrmebException("请先开启电子面单");
        }
        MyRecord record = new MyRecord();
        record.set("com", express.getCode());// 快递公司编码
        record.set("to_name", storeOrder.getRealName());// 收件人
        record.set("to_tel", storeOrder.getUserPhone());// 收件人电话
        record.set("to_addr", storeOrder.getUserAddress());// 收件人详细地址
        record.set("from_name", request.getToName());// 寄件人
        record.set("from_tel", request.getToTel());// 寄件人电话
        record.set("from_addr", request.getToAddr());// 寄件人详细地址
        record.set("temp_id", request.getExpressTempId());// 电子面单模板ID
        String siid = systemConfigService.getValueByKeyException("config_export_siid");
        record.set("siid", "");// 云打印机编号
        record.set("print_type", "IMAGE");// 打印图片
        record.set("count", storeOrder.getTotalNum());// 商品数量

        //获取购买商品名称
        List<Integer> orderIdList = new ArrayList<>();
        orderIdList.add(storeOrder.getId());
        HashMap<Integer, List<StoreOrderInfoOldVo>> orderInfoMap = StoreOrderInfoService.getMapInId(orderIdList);
        if (orderInfoMap.isEmpty() || !orderInfoMap.containsKey(storeOrder.getId())) {
            throw new CrmebException("没有找到购买的商品信息");
        }
        List<String> productNameList = new ArrayList<>();
        for (StoreOrderInfoOldVo storeOrderInfoVo : orderInfoMap.get(storeOrder.getId())) {
            productNameList.add(storeOrderInfoVo.getInfo().getProductName());
        }

        record.set("cargo", String.join(",", productNameList));// 物品名称
        if (express.getPartnerId()) {
            record.set("partner_id", express.getAccount());// 电子面单月结账号(部分快递公司必选)
        }
        if (express.getPartnerKey()) {
            record.set("partner_key", express.getPassword());// 电子面单密码(部分快递公司必选)
        }
        if (express.getNet()) {
            record.set("net", express.getNetName());// 收件网点名称(部分快递公司必选)
        }

        MyRecord myRecord = onePassService.expressDump(record);
        logger.info("电子面单的返回数据:{}", JSONObject.toJSONString(myRecord));
        storeOrder.setDeliveryId(myRecord.getStr("kuaidinum"));
        return myRecord.getStr("label");
    }

    /**
     * 正常发货
     */
    private void deliverGoods(StoreOrderSendRequest request, StoreOrder storeOrder) {
        storeOrder.setDeliveryId(request.getExpressNumber());
    }

    /**
     * 校验快递发货参数
     */
    private void validateExpressSend(StoreOrderSendRequest request) {
        if (request.getExpressRecordType().equals("1")) {
            if (StrUtil.isBlank(request.getExpressNumber())) throw new CrmebException("请填写快递单号");
            return;
        }
        if (StrUtil.isBlank(request.getExpressCode())) throw new CrmebException("请选择快递公司");
        if (StrUtil.isBlank(request.getExpressRecordType())) throw new CrmebException("请选择发货记录类型");
        if (StrUtil.isBlank(request.getExpressTempId())) throw new CrmebException("请选择电子面单");
        if (StrUtil.isBlank(request.getToName())) throw new CrmebException("请填写寄件人姓名");
        if (StrUtil.isBlank(request.getToTel())) throw new CrmebException("请填写寄件人电话");
        if (StrUtil.isBlank(request.getToAddr())) throw new CrmebException("请填写寄件人地址");
    }

    /** 送货上门
     * @param request StoreOrderSendRequest 发货参数
     * @param storeOrder StoreOrder 订单信息
     * @author Mr.Zhang
     * @since 2020-06-10
     */
    private void delivery(StoreOrderSendRequest request, StoreOrder storeOrder) {
        if (StrUtil.isBlank(request.getDeliveryName())) throw new CrmebException("请输入送货人姓名");
        if (StrUtil.isBlank(request.getDeliveryTel())) throw new CrmebException("请输入送货人电话号码");
        ValidateFormUtil.isPhone(request.getDeliveryTel(), "送货人联系方式");

        //送货信息
        storeOrder.setDeliveryName(request.getDeliveryName());
        storeOrder.setDeliveryId(request.getDeliveryTel());
        storeOrder.setStatus(1);
        storeOrder.setDeliveryType("send");
        storeOrder.setExpressRecordType(request.getExpressRecordType());

        //获取购买商品名称
        List<Integer> orderIdList = new ArrayList<>();
        orderIdList.add(storeOrder.getId());
        HashMap<Integer, List<StoreOrderInfoOldVo>> orderInfoMap = StoreOrderInfoService.getMapInId(orderIdList);
        if (orderInfoMap.isEmpty() || !orderInfoMap.containsKey(storeOrder.getId())) {
            throw new CrmebException("没有找到购买的商品信息");
        }
        List<String> productNameList = new ArrayList<>();
        for (StoreOrderInfoOldVo storeOrderInfoVo : orderInfoMap.get(storeOrder.getId())) {
            productNameList.add(storeOrderInfoVo.getInfo().getProductName());
        }

        String message = Constants.ORDER_LOG_MESSAGE_DELIVERY.replace("{deliveryName}", request.getDeliveryName()).replace("{deliveryCode}", request.getDeliveryTel());

        Boolean execute = transactionTemplate.execute(i -> {
            // 更新订单
            updateById(storeOrder);
            // 订单记录增加
            storeOrderStatusService.createLog(storeOrder.getId(), Constants.ORDER_LOG_DELIVERY, message);
            return Boolean.TRUE;
        });
        if (!execute) throw new CrmebException("订单更新送货失败");

        User user = userService.getById(storeOrder.getUid());
        // 发送消息通知
        pushMessageDeliveryOrder(storeOrder, user, request, productNameList);
        if (storeOrder.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT) && storeOrder.getIsChannel().equals(1)) {
            String shippingSwitch = systemConfigService.getValueByKey(WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH);
            if (StrUtil.isNotBlank(shippingSwitch) && shippingSwitch.equals("1")) {
                wechatOrderShippingService.uploadVirtualShippingInfo(storeOrder.getOrderId());
            }
        }
    }

    /**
     * 发送消息通知
     * 根据用户类型发送
     * 公众号模板消息
     * 小程序订阅消息
     */
    private void pushMessageDeliveryOrder(StoreOrder storeOrder, User user, StoreOrderSendRequest request, List<String> productNameList) {
        if (storeOrder.getIsChannel().equals(2)) {
            return;
        }
        if (!storeOrder.getPayType().equals(Constants.PAY_TYPE_WE_CHAT)) {
            return;
        }
        SystemNotification notification = systemNotificationService.getByMark(NotifyConstants.FULFILLMENT_ORDER_MARK);
        UserToken userToken;
        HashMap<String, String> map = new HashMap<>();
        String proName = "";
        if (CollUtil.isNotEmpty(productNameList)) {
            proName = StringUtils.join(productNameList, "|");
        }
        // 公众号
        if (storeOrder.getIsChannel().equals(Constants.ORDER_PAY_CHANNEL_PUBLIC) && notification.getIsWechat().equals(1)) {
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_WECHAT);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            map.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "订单配送提醒");
            map.put("keyword1", storeOrder.getOrderId());
            map.put("keyword2", CrmebDateUtil.dateToStr(storeOrder.getCreateTime(), Constants.DATE_FORMAT));
            map.put("keyword3", storeOrder.getUserAddress());
            map.put("keyword4", request.getDeliveryName());
            map.put("keyword5", request.getDeliveryTel());
            map.put(Constants.WE_CHAT_TEMP_KEY_END, "欢迎再次购买！");
            // 发送微信模板消息
            templateMessageService.pushTemplateMessage(notification.getWechatId(), map, userToken.getToken());
        } else if (notification.getIsRoutine().equals(1)) {
            // 小程序发送订阅消息
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }

            if (proName.length() > 20) {
                proName = proName.substring(0, 15) + "***";
            }
//        map.put("thing8", proName);
//        map.put("character_string1", storeOrder.getOrderId());
//        map.put("name4", request.getDeliveryName());
//        map.put("phone_number10", request.getDeliveryTel());
            map.put("thing8", proName);
            map.put("character_string1", storeOrder.getOrderId());
            map.put("name4", request.getDeliveryName());
            map.put("phone_number10", request.getDeliveryTel());
            templateMessageService.pushMiniTemplateMessage(notification.getRoutineId(), map, userToken.getToken());
        }
    }

    /** 虚拟
     * @param request StoreOrderSendRequest 发货参数
     * @param storeOrder StoreOrder 订单信息
     * @author Mr.Zhang
     * @since 2020-06-10
     */
    private void virtual(StoreOrderSendRequest request, StoreOrder storeOrder) {
        //快递信息
        storeOrder.setDeliveryType(request.getDeliveryType());
        storeOrder.setStatus(1);
        storeOrder.setExpressRecordType(request.getExpressRecordType());

        Boolean execute = transactionTemplate.execute(i -> {
            updateById(storeOrder);
            //订单记录增加
            storeOrderStatusService.createLog(storeOrder.getId(), Constants.ORDER_LOG_DELIVERY_VI, "虚拟物品发货");
            return Boolean.TRUE;
        });
        if (!execute) throw new CrmebException("虚拟物品发货失败");
    }

    /**
     * 获取订单总数
     * @param dateLimit 时间端
     * @param status String 状态
     * @return Integer
     */
    private Integer getCount(String dateLimit, String status) {
        //总数只计算时间
        QueryWrapper<StoreOrder> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(dateLimit)) {
            DateLimitUtilVo dateLimitUtilVo = CrmebDateUtil.getDateLimit(dateLimit);
            queryWrapper.between("create_time", dateLimitUtilVo.getStartTime(), dateLimitUtilVo.getEndTime());
        }
        getStatusWhereNew(queryWrapper, status);
        return dao.selectCount(queryWrapper);
    }

    /**
     * 获取订单总数
     * @param dateLimit 时间端
     * @param status String 状态
     * @return Integer
     */
    private Integer getCount(String dateLimit, String status, Integer type) {
        //总数只计算时间
        QueryWrapper<StoreOrder> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(dateLimit)) {
            DateLimitUtilVo dateLimitUtilVo = CrmebDateUtil.getDateLimit(dateLimit);
            queryWrapper.between("create_time", dateLimitUtilVo.getStartTime(), dateLimitUtilVo.getEndTime());
        }
        getStatusWhereNew(queryWrapper, status);
        if (ObjectUtil.isNotNull(type)) {
            queryWrapper.eq("type", type);
        }
        return dao.selectCount(queryWrapper);
    }

    /**
     * 获取订单金额
     * @param dateLimit 时间端
     * @param type  支付类型
     * @return Integer
     */
    private BigDecimal getAmount(String dateLimit, String type) {
        QueryWrapper<StoreOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(pay_price) as pay_price");
        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq("pay_type", type);
        }
        queryWrapper.isNotNull("pay_time");
        queryWrapper.eq("paid", 1);
        if (StringUtils.isNotBlank(dateLimit)) {
            DateLimitUtilVo dateLimitUtilVo = CrmebDateUtil.getDateLimit(dateLimit);
            queryWrapper.between("create_time", dateLimitUtilVo.getStartTime(), dateLimitUtilVo.getEndTime());
        }
        StoreOrder storeOrder = dao.selectOne(queryWrapper);
        if (ObjectUtil.isNull(storeOrder)) {
            return BigDecimal.ZERO;
        }
        return storeOrder.getPayPrice();
    }

    /**
     * 获取request的where条件
     * @param queryWrapper QueryWrapper<StoreOrder> 表达式
     * @param request StoreOrderSearchRequest 请求参数
     */
    private void getRequestTimeWhere(QueryWrapper<StoreOrder> queryWrapper, StoreOrderSearchRequest request) {
        if (StringUtils.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimitUtilVo = CrmebDateUtil.getDateLimit(request.getDateLimit());
            queryWrapper.between("create_time", dateLimitUtilVo.getStartTime(), dateLimitUtilVo.getEndTime());
        }
    }

    /**
     * 根据订单状态获取where条件
     * @param queryWrapper QueryWrapper<StoreOrder> 表达式
     * @param status String 类型
     */
    private void getStatusWhereNew(QueryWrapper<StoreOrder> queryWrapper, String status) {
        if (StrUtil.isBlank(status)) {
            return;
        }
        switch (status) {
            case Constants.ORDER_STATUS_ALL: //全部
                break;
            case Constants.ORDER_STATUS_UNPAID: //未支付
                queryWrapper.eq("paid", 0);//支付状态
                queryWrapper.eq("status", 0); //订单状态
                queryWrapper.eq("is_del", 0);//删除状态
                break;
            case Constants.ORDER_STATUS_NOT_SHIPPED: //未发货
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 0);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("shipping_type", 1);//配送方式
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_SPIKE: //待收货
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 1);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_BARGAIN: //待评价
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 2);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_COMPLETE: //交易完成
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 3);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_TOBE_WRITTEN_OFF: //待核销
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 0);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("shipping_type", 2);//配送方式
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_REFUNDING: //退款中
                queryWrapper.eq("paid", 1);
                queryWrapper.in("refund_status", 1,3);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_REFUNDED: //已退款
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("refund_status", 2);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_DELETED: //已删除
                queryWrapper.eq("is_del", 1);
                break;
            default:
                queryWrapper.eq("paid", 1);
                queryWrapper.ne("refund_status", 2);
                break;
        }
        queryWrapper.eq("is_system_del", 0);
    }

    /**
     * 根据订单状态获取where条件
     * @param queryWrapper QueryWrapper<StoreOrder> 表达式
     * @param status String 类型
     */
    private void getStatusWhere(QueryWrapper<StoreOrder> queryWrapper, String status) {
        if (StrUtil.isBlank(status)) {
            return;
        }
        switch (status) {
            case Constants.ORDER_STATUS_UNPAID: //未支付
                queryWrapper.eq("paid", 0);//支付状态
                queryWrapper.eq("status", 0); //订单状态
                queryWrapper.eq("is_del", 0);//删除状态
                break;
            case Constants.ORDER_STATUS_NOT_SHIPPED: //未发货
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 0);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("shipping_type", 1);//配送方式
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_SPIKE: //待收货
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 1);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_BARGAIN: //待评价
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 2);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_COMPLETE: //交易完成
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 3);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_TOBE_WRITTEN_OFF: //待核销
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("status", 0);
                queryWrapper.eq("refund_status", 0);
                queryWrapper.eq("shipping_type", 2);//配送方式
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_REFUNDING: //退款中
                queryWrapper.eq("paid", 1);
                queryWrapper.in("refund_status", 1,3);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_REFUNDED: //已退款
                queryWrapper.eq("paid", 1);
                queryWrapper.eq("refund_status", 2);
                queryWrapper.eq("is_del", 0);
                break;
            case Constants.ORDER_STATUS_DELETED: //已删除
                queryWrapper.eq("is_del", 1);
                break;
            default:
                queryWrapper.eq("paid", 1);
                queryWrapper.ne("refund_status", 2);
                break;
        }
        queryWrapper.eq("is_system_del", 0);
    }

    /**
     * 获取订单状态
     * @param storeOrder StoreOrder 订单信息
     * @author Mr.Zhang
     * @since 2020-06-12
     */
    private Map<String, String> getStatus(StoreOrder storeOrder) {
        Map<String, String> map = new HashMap<>();
        map.put("key", "");
        map.put("value", "");
        if (null == storeOrder) {
            return map;
        }
        // 未支付
        if (!storeOrder.getPaid()
                && storeOrder.getStatus() == 0
                && storeOrder.getRefundStatus() == 0
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_UNPAID);
            map.put("value", Constants.ORDER_STATUS_STR_UNPAID);
            return map;
        }
        // 未发货
        if (storeOrder.getPaid()
                && storeOrder.getStatus() == 0
                && storeOrder.getRefundStatus() == 0
                && storeOrder.getShippingType() == 1
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_NOT_SHIPPED);
            map.put("value", Constants.ORDER_STATUS_STR_NOT_SHIPPED);
            return map;
        }
        // 待收货
        if (storeOrder.getPaid()
                && storeOrder.getStatus() == 1
                && storeOrder.getRefundStatus() == 0
                && storeOrder.getShippingType() == 1
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_SPIKE);
            map.put("value", Constants.ORDER_STATUS_STR_SPIKE);
            return map;
        }
        // 待评价
        if (storeOrder.getPaid()
                && storeOrder.getStatus() == 2
                && storeOrder.getRefundStatus() == 0
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_BARGAIN);
            map.put("value", Constants.ORDER_STATUS_STR_BARGAIN);
            return map;
        }
        // 交易完成
        if (storeOrder.getPaid()
                && storeOrder.getStatus() == 3
                && storeOrder.getRefundStatus() == 0
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_COMPLETE);
            map.put("value", Constants.ORDER_STATUS_STR_COMPLETE);
            return map;
        }
        // 待核销
        if (storeOrder.getPaid()
                && storeOrder.getStatus() == 0
                && storeOrder.getRefundStatus() == 0
                && storeOrder.getShippingType() == 2
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_TOBE_WRITTEN_OFF);
            map.put("value", Constants.ORDER_STATUS_STR_TOBE_WRITTEN_OFF);
            return map;
        }

        //申请退款
        if (storeOrder.getPaid()
                && storeOrder.getRefundStatus() == 1
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_APPLY_REFUNDING);
            map.put("value", Constants.ORDER_STATUS_STR_APPLY_REFUNDING);
            return map;
        }

        //退款中
        if (storeOrder.getPaid()
                && storeOrder.getRefundStatus() == 3
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_REFUNDING);
            map.put("value", Constants.ORDER_STATUS_STR_REFUNDING);
            return map;
        }

        //已退款
        if (storeOrder.getPaid()
                && storeOrder.getRefundStatus() == 2
                && !storeOrder.getIsDel()
                && !storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_REFUNDED);
            map.put("value", Constants.ORDER_STATUS_STR_REFUNDED);
        }

        //已删除
        if (storeOrder.getIsDel() || storeOrder.getIsSystemDel()) {
            map.put("key", Constants.ORDER_STATUS_DELETED);
            map.put("value", Constants.ORDER_STATUS_STR_DELETED);
        }

        return map;
    }
    /**
     * 获取支付文字
     * @param payType String 支付方式
     */
    private String getPayType(String payType) {
        switch (payType) {
            case Constants.PAY_TYPE_WE_CHAT:
                return Constants.PAY_TYPE_STR_WE_CHAT;
            case Constants.PAY_TYPE_YUE:
                return Constants.PAY_TYPE_STR_YUE;
            case Constants.PAY_TYPE_ALI_PAY:
                return Constants.PAY_TYPE_STR_ALI_PAY;
            default:
                return Constants.PAY_TYPE_STR_OTHER;
        }
    }

}

