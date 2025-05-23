<template>
  <div class="divBox">
    <el-card class="box-card">
      <div class="clearfix">
        <div class="container">
          <el-form size="small" label-width="100px" inline>
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
                style="width: 250px"
                @change="onchangeTime"
              />
            </el-form-item>
            <el-form-item label="用户id：">
              <el-input v-model="tableFrom.uid" placeholder="用户id" class="selWidth" size="small" clearable>
                <el-button slot="append" icon="el-icon-search" size="small" @click="getList(1)" />
              </el-input>
            </el-form-item>
            <el-form-item label="订单号：">
              <el-input v-model="tableFrom.keywords" placeholder="订单号" class="selWidth" size="small" clearable>
                <el-button slot="append" icon="el-icon-search" size="small" @click="getList(1)" />
              </el-input>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
    <div class="mt20">
      <cards-data :card-lists="cardLists" v-if="checkPermi(['admin:recharge:balance'])" />
    </div>
    <el-card class="box-card">
      <el-table
        v-loading="listLoading"
        :data="tableData.data"
        style="width: 100%"
        size="mini"
        class="table"
        highlight-current-row
      >
        <el-table-column prop="uid" label="UID" width="60" />
        <el-table-column label="头像" min-width="80">
          <template slot-scope="scope">
            <div class="demo-image__preview">
              <el-image :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="用户昵称" min-width="130" />
        <el-table-column prop="orderId" label="订单号" min-width="180" />
        <el-table-column
          sortable
          label="支付金额"
          min-width="120"
          :sort-method="
            (a, b) => {
              return a.price - b.price;
            }
          "
          prop="price"
        />
        <el-table-column
          sortable
          label="赠送金额"
          min-width="120"
          prop="givePrice"
          :sort-method="
            (a, b) => {
              return a.givePrice - b.givePrice;
            }
          "
        />
        <el-table-column label="充值类型" min-width="80">
          <template slot-scope="scope">
            <span>{{ scope.row.rechargeType | rechargeTypeFilter }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" min-width="150">
          <template slot-scope="scope">
            <span class="spBlock">{{ scope.row.payTime || '无' }}</span>
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
    <!--退款-->
    <el-dialog title="退款" :visible.sync="dialogVisible" width="500px" :before-close="handleClose">
      <zb-parser
        v-if="dialogVisible"
        :form-id="130"
        :is-create="isCreate"
        :edit-data="editData"
        @submit="handlerSubmit"
        @resetForm="resetForm"
      />
    </el-dialog>
  </div>
</template>

<script>
import { topUpLogListApi, balanceApi, topUpLogDeleteApi, refundApi } from '@/api/financial';
import cardsData from '@/components/cards/index';
import zbParser from '@/components/FormGenerator/components/parser/ZBParser';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'AccountsBill',
  components: { cardsData, zbParser },
  data() {
    return {
      editData: {},
      isCreate: 1,
      cardLists: [],
      timeVal: [],
      tableData: {
        data: [],
        total: 0,
      },
      listLoading: true,
      tableFrom: {
        uid: '',
        // paid: '',
        dateLimit: '',
        keywords: '',
        page: 1,
        limit: 20,
      },
      fromList: this.$constants.fromList,
      dialogVisible: false,
    };
  },
  mounted() {
    this.getList();
    this.getStatistics();
  },
  methods: {
    checkPermi,
    resetForm(formValue) {
      this.handleClose();
    },
    handlerSubmit(formValue) {
      refundApi(formValue).then((data) => {
        this.$message.success('操作成功');
        this.dialogVisible = false;
        this.editData = {};
        this.getList();
      });
    },
    handleClose() {
      this.dialogVisible = false;
      this.editData = {};
    },
    handleRefund(row) {
      if (row.price == row.refundPrice) return this.$message.waiting('已退完支付金额！不能再退款了 ！');
      if (row.rechargeType === 'balance') return this.$message.waiting('佣金转入余额，不能退款 ！');
      this.editData.orderId = row.orderId;
      this.editData.id = row.id;
      this.dialogVisible = true;
    },
    handleDelete(row, idx) {
      this.$modalSure().then(() => {
        topUpLogDeleteApi({ id: row.id }).then(() => {
          this.$message.success('删除成功');
          this.getList(this.tableFrom.page);
        });
      });
    },
    // 选择时间
    selectChange(tab) {
      this.tableFrom.dateLimit = tab;
      this.timeVal = [];
      this.tableFrom.page = 1;
      this.getList();
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.tableFrom.dateLimit = e ? this.timeVal.join(',') : '';
      this.tableFrom.page = 1;
      this.getList();
    },
    // 列表
    getList(num) {
      this.listLoading = true;
      this.tableFrom.page = num ? num : this.tableFrom.page;
      topUpLogListApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list;
          this.tableData.total = res.total;
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
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
    // 统计
    getStatistics() {
      balanceApi().then((res) => {
        const stat = res;
        this.cardLists = [
          { name: '充值总金额', count: stat.total, color: '#1890FF', class: 'one', icon: 'iconchongzhijine' },
          { name: '小程序充值金额', count: stat.routine, color: '#A277FF', class: 'two', icon: 'iconweixinzhifujine' },
          { name: '公众号充值金额', count: stat.weChat, color: '#EF9C20', class: 'three', icon: 'iconyuezhifujine1' },
        ];
      });
    },
  },
};
</script>

<style scoped>
.selWidth {
  width: 300px;
}
</style>
