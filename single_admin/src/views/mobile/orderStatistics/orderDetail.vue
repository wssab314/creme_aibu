<template>
  <div class="order-details pos-order-details">
    <div class="header acea-row row-middle">
      <div class="state">{{ title }}</div>
      <div class="data">
        <div class="order-num">订单：{{ orderInfo.orderId }}</div>
        <div>
          <span class="time">{{ orderInfo.createTime }}</span>
        </div>
      </div>
    </div>
    <div class="remarks acea-row row-between-wrapper" v-if="$route.params.goname != 'looks'">
      <span class="iconfont iconios-flag"></span>
      <input
        type="button"
        class="line1"
        style="text-align: left"
        :value="orderInfo.remark ? orderInfo.remark : '订单未备注，点击添加备注信息'"
        @click="modify(1)"
      />
    </div>
    <div class="orderingUser acea-row row-middle">
      <span class="iconfont iconmd-contact"></span>{{ orderInfo.realName }}
    </div>
    <div class="address">
      <div class="name">
        {{ orderInfo.realName }}<span class="phone">{{ orderInfo.userPhone }}</span>
      </div>
      <div>{{ orderInfo.userAddress }}</div>
    </div>
    <div class="line"><img src="../../../assets/imgs/line.jpg" /></div>
    <div class="pos-order-goods">
      <div class="goods acea-row row-between-wrapper" v-for="(item, index) in orderInfo.orderInfo" :key="index">
        <div class="picTxt acea-row row-between-wrapper">
          <div class="pictrue">
            <img :src="item.info.image" />
          </div>
          <div class="text">
            <div class="info line2">
              {{ item.info.productName }}
            </div>
            <div class="attr overflow">{{ item.info.sku }}</div>
          </div>
        </div>
        <div class="money">
          <div class="x-money">￥{{ item.info.price }}</div>
          <div class="num">x{{ item.info.payNum }}</div>
          <!--<div class="y-money">￥{{ item.info.price }}</div>-->
        </div>
      </div>
    </div>
    <div class="public-total">
      共{{ orderInfo.totalNum }}件商品，应支付 <span class="money">￥{{ orderInfo.payPrice }}</span> ( 邮费 ¥{{
        orderInfo.payPostage
      }})
    </div>
    <div class="wrapper">
      <div class="item acea-row row-between">
        <div>订单编号：</div>
        <div class="conter acea-row row-middle row-right">
          {{ orderInfo.orderId }}
          <span class="copy copy-data" :data-clipboard-text="orderInfo.orderId">复制</span>
        </div>
      </div>
      <div class="item acea-row row-between">
        <div>下单时间：</div>
        <div class="conter">{{ orderInfo.createTime }}</div>
      </div>
      <div class="item acea-row row-between">
        <div>支付状态：</div>
        <div class="conter">
          {{ orderInfo.statusStr.value }}
        </div>
      </div>
      <div class="item acea-row row-between">
        <div>支付方式：</div>
        <div class="conter">{{ orderInfo.payTypeStr }}</div>
      </div>
      <div class="item acea-row row-between">
        <div>买家留言：</div>
        <div class="conter">{{ orderInfo.mark }}</div>
      </div>
    </div>
    <div class="wrapper">
      <div class="item acea-row row-between">
        <div>支付金额：</div>
        <div class="conter">￥{{ orderInfo.totalPrice }}</div>
      </div>
      <div class="item acea-row row-between">
        <div>优惠券抵扣：</div>
        <div class="conter">-￥{{ orderInfo.couponPrice }}</div>
      </div>
      <div class="item acea-row row-between">
        <div>运费：</div>
        <div class="conter">￥{{ orderInfo.payPostage }}</div>
      </div>
      <div class="actualPay acea-row row-right">
        实付款：<span class="money font-color-red">￥{{ orderInfo.payPrice }}</span>
      </div>
    </div>
    <div class="wrapper" v-if="orderInfo.deliveryType === 'express'">
      <div class="item acea-row row-between">
        <div>配送方式：</div>
        <div class="conter" v-if="orderInfo.deliveryType === 'express'">快递</div>
        <div class="conter" v-if="orderInfo.deliveryType === 'send'">送货</div>
      </div>
      <div class="item acea-row row-between">
        <div v-if="orderInfo.deliveryType === 'express'">快递公司：</div>
        <div v-if="orderInfo.deliveryType === 'send'">送货人：</div>
        <div class="conter">{{ orderInfo.deliveryName }}</div>
      </div>
      <div class="item acea-row row-between">
        <div v-if="orderInfo.deliveryType === 'express'">快递单号：</div>
        <div v-if="orderInfo.deliveryType === 'send'">送货人电话：</div>
        <div class="conter">
          {{ orderInfo.deliveryId }}<span class="copy copy-data" :data-clipboard-text="orderInfo.deliveryId">复制</span>
        </div>
      </div>
    </div>
    <div style="height: 1.2rem"></div>
    <div class="footer acea-row row-right row-middle" v-if="$route.params.goname != 'looks'">
      <div class="more"></div>
      <div class="bnt cancel" @click="modify(0)" v-if="types === 'unPaid'">一键改价</div>
      <div class="bnt cancel" @click="modify(0)" v-if="types === 'refunding'">立即退款</div>
      <div class="bnt cancel" @click="modify(1)">订单备注</div>
      <!--<div-->
      <!--class="bnt cancel"-->
      <!--v-if="orderInfo.pay_type === 'offline' && orderInfo.paid === 0"-->
      <!--@click="offlinePay"-->
      <!--&gt;-->
      <!--确认付款-->
      <!--</div>-->
      <router-link
        class="bnt delivery"
        v-if="types == 'notShipped' && orderInfo.shippingType !== 2 && orderInfo.refundStatus !== 2"
        :to="'/javaMobile/orderDelivery/' + orderInfo.orderId + '/' + orderInfo.id"
        >去发货</router-link
      >
      <router-link
        class="bnt delivery"
        v-if="
          types === 'toBeWrittenOff' &&
          orderInfo.shippingType === 2 &&
          isWriteOff &&
          orderInfo.refundStatus === 0 &&
          orderInfo.paid == true
        "
        :to="'/operation/systemStore/orderCancellation'"
        >去核销
      </router-link>
    </div>
    <PriceChange
      :change="change"
      :orderInfo="orderInfo"
      v-on:closechange="changeclose($event)"
      :status="status"
    ></PriceChange>
  </div>
