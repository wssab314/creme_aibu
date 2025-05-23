package com.zbkj.common.response;

import com.zbkj.common.vo.StoreOrderInfoOldVo;
import com.zbkj.common.model.system.SystemStore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单详情响应对象
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
@ApiModel(value="StoreOrderInfoResponse对象", description="订单详情响应对象")
public class StoreOrderInfoResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单ID")
    private Integer id;

    @ApiModelProperty(value = "订单号")
    private String orderId;

    @ApiModelProperty(value = "用户id")
    private Integer uid;

    @ApiModelProperty(value = "用户姓名")
    private String realName;

    @ApiModelProperty(value = "用户电话")
    private String userPhone;

    @ApiModelProperty(value = "详细地址")
    private String userAddress;

    @ApiModelProperty(value = "订单商品总数")
    private Integer totalNum;

    @ApiModelProperty(value = "订单总价")
    private BigDecimal totalPrice;

    @ApiModelProperty(value = "实际支付金额")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "支付邮费")
    private BigDecimal payPostage;

    @ApiModelProperty(value = "优惠券金额")
    private BigDecimal couponPrice;

    @ApiModelProperty(value = "抵扣金额")
    private BigDecimal deductionPrice;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "订单状态（-1 : 申请退款 -2 : 退货成功 0：待发货；1：待收货；2：已收货；3：待评价；-1：已退款）")
    private Integer status;

    @ApiModelProperty(value = "0 未退款 1 申请中 2 已退款")
    private Integer refundStatus;

    @ApiModelProperty(value = "发货记录类型，1正常、2电子面单,3商家发货 发货类型必传 =3 时根据描述填写必填字段，注意为了方便本身不做必填校验")
    private String expressRecordType;

    @ApiModelProperty(value = "快递名称/送货人姓名")
    private String deliveryName;

    @ApiModelProperty(value = "发货类型 express 发货，send 送货，fictitious虚拟")
    private String deliveryType;

    @ApiModelProperty(value = "快递单号/手机号")
    private String deliveryId;

    @ApiModelProperty(value = "快递公司简称")
    private String deliveryCode;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "管理员备注")
    private String remark;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundPrice;

    @ApiModelProperty(value = "使用积分")
    private Integer useIntegral;

    @ApiModelProperty(value = "给用户退了多少积分")
    private Integer backIntegral;

    @ApiModelProperty(value = "核销码")
    private String verifyCode;

    @ApiModelProperty(value = "配送方式 1=快递 ，2=门店自提")
    private Integer shippingType;

    @ApiModelProperty(value = "订单状态")
    private Map<String, String> statusStr;

    @ApiModelProperty(value = "支付方式")
    private String payTypeStr;

    @ApiModelProperty(value = "用户昵称")
    private String nikeName;

    @ApiModelProperty(value = "用户电话")
    private String phone;

    @ApiModelProperty(value = "订单商品详情")
    List<StoreOrderInfoOldVo> orderInfo;

    @ApiModelProperty(value = "提货点")
    private SystemStore systemStore;

    @ApiModelProperty(value = "推广人名称")
    private String spreadName;

    @ApiModelProperty(value = "商品总价")
    private BigDecimal proTotalPrice;

    @ApiModelProperty(value = "退款时间")
    private Date refundReasonTime;

    @ApiModelProperty(value = "退款图片")
    private String refundReasonWapImg;

    @ApiModelProperty(value = "商家寄件单号图片")
    private String shipmentPic;

    @ApiModelProperty(value = "商家寄件订单任务id")
    private String shipmentTaskId;

    @ApiModelProperty(value = "商家寄件订单单号")
    private String shipmentOrderId;

    @ApiModelProperty(value = "商家寄件快递单号")
    private String shipmentNum;
}
