<template>
  <div class="divBox">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <div class="container">
          <el-form size="small" label-width="100px">
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
            <el-form-item label="关键字：">
              <el-input
                v-model="tableFrom.keywords"
                placeholder="请输入姓名、电话、UID"
                class="selWidth"
                size="small"
                clearable
              >
                <el-button slot="append" icon="el-icon-search" size="small" @click="seachList" />
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <!-- <cards-data :cardLists="cardLists"></cards-data> -->
      </div>
      <el-table
        v-loading="listLoading"
        :data="tableData.data"
        style="width: 100%"
        size="mini"
        class="table"
        highlight-current-row
      >
        <el-table-column prop="uid" label="ID" width="60" />
        <el-table-column label="头像" min-width="80">
          <template slot-scope="scope">
            <div class="demo-image__preview">
              <el-image :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="用户信息" min-width="130" />
        <el-table-column
          sortable
          prop="spreadCount"
          label="推广用户(一级)数量"
          :sort-method="
            (a, b) => {
              return a.spreadCount - b.spreadCount;
            }
          "
          min-width="150"
        />
        <el-table-column
          sortable
          label="推广订单数量"
          prop="spreadOrderNum"
          :sort-method="
            (a, b) => {
              return a.spreadOrderNum - b.spreadOrderNum;
            }
          "
          min-width="120"
        />
        <el-table-column
          sortable
          label="推广订单金额"
          min-width="120"
          :sort-method="
            (a, b) => {
              return a.spreadOrderTotalPrice - b.spreadOrderTotalPrice;
            }
          "
          prop="spreadOrderTotalPrice"
        />
        <el-table-column
          sortable
          label="佣金总金额"
          min-width="120"
          :sort-method="
            (a, b) => {
              return a.totalBrokeragePrice - b.totalBrokeragePrice;
            }
          "
          prop="totalBrokeragePrice"
        />
        <el-table-column
          sortable
          label="已提现金额"
          min-width="120"
          :sort-method="
            (a, b) => {
              return a.extractCountPrice - b.extractCountPrice;
            }
          "
          prop="extractCountPrice"
        />
        <el-table-column
          sortable
          label="已提现次数"
          min-width="120"
          :sort-method="
            (a, b) => {
              return a.extractCountNum - b.extractCountNum;
            }
          "
          prop="extractCountNum"
        />
        <el-table-column
          sortable
          label="未提现金额"
          min-width="120"
          :sort-method="
            (a, b) => {
              return a.brokeragePrice - b.brokeragePrice;
            }
          "
          prop="brokeragePrice"
        />
        <el-table-column
          sortable
          label="冻结中佣金"
          min-width="120"
          :sort-method="
            (a, b) => {
              return a.freezeBrokeragePrice - b.freezeBrokeragePrice;
            }
          "
          prop="freezeBrokeragePrice"
        />
        <el-table-column prop="promoterTime" label="成为推广员时间" min-width="150" />
        <el-table-column prop="spreadNickname" label="上级推广人" min-width="150" />
        <el-table-column label="操作" min-width="150" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              class="mr10"
              @click="onSpread(scope.row.uid, 'man', '推广人')"
              v-hasPermi="['admin:retail:spread:list']"
              >推广人</el-button
            >
            <el-dropdown>
              <span class="el-dropdown-link"> 更多<i class="el-icon-arrow-down el-icon--right" /> </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  @click.native="onSpreadOrder(scope.row.uid, 'order', '推广订单')"
                  v-if="checkPermi(['admin:retail:spread:order:list'])"
                  >推广订单</el-dropdown-item
                >
                <!--<el-dropdown-item @click.native="onSpreadType(scope.row.uid)">推广方式</el-dropdown-item>-->
                <el-dropdown-item
                  @click.native="clearSpread(scope.row)"
                  v-if="scope.row.spreadNickname && scope.row.spreadNickname !== '无'"
                  v-hasPermi="['admin:retail:spread:clean']"
                  >清除上级推广人</el-dropdown-item
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

    <!--推广人-->
    <el-dialog :title="titleName + '列表'" :visible.sync="dialogVisible" width="900px" :before-close="handleClose">
      <div class="container">
        <el-form size="small" label-width="100px">
          <el-form-item v-if="this.onName !== 'man'" key="1" label="时间选择：" class="width100">
            <el-radio-group
              v-model="spreadFrom.dateLimit"
              type="button"
              class="mr20"
              size="small"
              @change="selectChangeSpread(spreadFrom.dateLimit)"
            >
              <el-radio-button v-for="(item, i) in fromList.fromTxt" :key="i" :label="item.val">{{
                item.text
              }}</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-model="timeValSpread"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              size="small"
              type="daterange"
              placement="bottom-end"
              placeholder="自定义时间"
              style="width: 250px"
              @change="onchangeTimeSpread"
            />
          </el-form-item>
          <el-form-item label="用户类型：">
            <el-radio-group v-model="spreadFrom.type" size="small" @change="onChanges">
              <el-radio-button label="0">全部</el-radio-button>
              <el-radio-button label="1">一级推广人</el-radio-button>
              <el-radio-button label="2">二级推广人</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="关键字：" class="width100">
            <el-input
              v-model="spreadFrom.nickName"
              :placeholder="onName === 'order' ? '请输入订单号' : '请输入姓名、电话、UID'"
              class="selWidth"
              size="small"
              clearable
            >
              <el-button slot="append" icon="el-icon-search" size="small" @click="onChanges" />
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-if="onName === 'man'"
        key="men"
        v-loading="spreadLoading"
        :data="spreadData.data"
        style="width: 100%"
        size="mini"
        class="table"
        highlight-current-row
      >
        <el-table-column prop="uid" label="ID" width="60" />
        <el-table-column label="头像" min-width="80">
          <template slot-scope="scope">
            <div class="demo-image__preview">
              <el-image :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="用户信息" min-width="130" />
        <el-table-column prop="is_promoter" label="是否推广员" min-width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.isPromoter | filterYesOrNo }}</span>
          </template>
        </el-table-column>
        <el-table-column sortable label="推广人数" min-width="120" prop="spreadCount" />
        <el-table-column sortable label="订单数" min-width="120" prop="payCount" />
        <!--<el-table-column-->
        <!--prop="spreadTime"-->
        <!--label="关注时间"-->
        <!--min-width="150"-->
        <!--/>-->
      </el-table>
      <el-table
        v-if="onName === 'order'"
        key="order"
        v-loading="spreadLoading"
        :data="spreadData.data"
        style="width: 100%"
        size="mini"
        class="table"
        highlight-current-row
      >
        <el-table-column prop="orderId" label="订单ID" min-width="120" />
        <el-table-column label="用户信息" min-width="150">
          <template slot-scope="scope">
            <span>{{ scope.row.realName }}<el-divider direction="vertical"></el-divider>{{ scope.row.userPhone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="时间" min-width="150" />
        <el-table-column sortable label="返佣金额" min-width="120" prop="price" />
      </el-table>
      <div class="block">
        <el-pagination
          :page-sizes="[10, 20, 30, 40]"
          :page-size="spreadFrom.limit"
          :current-page="spreadFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="spreadData.total"
          @size-change="handleSizeChangeSpread"
          @current-change="pageChangeSpread"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  promoterListApi,
  spreadStatisticsApi,
  spreadListApi,
  spreadOrderListApi,
  spreadClearApi,
} from '@/api/distribution';
import cardsData from '@/components/cards/index';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'AccountsUser',
  components: { cardsData },
  data() {
    return {
      cardLists: [],
      timeVal: [],
      tableData: {
        data: [],
        total: 0,
      },
      listLoading: true,
      tableFrom: {
        dateLimit: '',
        keywords: '',
        page: 1,
        limit: 20,
      },
      fromList: this.$constants.fromList,
      dialogVisible: false,
      spreadData: {
        data: [],
        total: 0,
      },
      spreadFrom: {
        page: 1,
        limit: 10,
        dateLimit: '',
        type: 0,
        nickName: '',
        uid: '',
      },
      timeValSpread: [],
      spreadLoading: false,
      uid: '',
      onName: '',
      titleName: '',
    };
  },
  mounted() {
    // this.spreadStatistics()
    this.getList();
  },
  methods: {
    checkPermi,
    seachList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    // 统计
    // spreadStatistics() {
    //   spreadStatisticsApi({ dateLimit: this.tableFrom.dateLimit, keywords: this.tableFrom.nickName}).then((res) => {
    //     this.cardLists = [
    //       { name: '分销人员人数', count: res.distributionNum },
    //       { name: '发展会员人数', count: res.developNum },
    //       { name: '推广订单总数', count: res.orderNum },
    //       { name: '推广订单金额（元）', count: res.orderPriceCount },
    //       { name: '提现次数', count: res.withdrawCount }
    //     ]
    //   })
    // },
    // 清除
    clearSpread(row) {
      this.$modalSure('解除【' + row.nickname + '】的上级推广人吗').then(() => {
        spreadClearApi(row.uid).then((res) => {
          this.$message.success('清除成功');
          this.getList();
        });
      });
    },
    onSpread(uid, n, p) {
      this.onName = n;
      this.titleName = p;
      this.uid = uid;
      this.dialogVisible = true;
      this.spreadFrom = {
        page: 1,
        limit: 10,
        dateLimit: '',
        type: 0,
        nickName: '',
        uid: uid,
      };
      this.getListSpread();
    },
    handleClose() {
      this.dialogVisible = false;
    },
    // 选择时间
    selectChangeSpread(tab) {
      this.timeValSpread = [];
      this.spreadFrom.dateLimit = tab;
      this.spreadFrom.page = 1;
      this.onName === 'man' ? this.getListSpread() : this.getSpreadOrderList();
    },
    // 具体日期
    onchangeTimeSpread(e) {
      this.timeValSpread = e;
      this.tableFrom.dateLimit = e ? this.timeValSpread.join(',') : '';
      this.spreadFrom.page = 1;
      this.onName === 'man' ? this.getListSpread() : this.getSpreadOrderList();
    },
    onChanges() {
      this.spreadFrom.page = 1;
      this.onName === 'man' ? this.getListSpread() : this.getSpreadOrderList();
    },
    // 推广人列表
    getListSpread() {
      this.spreadLoading = true;
      spreadListApi({ page: this.spreadFrom.page, limit: this.spreadFrom.limit }, this.spreadFrom)
        .then((res) => {
          this.spreadData.data = res.list;
          this.spreadData.total = res.total;
          this.spreadLoading = false;
        })
        .catch(() => {
          this.spreadLoading = false;
        });
    },
    pageChangeSpread(page) {
      this.spreadFrom.page = page;
      this.onName === 'man' ? this.getListSpread(this.uid) : this.getSpreadOrderList(this.uid);
    },
    handleSizeChangeSpread(val) {
      this.spreadFrom.limit = val;
      this.onName === 'man' ? this.getListSpread(this.uid) : this.getSpreadOrderList(this.uid);
    },
    // 推广订单
    onSpreadOrder(uid, n, p) {
      this.uid = uid;
      this.onName = n;
      this.titleName = p;
      this.dialogVisible = true;
      this.spreadFrom = {
        page: 1,
        limit: 10,
        dateLimit: '',
        type: 0,
        nickName: '',
        uid: uid,
      };
      this.getSpreadOrderList();
    },
    getSpreadOrderList() {
      this.spreadLoading = true;
      spreadOrderListApi({ page: this.spreadFrom.page, limit: this.spreadFrom.limit }, this.spreadFrom)
        .then((res) => {
          this.spreadData.data = res.list;
          this.spreadData.total = res.total;
          this.spreadLoading = false;
        })
        .catch(() => {
          this.spreadLoading = false;
        });
    },
    selectChange(tab) {
      this.tableFrom.dateLimit = tab;
      this.tableFrom.page = 1;
      this.timeVal = [];
      // this.spreadStatistics()
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
    getList() {
      this.listLoading = true;
      promoterListApi(this.tableFrom)
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
  },
};
</script>

<style scoped>
.selWidth {
  width: 350px;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
  font-size: 12px;
}
.el-icon-arrow-down {
  font-size: 12px;
}
</style>
