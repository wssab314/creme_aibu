<template>
  <div class="divBox">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <div class="container">
          <el-form inline>
            <el-form-item label="拼团状态：">
              <el-select
                v-model="tableFrom.isShow"
                placeholder="请选择"
                class="filter-item selWidth mr20"
                @change="getList(1)"
                clearable
              >
                <el-option label="关闭" :value="0" />
                <el-option label="开启" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item label="商品搜索：">
              <el-input v-model="tableFrom.keywords" placeholder="请输入商品名称、ID" class="selWidth" clearable>
                <el-button slot="append" icon="el-icon-search" @click="getList(1)" />
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <router-link :to="{ path: '/marketing/groupBuy/creatGroup' }">
          <el-button type="primary" class="mr10" v-hasPermi="['admin:combination:save']">添加拼团商品</el-button>
        </router-link>
        <el-button class="mr10" @click="exportList" v-hasPermi="['admin:export:excel:combiantion']">导出</el-button>
      </div>
      <el-table
        v-loading="listLoading"
        :data="tableData.data"
        style="width: 100%"
        size="mini"
        ref="multipleTable"
        :header-cell-style="{ fontWeight: 'bold' }"
      >
        <el-table-column prop="id" label="ID" min-width="50" />
        <el-table-column label="拼团图片" min-width="80">
          <template slot-scope="scope">
            <div class="demo-image__preview">
              <el-image
                style="width: 36px; height: 36px"
                :src="scope.row.image"
                :preview-src-list="[scope.row.image]"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="拼团名称" prop="title" min-width="300">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="right" :open-delay="800">
              <div class="text_overflow" slot="reference">{{ scope.row.title }}</div>
              <div class="pup_card">{{ scope.row.title }}</div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="原价" prop="otPrice" min-width="100" align="center" />
        <el-table-column label="拼团价" prop="price" min-width="100" align="center" />
        <el-table-column label="拼团人数" prop="countPeople" min-width="100" align="center" />
        <el-table-column label="参与人数" prop="countPeopleAll" min-width="100" align="center" />
        <el-table-column label="成团数量" prop="countPeoplePink" min-width="100" align="center" />
        <el-table-column label="限量" min-width="100" prop="quotaShow" align="center" />
        <el-table-column label="限量剩余" prop="remainingQuota" min-width="100" align="center" />
        <el-table-column prop="stopTime" label="结束时间" min-width="130">
          <template slot-scope="scope">
            <span>{{ scope.row.stopTime | formatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="拼团状态" min-width="80" fixed="right">
          <template slot-scope="scope">
            <el-switch
              v-if="checkPermi(['admin:combination:update:status'])"
              v-model="scope.row.isShow"
              :active-value="true"
              :inactive-value="false"
              active-text="开启"
              inactive-text="关闭"
              @change="onchangeIsShow(scope.row)"
            />
            <span v-else>{{ scope.row.isShow ? '开启' : '关闭' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="130" fixed="right" align="center">
          <template slot-scope="scope">
            <router-link
              :to="{
                path: scope.row.isShow
                  ? '/marketing/groupBuy/creatGroup/' + scope.row.id + '/info'
                  : '/marketing/groupBuy/creatGroup/' + scope.row.id,
              }"
            >
              <el-button type="text" size="small" v-hasPermi="['admin:combination:info']">{{
                scope.row.isShow ? '详情' : '编辑'
              }}</el-button>
            </router-link>
            <el-button
              type="text"
              size="small"
              @click="handleDelete(scope.row.id, scope.$index)"
              class="mr10"
              v-hasPermi="['admin:combination:delete']"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div class="block mb20">
        <el-pagination
          :page-sizes="[10, 20, 30, 40]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { combinationListApi, combinationDeleteApi, combinationStatusApi, exportcombiantionApi } from '@/api/marketing';
import { formatDates } from '@/utils/index';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'index',
  filters: {
    formatDate(time) {
      if (time !== 0) {
        const date = new Date(time);
        return formatDates(date, 'yyyy-MM-dd hh:mm');
      }
    },
  },
  data() {
    return {
      tableFrom: {
        page: 1,
        limit: 20,
        keywords: '',
        isShow: '',
      },
      listLoading: true,
      tableData: {
        data: [],
        total: 0,
      },
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    checkPermi,
    //导出
    exportList() {
      exportcombiantionApi({ keywords: this.tableFrom.keywords, isShow: this.tableFrom.isShow }).then((res) => {
        window.open(res.fileName);
      });
    },
    // 删除
    handleDelete(id, idx) {
      this.$modalSure('永久删除该商品').then(() => {
        combinationDeleteApi({ id: id }).then(() => {
          this.$message.success('删除成功');
          if (this.tableData.data.length === 1 && this.tableFrom.page > 1)
            this.tableFrom.page = this.tableFrom.page - 1;
          this.getList();
        });
      });
    },
    onchangeIsShow(row) {
      combinationStatusApi({ id: row.id, isShow: row.isShow })
        .then(async () => {
          this.$message.success('修改成功');
          this.getList();
        })
        .catch(() => {
          row.isShow = !row.isShow;
        });
    },
    // 列表
    getList(num) {
      this.listLoading = true;
      this.tableFrom.page = num ? num : this.tableFrom.page;
      combinationListApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list;
          this.tableData.total = res.total;
          this.listLoading = false;
        })
        .catch((res) => {
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
.el-table__body {
  width: 100%;
  table-layout: fixed !important;
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
</style>