</template>
<script>
import PriceChange from '../components/PriceChange';
import ClipboardJS from 'clipboard';
import { orderDetailApi } from '@/api/order';
import { required, num } from '@/utils/validate';
import { validatorDefaultCatch } from '@/libs/dialog';
import { isWriteOff } from '@/utils';
export default {
  name: 'AdminOrder',
  components: {
    PriceChange,
  },
  props: {},
  data: function () {
    return {
      isWriteOff: isWriteOff(),
      order: false,
      change: false,
      orderId: '',
      orderInfo: {},
      status: 0,
      title: '',
      payType: '',
      types: '',
    };
  },
  watch: {
    '$route.params.id': function (newVal) {
      let that = this;
      if (newVal != undefined) {
        that.orderId = newVal;
        that.getIndex();
      }
    },
  },
  mounted: function () {
    // this.orderId = this.$route.params.id;
    this.getIndex();
    this.$nextTick(function () {
      var copybtn = document.getElementsByClassName('copy-data');
      const clipboard = new ClipboardJS(copybtn);
      clipboard.on('success', () => {
        this.$dialog.success('复制成功');
      });
    });
  },
  methods: {
    more: function () {
      this.order = !this.order;
    },
    modify: function (status) {
      this.change = true;
      this.status = status;
    },
    changeclose: function (msg) {
      this.change = msg;
      this.getIndex();
    },
    getIndex: function () {
      let that = this;
      orderDetailApi({ orderNo: this.$route.params.id }).then(
        (res) => {
          that.orderInfo = res;
          that.types = res.statusStr.key;
          that.title = res.statusStr.value;
          that.payType = res.payTypeStr;
          this.$nextTick(function () {
            let copybtn = document.getElementsByClassName('copy-data');
            const clipboard = new ClipboardJS(copybtn);
            clipboard.on('success', () => {
              this.$dialog.success('复制成功');
            });
          });
        },
        (err) => {
          that.$dialog.error(err.msg);
        },
      );
    },
    // async savePrice(opt) {
    //   let that = this,
    //     data = {},
    //     price = opt.price,
    //     remark = opt.remark,
    //     refundStatus = that.orderInfo.refundStatus,
    //     refundPrice = opt.refundPrice;
    //   if (that.status == 0 && refundStatus === 0) {
    //     try {
    //       await this.$validator({
    //         price: [
    //           required(required.message("金额")),
    //           num(num.message("金额"))
    //         ]
    //       }).validate({ price });
    //     } catch (e) {
    //       return validatorDefaultCatch(e);
    //     }
    //     data.price = price;
    //     data.orderId  = opt.orderId;
    //     setAdminOrderPrice(data).then(
    //       function() {
    //         that.change = false;
    //         that.$dialog.success("改价成功");
    //         that.getIndex();
    //       },
    //       function() {
    //         that.change = false;
    //         that.$dialog.error("改价失败");
    //       }
    //     );
    //   } else if (that.status == 0 && that.orderInfo.refund_status === 1) {
    //     try {
    //       await this.$validator({
    //         refund_price: [
    //           required(required.message("金额")),
    //           num(num.message("金额"))
    //         ]
    //       }).validate({ refund_price });
    //     } catch (e) {
    //       return validatorDefaultCatch(e);
    //     }
    //     data.price = refund_price;
    //     data.type = opt.type;
    //     setOrderRefund(data).then(
    //       res => {
    //         that.change = false;
    //         that.$dialog.success(res.msg);
    //         that.getIndex();
    //       },
    //       err => {
    //         that.change = false;
    //         that.$dialog.error(err.msg);
    //         that.getIndex();
    //       }
    //     );
    //   } else {
    //     try {
    //       await this.$validator({
    //         remark: [required(required.message("备注"))]
    //       }).validate({ remark });
    //     } catch (e) {
    //       return validatorDefaultCatch(e);
    //     }
    //     data.remark = remark;
    //     setAdminOrderRemark(data).then(
    //       res => {
    //         that.change = false;
    //         that.$dialog.success(res.msg);
    //         that.getIndex();
    //       },
    //       err => {
    //         that.change = false;
    //         that.$dialog.error(err.msg);
    //       }
    //     );
    //   }
    // },
    offlinePay: function () {
      setOfflinePay({ orderId: this.orderInfo.orderId }).then(
        (res) => {
          this.$dialog.success(res.msg);
          this.getIndex();
        },
        (err) => {
          this.$dialog.error(err.msg);
        },
      );
    },
  },
};
</script>
<style scoped lang="scss">
.pos-order-goods {
  padding: 0 0.3rem;
  background-color: #fff;
}
.pos-order-goods .goods {
  height: 1.85rem;
}
.pos-order-goods .goods ~ .goods {
  border-top: 1px dashed #e5e5e5;
}
.pos-order-goods .goods .picTxt {
  width: 5.15rem;
}
.pos-order-goods .goods .picTxt .pictrue {
  width: 1.3rem;
  height: 1.3rem;
}
.pos-order-goods .goods .picTxt .pictrue img {
  width: 100%;
  height: 100%;
  border-radius: 0.06rem;
}
.pos-order-goods .goods .picTxt .text {
  width: 3.65rem;
  height: 1.3rem;
}
.pos-order-goods .goods .picTxt .text .info {
  font-size: 0.28rem;
  color: #282828;
}
.pos-order-goods .goods .picTxt .text .attr {
  font-size: 0.2rem;
  color: #999;
  height: 0.8rem;
  line-height: 0.8rem;
}
.overflow {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 5rem;
}
.pos-order-goods .goods .money {
  width: 1.64rem;
  text-align: right;
  font-size: 0.28rem;
}
.pos-order-goods .goods .money .x-money {
  color: #282828;
}
.pos-order-goods .goods .money .num {
  color: #ff9600;
  margin: 0.05rem 0;
}
.pos-order-goods .goods .money .y-money {
  color: #999;
  text-decoration: line-through;
}
.order-details .header {
  padding: 0 0.3rem;
  height: 1.5rem;
  margin-top: -50px;
}
.order-details .header.on {
  background-color: #666 !important;
}
.order-details .header .pictrue {
  width: 1.1rem;
  height: 1.1rem;
}
.order-details .header .pictrue img {
  width: 100%;
  height: 100%;
}
.order-details .header .data {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.24rem;
  margin-left: 0.27rem;
}
.order-details .header.on .data {
  margin-left: 0;
}
.order-details .header .data .state {
  font-size: 0.3rem;
  font-weight: bold;
  color: #fff;
  margin-bottom: 0.07rem;
}
.order-details .nav {
  background-color: #fff;
  font-size: 0.26rem;
  color: #282828;
  padding: 0.25rem 0;
}
.order-details .nav .navCon {
  padding: 0 0.4rem;
}
.order-details .nav .navCon .on {
  font-weight: bold;
  color: #e93323;
}
.order-details .nav .progress {
  padding: 0 0.65rem;
  margin-top: 0.1rem;
}
.order-details .nav .progress .line {
  width: 1rem;
  height: 0.02rem;
  background-color: #939390;
}
.order-details .nav .progress .iconfont {
  font-size: 0.25rem;
  color: #939390;
  margin-top: -0.02rem;
  width: 0.3rem;
  height: 0.3rem;
  line-height: 0.33rem;
  text-align: center;
  margin-right: 0 !important;
}
.order-details .address {
  font-size: 0.26rem;
  color: #868686;
  background-color: #fff;
  padding: 0.25rem 0.3rem 0.3rem 0.3rem;
}
.order-details .address .name {
  font-size: 0.3rem;
  color: #282828;
  margin-bottom: 0.1rem;
}
.order-details .address .name .phone {
  margin-left: 0.4rem;
}
.order-details .line {
  width: 100%;
  height: 0.03rem;
}
.order-details .line img {
  width: 100%;
  height: 100%;
  display: block;
}
.order-details .wrapper {
  background-color: #fff;
  margin-top: 0.12rem;
  padding: 0.3rem;
}
.order-details .wrapper .item {
  font-size: 0.28rem;
  color: #282828;
}
.order-details .wrapper .item ~ .item {
  margin-top: 0.2rem;
}
.order-details .wrapper .item .conter {
  color: #868686;
  width: 5rem;
  text-align: right;
}
.order-details .wrapper .item .conter .copy {
  font-size: 0.2rem;
  color: #333;
  border-radius: 0.03rem;
  border: 1px solid #666;
  padding: 0.03rem 0.15rem;
  margin-left: 0.24rem;
}
.order-details .wrapper .actualPay {
  border-top: 0.01rem solid #eee;
  margin-top: 0.3rem;
  padding-top: 0.3rem;
}
.order-details .wrapper .actualPay .money {
  font-weight: bold;
  font-size: 0.3rem;
}
.order-details .footer {
  width: 100%;
  height: 1rem;
  position: fixed;
  bottom: 0;
  left: 0;
  background-color: #fff;
  padding: 0 0.3rem;
  border-top: 1px solid #eee;
}
.order-details .footer .bnt {
  width: auto;
  height: 0.6rem;
  text-align: center;
  line-height: 0.6rem;
  border-radius: 0.5rem;
  color: #fff;
  font-size: 0.27rem;
  padding: 0 3%;
}
.order-details .footer .bnt.cancel {
  color: #aaa;
  border: 1px solid #ddd;
}
.order-details .footer .bnt.default {
  color: #444;
  border: 1px solid #444;
}
.order-details .footer .bnt ~ .bnt {
  margin-left: 0.18rem;
}
.pos-order-details .header {
  background: linear-gradient(to right, #2291f8 0%, #1cd1dc 100%);
  background: -webkit-linear-gradient(to right, #2291f8 0%, #1cd1dc 100%);
  background: -moz-linear-gradient(to right, #2291f8 0%, #1cd1dc 100%);
}
.pos-order-details .header .state {
  font-size: 0.36rem;
  color: #fff;
}
.pos-order-details .header .data {
  margin-left: 0.35rem;
  font-size: 0.28rem;
}
.pos-order-details .header .data .order-num {
  font-size: 0.3rem;
  margin-bottom: 0.08rem;
}
.pos-order-details .remarks {
  width: 100%;
  height: 0.86rem;
  background-color: #fff;
  padding: 0 0.3rem;
}
.pos-order-details .remarks .iconfont {
  font-size: 0.4rem;
  color: #2a7efb;
}
.pos-order-details .remarks input {
  width: 6.3rem;
  height: 100%;
  font-size: 0.3rem;
}
.pos-order-details .remarks input::placeholder {
  color: #666;
}
.pos-order-details .orderingUser {
  font-size: 0.26rem;
  color: #282828;
  padding: 0 0.3rem;
  height: 0.67rem;
  background-color: #fff;
  margin-top: 0.16rem;
  border-bottom: 1px solid #f5f5f5;
}
.pos-order-details .orderingUser .iconfont {
  font-size: 0.4rem;
  color: #2a7efb;
  margin-right: 0.15rem;
}
.pos-order-details .address {
  margin-top: 0;
}
.pos-order-details .pos-order-goods {
  margin-top: 0.17rem;
}
.pos-order-details .footer .more {
  font-size: 0.27rem;
  color: #aaa;
  width: 1rem;
  height: 0.64rem;
  text-align: center;
  line-height: 0.64rem;
  margin-right: 0.25rem;
  position: relative;
}
.pos-order-details .footer .delivery {
  background: linear-gradient(to right, #2291f8 0%, #1cd1dc 100%);
  background: -webkit-linear-gradient(to right, #2291f8 0%, #1cd1dc 100%);
  background: -moz-linear-gradient(to right, #2291f8 0%, #1cd1dc 100%);
}
.pos-order-details .footer .more .order .arrow {
  width: 0;
  height: 0;
  border-left: 0.11rem solid transparent;
  border-right: 0.11rem solid transparent;
  border-top: 0.2rem solid #e5e5e5;
  position: absolute;
  left: 0.15rem;
  bottom: -0.18rem;
}
.pos-order-details .footer .more .order .arrow:before {
  content: '';
  width: 0;
  height: 0;
  border-left: 0.09rem solid transparent;
  border-right: 0.09rem solid transparent;
  border-top: 0.19rem solid #fff;
  position: absolute;
  left: -0.1rem;
  bottom: 0;
}
.pos-order-details .footer .more .order {
  width: 2rem;
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 0.1rem;
  position: absolute;
  top: -2rem;
  z-index: 9;
}
.pos-order-details .footer .more .order .item {
  height: 0.77rem;
  line-height: 0.77rem;
}
.pos-order-details .footer .more .order .item ~ .item {
  border-top: 1px solid #f5f5f5;
}
.pos-order-details .footer .more .moreName {
  width: 100%;
  height: 100%;
}
.public-total {
  font-size: 0.28rem;
  color: #282828;
  border-top: 1px solid #eee;
  height: 0.92rem;
  line-height: 0.92rem;
  text-align: right;
  padding: 0 0.3rem;
  background-color: #fff;
}
.public-total .money {
  color: #ff4c3c;
}
</style>
