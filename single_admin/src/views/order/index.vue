<template>
  <div class="divBox relative">
    <el-card class="box-card">
      <div class="clearfix">
        <div class="container">
          <el-form size="small" label-width="100px">
            <el-form-item label="订单类型：">
              <el-radio-group v-model="tableFrom.type" type="button" class="mr20" size="small" @change="seachList">
                <el-radio-button v-for="(item, i) in options" :key="i" :label="item.value">{{
                  item.label
                }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="订单状态：" v-if="checkPermi(['admin:order:status:num'])">
              <el-radio-group v-model="tableFrom.status" type="button" @change="seachList">
                <el-radio-button label="all"
                  >全部 {{ '(' + orderChartType.all ? orderChartType.all : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="unPaid"
                  >未支付 {{ '(' + orderChartType.unPaid ? orderChartType.unPaid : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="notShipped"
                  >未发货 {{ '(' + orderChartType.notShipped ? orderChartType.notShipped : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="spike"
                  >待收货 {{ '(' + orderChartType.spike ? orderChartType.spike : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="bargain"
                  >待评价 {{ '(' + orderChartType.bargain ? orderChartType.bargain : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="complete"
                  >交易完成 {{ '(' + orderChartType.complete ? orderChartType.complete : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="toBeWrittenOff"
                  >待核销
                  {{ '(' + orderChartType.toBeWrittenOff ? orderChartType.toBeWrittenOff : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="refunding"
                  >退款中 {{ '(' + orderChartType.refunding ? orderChartType.refunding : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="refunded"
                  >已退款 {{ '(' + orderChartType.refunded ? orderChartType.refunded : 0 + ')' }}</el-radio-button
                >
                <el-radio-button label="deleted"
                  >已删除 {{ '(' + orderChartType.deleted ? orderChartType.deleted : 0 + ')' }}</el-radio-button
                >
              </el-radio-group>
            </el-form-item>
            <el-form-item label="时间选择：" class="width100">
              <el-radio-group
                v-model="tableFrom.dateLimit"
                type="button"
                class="mr20"
                size="small"
                @change="selectChange(tableFrom.dateLimit)"
              >
                <el-radio-button v-for="(item, i) in fromList.fromTxt" :key="i" :label="item.val">{{
                  item.text
                }}</el-radio-button>
              </el-radio-group>
              <el-date-picker
                v-model="timeVal"
                value-format="yyyy-MM-dd"
                format="yyyy-MM-dd"
                size="small"
                type="daterange"
                placement="bottom-end"
                placeholder="自定义时间"
                style="width: 220px"
                @change="onchangeTime"
              />
            </el-form-item>
            <el-form-item label="订单号：" class="width100">
              <el-input v-model="tableFrom.orderNo" placeholder="请输入订单号" class="selWidth" size="small" clearable>
                <el-button slot="append" icon="el-icon-search" size="small" @click="seachList" />
              </el-input>
            </el-form-item>
            <el-form-item class="width100">
              <el-button @click="exports" v-hasPermi="['admin:export:excel:order']">导出</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
    <el-card class="box-card">
      <el-table
        v-loading="listLoading"
        :data="tableData.data"
        size="mini"
        class="table"
        highlight-current-row
        :header-cell-style="{ fontWeight: 'bold' }"
        :row-key="
          (row) => {
            return row.orderId;
          }
        "
      >
        <!-- @selection-change="handleSelectionChange" -->
        <!-- <el-table-column
          type="selection"
          :reserve-selection="true"
          width="55"
        /> -->
        <el-table-column label="订单号" min-width="210" v-if="checkedCities.includes('订单号')">
          <template slot-scope="scope">
            <span style="display: block" v-text="scope.row.orderId" />
            <span v-show="scope.row.isDel" style="color: #ed4014; display: block">用户已删除</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderType" label="订单类型" min-width="110" v-if="checkedCities.includes('订单类型')" />
        <el-table-column prop="realName" label="收货人" min-width="100" v-if="checkedCities.includes('收货人')" />
        <el-table-column label="商品信息" min-width="400" v-if="checkedCities.includes('商品信息')">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="right" :open-delay="800">
              <div v-if="scope.row.productList && scope.row.productList.length" slot="reference">
                <div
                  v-for="(val, i) in scope.row.productList"
                  :key="i"
                  class="tabBox acea-row row-middle"
                  style="flex-wrap: inherit"
                >
                  <div class="demo-image__preview mr10">
                    <el-image :src="val.info.image" :preview-src-list="[val.info.image]" />
                  </div>
                  <div class="text_overflow">
                    <span class="tabBox_tit mr10"
                      >{{ val.info.productName + ' | ' }}{{ val.info.sku ? val.info.sku : '-' }}</span
                    >
                    <span class="tabBox_pice">{{
                      '￥' + val.info.price ? val.info.price + ' x ' + val.info.payNum : '-'
                    }}</span>
                  </div>
                </div>
              </div>
              <div class="pup_card" v-if="scope.row.productList && scope.row.productList.length">
                <div
                  v-for="(val, i) in scope.row.productList"
                  :key="i"
                  class="tabBox acea-row row-middle"
                  style="flex-wrap: inherit"
                >
                  <div class="">
                    <span class="tabBox_tit mr10"
                      >{{ val.info.productName + ' | ' }}{{ val.info.sku ? val.info.sku : '-' }}</span
                    >
                    <span class="tabBox_pice">{{
                      '￥' + val.info.price ? val.info.price + ' x ' + val.info.payNum : '-'
                    }}</span>
                  </div>
                </div>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="payPrice" label="实际支付" min-width="80" v-if="checkedCities.includes('实际支付')" />
        <el-table-column label="支付方式" min-width="80" v-if="checkedCities.includes('支付方式')">
          <template slot-scope="scope">
            <span>{{ scope.row.payTypeStr }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" min-width="100" v-if="checkedCities.includes('订单状态')">
          <template slot-scope="scope">
            <div>
              <div v-if="scope.row.refundStatus === 1 || scope.row.refundStatus === 2" class="refunding">
                <template>
                  <el-popover trigger="hover" placement="left" :open-delay="500">
                    <b style="color: #f124c7; cursor: pointer" slot="reference">{{ scope.row.statusStr.value }}</b>
                    <div class="pup_card flex-column">
                      <span>退款原因：{{ scope.row.refundReasonWap }}</span>
                      <span>备注说明：{{ scope.row.refundReasonWapExplain }}</span>
                      <span>退款时间：{{ scope.row.refundReasonTime }}</span>
                      <span class="acea-row">
                        退款凭证：
                        <template v-if="scope.row.refundReasonWapImg">
                          <div
                            v-for="(item, index) in scope.row.refundReasonWapImg.split(',')"
                            :key="index"
                            class="demo-image__preview"
                            style="width: 35px; height: auto; display: inline-block"
                          >
                            <el-image :src="item" :preview-src-list="[item]" />
                          </div>
                        </template>
                        <span v-else style="display: inline-block">无</span>
                      </span>
                    </div>
                  </el-popover>
                </template>
              </div>
              <span v-else>{{ scope.row.statusStr.value }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="150" v-if="checkedCities.includes('下单时间')" />
        <el-table-column label="操作" min-width="150" fixed="right" align="center" :render-header="renderHeader">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.paid === false && !scope.row.isAlterPrice && checkPermi(['admin:order:update:price'])"
              type="text"
              size="small"
              @click="edit(scope.row)"
              class="mr10"
              >编辑</el-button
            >
            <el-button
              v-if="
                scope.row.statusStr.key === 'notShipped' &&
                scope.row.refundStatus === 0 &&
                checkPermi(['admin:order:send'])
              "
              type="text"
              size="small"
              class="mr10"
              @click="sendOrder(scope.row)"
              >发送货</el-button
            >
            <!--视频号订单不可修改-->
            <el-button
              v-if="
                scope.row.statusStr.key === 'spike' &&
                scope.row.type === 0 &&
                checkPermi(['admin:order:tracking:number:update']) &&
                !scope.row.shipmentTaskId
              "
              type="text"
              size="small"
              class="mr10"
              @click="handleUpdateNumber(scope.row)"
              >修改快递单号</el-button
            >
            <el-button
              v-if="
                scope.row.statusStr.key === 'toBeWrittenOff' &&
                scope.row.paid == true &&
                scope.row.refundStatus === 0 &&
                checkPermi(['admin:order:write:update'])
              "
              type="text"
              size="small"
              class="mr10"
              @click="onWriteOff(scope.row)"
              >立即核销</el-button
            >
            <el-dropdown trigger="click">
              <span class="el-dropdown-link"> 更多<i class="el-icon-arrow-down el-icon--right" /> </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  @click.native="onOrderDetails(scope.row.orderId)"
                  v-if="checkPermi(['admin:order:info'])"
                  >订单详情</el-dropdown-item
                >
                <el-dropdown-item
                  @click.native="onOrderLog(scope.row.orderId)"
                  v-if="checkPermi(['admin:order:status:list'])"
                  >订单记录</el-dropdown-item
                >
                <el-dropdown-item @click.native="onOrderMark(scope.row)" v-if="checkPermi(['admin:order:mark'])"
                  >订单备注</el-dropdown-item
                >
                <el-dropdown-item
                  v-if="scope.row.refundStatus === 1 && checkPermi(['admin:order:refund:refuse'])"
                  @click.native="onOrderRefuse(scope.row)"
                  >拒绝退款</el-dropdown-item
                >
                <!--v-show="((scope.row.statusStr.key !== 'refunded' && scope.row.statusStr.key !== 'unPaid') && (parseFloat(scope.row.payPrice) >= parseFloat(scope.row.refundPrice))) || (scope.row.payPrice == 0 && [0,1].indexOf(scope.row.refundStatus) !== -1)"-->
                <el-dropdown-item
                  v-if="scope.row.refundStatus === 1 && checkPermi(['admin:order:refund'])"
                  @click.native="onOrderRefund(scope.row)"
                  >立即退款</el-dropdown-item
                >
                <el-dropdown-item
                  v-if="scope.row.statusStr.key === 'deleted' && checkPermi(['admin:order:delete'])"
                  @click.native="handleDelete(scope.row, scope.$index)"
                  >删除订单</el-dropdown-item
                >
                <el-dropdown-item v-if="scope.row.statusStr.key !== 'unPaid'" @click.native="onOrderPrint(scope.row)"
                  >打印小票</el-dropdown-item
                >
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
        <el-pagination
          :page-sizes="[20, 40, 60, 80]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
        />
      </div>
    </el-card>
    <div class="card_abs" v-show="card_select_show">
      <template>
        <div class="cell_ht">
          <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange"
            >全选</el-checkbox
          >
          <el-button type="text" @click="checkSave()">保存</el-button>
        </div>
        <el-checkbox-group v-model="checkedCities" @change="handleCheckedCitiesChange">
          <el-checkbox v-for="item in columnData" :label="item" :key="item" class="check_cell">{{ item }}</el-checkbox>
        </el-checkbox-group>
      </template>
    </div>
    <!--编辑-->
    <el-dialog title="编辑订单" :visible.sync="dialogVisible" width="500px" :before-close="handleClose">
      <zb-parser
        v-if="dialogVisible"
        :form-id="104"
        :is-create="isCreate"
        :edit-data="editData"
        @submit="handlerSubmit"
        @resetForm="resetForm"
      />
    </el-dialog>

    <!--记录-->
    <el-dialog title="操作记录" :visible.sync="dialogVisibleJI" width="700px">
      <el-table v-loading="LogLoading" border :data="tableDataLog.data" style="width: 100%">
        <el-table-column prop="oid" align="center" label="ID" min-width="80" />
        <el-table-column prop="changeMessage" label="操作记录" align="center" min-width="280" />
        <el-table-column prop="createTime" label="操作时间" align="center" min-width="280" />
      </el-table>
      <div class="block">
        <el-pagination
          :page-sizes="[10, 20, 30, 40]"
          :page-size="tableFromLog.limit"
          :current-page="tableFromLog.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableDataLog.total"
          @size-change="handleSizeChangeLog"
          @current-change="pageChangeLog"
        />
      </div>
    </el-dialog>

    <!--详情-->
    <details-from ref="orderDetail" :orderId="orderId" />

    <!-- 发送货 -->
    <order-send
      ref="send"
      :orderId="orderId"
      @submitFail="getList"
      :expressListNormal="expressListNormal"
      :expressListElec="expressListElec"
      :orderDetail="orderDetail"
    ></order-send>

    <!-- 发送货视频号商品 -->
    <order-video-send ref="videoSend" :orderId="orderId" @submitFail="getList"></order-video-send>

    <!--拒绝退款-->
    <el-dialog
      title="拒绝退款原因"
      v-if="RefuseVisible"
      :visible.sync="RefuseVisible"
      width="500px"
      :before-close="RefusehandleClose"
    >
      <zb-parser
        :form-id="106"
        :is-create="1"
        :edit-data="RefuseData"
        @submit="RefusehandlerSubmit"
        @resetForm="resetFormRefusehand"
      />
    </el-dialog>

    <!--立即退款-->
    <el-dialog title="退款处理" :visible.sync="refundVisible" width="500px" :before-close="refundhandleClose">
      <zb-parser
        :form-id="107"
        :is-create="1"
        :edit-data="refundData"
        @submit="refundhandlerSubmit"
        v-if="refundVisible"
        @resetForm="resetFormRefundhandler"
      />
    </el-dialog>
  </div>
</template>

<script>
import {
  orderListDataApi,
  orderStatusNumApi,
  writeUpdateApi,
  orderListApi,
  updatePriceApi,
  orderLogApi,
  orderMarkApi,
  orderDeleteApi,
  orderRefuseApi,
  orderRefundApi,
  orderPrint,
  orderDetailApi,
} from '@/api/order';
import zbParser from '@/components/FormGenerator/components/parser/ZBParser';
import detailsFrom from './orderDetail';
import orderSend from './orderSend';
import orderVideoSend from './orderVideoSend';
import { storeStaffListApi } from '@/api/storePoint';
import Cookies from 'js-cookie';
import { isWriteOff } from '@/utils';
import { orderExcelApi } from '@/api/store';
import { expressAllApi } from '@/api/sms';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'orderlistDetails',
  components: {
    zbParser,
    detailsFrom,
    orderSend,
    orderVideoSend,
  },
  data() {
    return {
      options: [
        {
          value: 2,
          label: '全部',
        },
        {
          value: 0,
          label: '普通订单',
        },
        {
          value: 1,
          label: '视频号订单',
        },
      ],
      RefuseVisible: false,
      RefuseData: {},
      orderId: '',
      refundVisible: false,
      refundData: {},
      dialogVisibleJI: false,
      tableDataLog: {
        data: [],
        total: 0,
      },
      tableFromLog: {
        page: 1,
        limit: 10,
        orderNo: 0,
      },
      LogLoading: false,
      isCreate: 1,
      editData: null,
      dialogVisible: false,
      tableData: {
        data: [],
        total: 0,
      },
      listLoading: true,
      //订单状态（all 总数； 未支付 unPaid； 未发货 notShipped；待收货 spike；待评价 bargain；已完成 complete；
      // 待核销 toBeWrittenOff；退款中:refunding；已退款:refunded；已删除:deleted
      tableFrom: {
        status: 'all',
        dateLimit: '',
        orderNo: '',
        page: 1,
        limit: 20,
        type: 2,
      },
      orderChartType: {},
      timeVal: [],
      fromList: this.$constants.fromList,
      fromType: [
        { value: 'all', text: '全部' },
        { value: 'info', text: '普通' },
        { value: 'pintuan', text: '拼团' },
        { value: 'bragin', text: '砍价' },
        { value: 'miaosha', text: '秒杀' },
      ],
      selectionList: [],
      ids: '',
      orderids: '',
      cardLists: [],
      isWriteOff: isWriteOff(),
      proType: 0,
      active: false,
      card_select_show: false,
      checkAll: false,
      checkedCities: ['订单号', '订单类型', '收货人', '商品信息', '实际支付', '支付方式', '订单状态', '下单时间'],
      columnData: ['订单号', '订单类型', '收货人', '商品信息', '实际支付', '支付方式', '订单状态', '下单时间'],
      isIndeterminate: true,
      expressListNormal: [], //全部物流公司 normal
      expressListElec: [], //全部物流公司 elec
      orderDetail: null, //订单详情
    };
  },
  mounted() {
    this.getList();
    this.getOrderStatusNum();
    // this.getOrderListData();
    if (checkPermi(['admin:express:list'])) this.getExpress();
  },
  methods: {
    checkPermi,
    // 物流公司列表
    async getExpress() {
      this.expressListNormal = await this.getExpressList('normal'); //全部物流公司
      this.expressListElec = await this.getExpressList('elec');
    },
    // 物流公司列表
    async getExpressList(expressType) {
      return new Promise((resolve, reject) => {
        expressAllApi({ type: expressType }).then((res) => {
          resolve(res);
        });
      });
    },
    resetFormRefundhandler() {
      this.refundVisible = false;
    },
    resetFormRefusehand() {
      this.RefuseVisible = false;
    },
    resetForm(formValue) {
      this.dialogVisible = false;
    },
    // 核销订单
    onWriteOff(row) {
      this.$modalSure('核销订单吗').then(() => {
        writeUpdateApi(row.verifyCode).then(() => {
          this.$message.success('核销成功');
          this.tableFrom.page = 1;
          this.getList();
        });
      });
    },
    seachList() {
      this.tableFrom.page = 1;
      this.getList();
      this.getOrderStatusNum();
    },
    // 拒绝退款
    RefusehandleClose() {
      this.RefuseVisible = false;
    },
    onOrderRefuse(row) {
      this.orderids = row.orderId;
      this.RefuseData = {
        orderId: row.orderId,
        reason: '',
      };
      this.RefuseVisible = true;
    },
    RefusehandlerSubmit(formValue) {
      orderRefuseApi({ orderNo: this.orderids, reason: formValue.reason }).then((data) => {
        this.$message.success('操作成功');
        this.RefuseVisible = false;
        this.getList();
      });
    },
    // 立即退款
    refundhandleClose() {
      this.refundVisible = false;
    },
    onOrderRefund(row) {
      this.refundData = {
        orderId: row.orderId,
        amount: row.payPrice,
        type: '',
      };
      this.orderids = row.orderId;
      this.refundVisible = true;
    },
    refundhandlerSubmit(formValue) {
      orderRefundApi({ amount: formValue.amount, orderNo: this.orderids }).then((data) => {
        this.$message.success('操作成功');
        this.refundVisible = false;
        this.getList();
      });
    },
    // 详情接口
    getDetail(id) {
      orderDetailApi({ orderNo: id })
        .then((res) => {
          this.orderDetail = res;
          this.editData = {
            orderId: id,
            totalPrice: this.orderDetail.proTotalPrice,
            totalPostage: this.orderDetail.payPostage,
            payPrice: this.orderDetail.payPrice,
            payPostage: this.orderDetail.payPostage,
          };
          this.loading = true;
        })
        .catch(() => {
          this.orderDetail = null;
          this.loading = false;
        });
    },
    //修改快递单号
    handleUpdateNumber(row) {
      this.orderId = row.orderId;
      this.$refs.send.modals = true;
      this.$refs.send.loading = true;
      //默认加载Normal物流公司
      this.$refs.send.express = this.expressListNormal;
      this.getDetail(row.orderId);
    },
    // 发送
    sendOrder(row) {
      this.orderDetail = null;
      if (row.type === 0) {
        this.$refs.send.modals = true;
        //默认加载Normal物流公司
        this.$refs.send.express = this.expressListNormal;
        this.$refs.send.sheetInfo();
      } else {
        this.$refs.videoSend.modals = true;
        if (!JSON.parse(sessionStorage.getItem('videoExpress'))) this.$refs.videoSend.companyGetList();
      }
      this.orderId = row.orderId;
    },
    // 订单删除
    handleDelete(row, idx) {
      if (row.isDel) {
        this.$modalSure().then(() => {
          orderDeleteApi({ orderNo: row.orderId }).then(() => {
            this.$message.success('删除成功');
            if (this.tableData.data.length === 1 && this.tableFrom.page > 1)
              this.tableFrom.page = this.tableFrom.page - 1;
            this.getList();
          });
        });
      } else {
        this.$confirm('您选择的的订单存在用户未删除的订单，无法删除用户未删除的订单！', '提示', {
          confirmButtonText: '确定',
          type: 'error',
        });
      }
    },
    // 详情
    onOrderDetails(id) {
      this.orderId = id;
      this.$refs.orderDetail.getDetail(id);
      this.$refs.orderDetail.dialogVisible = true;
    },
    // 订单记录
    onOrderLog(id) {
      this.tableFromLog.limit = 10;
      this.orderId = id;
      this.dialogVisibleJI = true;
      this.getOrderStatusList(id);
    },
    //订单记录请求列表
    getOrderStatusList(id) {
      this.LogLoading = true;
      this.tableFromLog.orderNo = id;
      orderLogApi(this.tableFromLog)
        .then((res) => {
          this.tableDataLog.data = res.list;
          this.tableDataLog.total = res.total;
          this.LogLoading = false;
        })
        .catch(() => {
          this.LogLoading = false;
        });
    },
    pageChangeLog(page) {
      this.tableFromLog.page = page;
      this.getOrderStatusList(this.orderId);
    },
    handleSizeChangeLog(val) {
      this.tableFromLog.limit = val;
      this.getOrderStatusList(this.orderId);
    },
    handleClose() {
      this.dialogVisible = false;
    },
    // 备注
    onOrderMark(row) {
      this.$prompt('订单备注', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputErrorMessage: '请输入订单备注',
        inputType: 'textarea',
        inputValue: row.remark,
        inputPlaceholder: '请输入订单备注',
        inputValidator: (value) => {
          if (!value) return '输入不能为空';
        },
      })
        .then(({ value }) => {
          orderMarkApi({ mark: value, orderNo: row.orderId }).then(() => {
            this.$message.success('操作成功');
            this.getList();
          });
        })
        .catch(() => {
          this.$message.info('取消输入');
        });
    },
    handleSelectionChange(val) {
      this.selectionList = val;
      const data = [];
      this.selectionList.map((item) => {
        data.push(item.orderId);
      });
      this.ids = data.join(',');
    },
    // 选择时间
    selectChange(tab) {
      this.timeVal = [];
      this.tableFrom.page = 1;
      this.getList();
      this.getOrderStatusNum();
      // this.getOrderListData();
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.tableFrom.dateLimit = e ? this.timeVal.join(',') : '';
      this.tableFrom.page = 1;
      this.getList();
      this.getOrderStatusNum();
      // this.getOrderListData();
    },
    // 编辑
    edit(row) {
      //this.getDetail(row.orderId);
      this.orderId = row.orderId;

      this.editData = {
        orderId: row.orderId,
        proTotalPrice: row.proTotalPrice,
        beforePayPrice: row.beforePayPrice ? row.beforePayPrice : row.payPrice,
        payPrice: row.payPrice,
      };
      this.dialogVisible = true;
    },
    handlerSubmit(formValue) {
      let data = {
        orderNo: formValue.orderId,
        payPrice: formValue.payPrice,
      };
      updatePriceApi(data).then((data) => {
        this.$message.success('编辑成功');
        this.dialogVisible = false;
        this.getList();
      });
    },
    // 列表
    getList() {
      this.listLoading = true;
      orderListApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list || [];
          this.tableData.total = res.total;
          this.listLoading = false;
          this.checkedCities = this.$cache.local.has('order_stroge')
            ? this.$cache.local.getJSON('order_stroge')
            : this.checkedCities;
        })
        .catch(() => {
          this.listLoading = false;
        });
    },
    // 数据统计
    getOrderListData() {
      orderListDataApi({ dateLimit: this.tableFrom.dateLimit }).then((res) => {
        this.cardLists = [
          { name: '订单数量', count: res.count, color: '#1890FF', class: 'one', icon: 'icondingdan' },
          { name: '订单金额', count: res.amount, color: '#A277FF', class: 'two', icon: 'icondingdanjine' },
          {
            name: '微信支付金额',
            count: res.weChatAmount,
            color: '#EF9C20',
            class: 'three',
            icon: 'iconweixinzhifujine',
          },
          { name: '余额支付金额', count: res.yueAmount, color: '#1BBE6B', class: 'four', icon: 'iconyuezhifujine2' },
        ];
      });
    },
    // 获取各状态数量
    getOrderStatusNum() {
      orderStatusNumApi({ dateLimit: this.tableFrom.dateLimit, type: this.tableFrom.type }).then((res) => {
        this.orderChartType = res;
      });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    handleSizeChange(val) {
      this.tableFrom.limit = val;
      this.getList();
    },
    exports() {
      let data = {
        dateLimit: this.tableFrom.dateLimit,
        orderNo: this.tableFrom.orderNo,
        status: this.tableFrom.status,
        type: this.tableFrom.type,
      };
      orderExcelApi(data).then((res) => {
        window.open(res.fileName);
      });
    },
    renderHeader(h) {
      return (
        <p>
          <span style="padding-right:5px;">操作</span>
          <i class="el-icon-setting" onClick={() => this.handleAddItem()}></i>
        </p>
      );
    },
    handleAddItem() {
      if (this.card_select_show) {
        this.$set(this, 'card_select_show', false);
      } else if (!this.card_select_show) {
        this.$set(this, 'card_select_show', true);
      }
    },
    handleCheckAllChange(val) {
      this.checkedCities = val ? this.columnData : [];
      this.isIndeterminate = false;
    },
    handleCheckedCitiesChange(value) {
      let checkedCount = value.length;
      this.checkAll = checkedCount === this.columnData.length;
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.columnData.length;
    },
    checkSave() {
      this.card_select_show = false;
      this.$modal.loading('正在保存到本地，请稍候...');
      this.$cache.local.setJSON('order_stroge', this.checkedCities);
      setTimeout(this.$modal.closeLoading(), 1000);
    },
    //打印小票
    onOrderPrint(data) {
      orderPrint(data.orderId)
        .then((res) => {
          this.$modal.msgSuccess('打印成功');
        })
        .catch((error) => {
          this.$modal.msgError(error.message);
        });
    },
  },
};
</script>
<style lang="scss" scoped>
.el-table__body {
  width: 100%;
  table-layout: fixed !important;
}

.demo-table-expand {
  ::v-deeplabel {
    width: 83px !important;
  }
}

.refunding {
  span {
    display: block;
  }
}

.selWidth {
  width: 300px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
  font-size: 12px;
}

.el-icon-arrow-down {
  font-size: 12px;
}

.tabBox_tit {
  font-size: 12px !important;
  /*margin: 0 2px 0 10px;*/
  letter-spacing: 1px;
  /*padding: 5px 0;*/
  box-sizing: border-box;
}

.text_overflow {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.pup_card {
  width: 200px;
  border-radius: 5px;
  padding: 5px;
  box-sizing: border-box;
  font-size: 12px;
  line-height: 16px;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.mt20 {
  margin-top: 20px;
}

.relative {
  position: relative;
}

.card_abs {
  position: absolute;
  padding-bottom: 15px;
  top: 520px;
  right: 40px;
  width: 200px;
  background: #fff;
  z-index: 99999;
  box-shadow: 0px 0px 14px 0px rgba(0, 0, 0, 0.1);
}

.cell_ht {
  height: 50px;
  padding: 15px 20px;
  box-sizing: border-box;
  border-bottom: 1px solid #eeeeee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.check_cell {
  width: 100%;
  padding: 15px 20px 0;
}

::v-deep .el-checkbox__input.is-checked + .el-checkbox__label {
  color: #606266;
}
</style>
