<template>
  <div class="divBox">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <div class="container">
          <el-form ref="form" inline :model="artFrom" size="small" @submit.native.prevent>
            <el-form-item label="提货点名称：">
              <el-select v-model="artFrom.storeId" placeholder="请选择" class="selWidth" clearable @change="search">
                <el-option v-for="item in storeSelectList" :key="item.id" :label="item.name" :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <el-button type="primary" size="small" @click="add" v-hasPermi="['admin:system:staff:save']">添加核销员</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" size="small" :header-cell-style="{ fontWeight: 'bold' }">
        <el-table-column prop="id" label="ID" sortable width="80"> </el-table-column>
        <el-table-column prop="staffName" label="核销员名称" min-width="150"> </el-table-column>
        <el-table-column prop="avatar" label="账号" min-width="150"> </el-table-column>
        <el-table-column prop="phone" label="手机号码" min-width="100"> </el-table-column>
        <el-table-column prop="systemStore.detailedAddress" label="所属提货点" min-width="200"> </el-table-column>
        <el-table-column prop="createTime" label="添加时间" min-width="180"> </el-table-column>
        <el-table-column fixed="right" label="操作" min-width="120">
          <template slot-scope="{ row, index }">
            <el-button type="text" size="small" @click="edit(row.id)"
              v-hasPermi="['admin:system:staff:info']">编辑</el-button>
            <!--<el-divider direction="vertical"></el-divider>-->
            <el-button type="text" size="small" @click="storeDelete(row.id)"
              v-hasPermi="['admin:system:staff:delete']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="mt20" @size-change="sizeChange" @current-change="pageChange" :current-page="artFrom.page"
        :page-sizes="[20, 40, 60, 100]" :page-size="artFrom.limit" layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
      <add-clerk-list ref="template" @tableList="tableList" :storeSelectList="storeSelectList"></add-clerk-list>
    </el-card>
  </div>
</template>

<script>
import addClerkList from './addClerk';
import { storeStaffListApi, storeListApi, storeStaffDeleteApi, storeStaffUpdateStatusApi } from '@/api/storePoint';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'clerkList',
  components: { addClerkList },
  data() {
    return {
      storeSelectList: [],
      artFrom: {
        page: 1,
        limit: 20,
        storeId: '',
      },
      loading: false,
      tableData: [],
      total: 0,
    };
  },
  created() {
    if (checkPermi(['admin:system:staff:list'])) this.tableList();
    this.storeList();
  },
  methods: {
    //是否显示
    onchangeIsShow(id, isShow) {
      let that = this;
      storeStaffUpdateStatusApi({ id: id, status: isShow })
        .then(() => {
          that.$message.success('操作成功');
          that.tableList();
        })
        .catch(() => {
          row.isShow = !row.isShow;
        });
    },
    storeList() {
      let artFrom = {
        page: 1,
        limit: 9999,
        status: '1',
        keywords: '',
      };
      storeListApi(artFrom).then((res) => {
        this.storeSelectList = res.list;
      });
    },
    tableList() {
      let that = this;
      that.loading = true;
      storeStaffListApi(that.artFrom)
        .then((res) => {
          that.loading = false;
          that.tableData = res.list;
          that.total = res.total;
        })
        .catch((res) => {
          that.$message.error(res.message);
        });
    },
    //切换页数
    pageChange(index) {
      this.artFrom.page = index;
      this.tableList();
    },
    //切换显示条数
    sizeChange(index) {
      this.artFrom.limit = index;
      this.tableList();
    },
    //搜索
    search() {
      this.artFrom.page = 1;
      this.tableList();
    },
    //刪除
    storeDelete(id) {
      let that = this;
      that
        .$modalSure()
        .then(() => {
          storeStaffDeleteApi({ id: id }).then(() => {
            that.$message.success('删除成功');
            that.tableList();
          });
        })
        .catch((res) => {
          that.$message.error(res.message);
        });
    },
    //添加
    add() {
      this.$refs.template.dialogFormVisible = true;
    },
    //编辑
    edit(id) {
      this.$refs.template.dialogFormVisible = true;
      this.$refs.template.getInfo(id);
    },
  },
};
</script>

<style scoped lang="scss"></style>
