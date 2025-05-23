<template>
  <div class="divBox relative">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <el-tabs v-model="tableFrom.type" @tab-click="seachList" v-if="checkPermi(['admin:product:tabs:headers'])">
          <el-tab-pane
            :label="item.name + '(' + item.count + ')'"
            :name="item.type.toString()"
            v-for="(item, index) in headeNum"
            :key="index"
          />
        </el-tabs>
        <div class="container mt-1">
          <el-form inline size="small">
            <el-form-item label="商品分类：">
              <el-cascader
                v-model="tableFrom.cateId"
                :options="merCateList"
                :props="props"
                clearable
                class="selWidth mr20"
                @change="seachList"
                size="small"
              />
            </el-form-item>
            <el-form-item label="商品搜索：">
              <el-input
                v-model="tableFrom.keywords"
                placeholder="请输入商品名称，关键字，商品ID"
                class="selWidth"
                size="small"
                clearable
              >
                <el-button
                  slot="append"
                  icon="el-icon-search"
                  @click="seachList"
                  size="small"
                  v-hasPermi="['admin:product:list']"
                />
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <router-link :to="{ path: '/store/list/creatProduct' }">
          <el-button type="primary" class="mr10" v-hasPermi="['admin:product:save']">添加商品</el-button>
        </router-link>
        <el-button type="success" class="mr10" @click="onCopy" v-hasPermi="['admin:product:save']">商品采集</el-button>
        <el-button icon="el-icon-upload2" @click="exports" v-hasPermi="['admin:export:excel:product']">导出</el-button>
      </div>
      <el-table
        v-loading="listLoading"
        :data="tableData.data"
        style="width: 100%"
        size="mini"
        :highlight-current-row="true"
        :header-cell-style="{ fontWeight: 'bold' }"
      >
        <el-table-column type="expand">
          <template slot-scope="props">
            <el-form label-position="left" inline class="demo-table-expand">
              <el-form-item label="商品分类：">
                <span v-for="(item, index) in props.row.cateValues.split(',')" :key="index" class="mr10">{{
                  item
                }}</span>
              </el-form-item>
              <el-form-item label="市场价：">
                <span>{{ props.row.otPrice }}</span>
              </el-form-item>
              <el-form-item label="成本价：">
                <span>{{ props.row.cost }}</span>
              </el-form-item>
              <el-form-item label="收藏：">
                <span>{{ props.row.collectCount }}</span>
              </el-form-item>
              <el-form-item label="虚拟销量：">
                <span>{{ props.row.ficti }}</span>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" min-width="50" v-if="checkedCities.includes('ID')" />
        <el-table-column label="商品图" min-width="80" v-if="checkedCities.includes('商品图')">
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
        <el-table-column
          label="商品名称"
          prop="storeName"
          min-width="300"
          v-if="checkedCities.includes('商品名称')"
          :show-overflow-tooltip="true"
        >
        </el-table-column>
        <el-table-column
          prop="price"
          label="商品售价"
          min-width="90"
          align="center"
          v-if="checkedCities.includes('商品售价')"
        />
        <el-table-column
          prop="sales"
          label="销量"
          min-width="90"
          align="center"
          v-if="checkedCities.includes('销量')"
        />
        <el-table-column
          prop="stock"
          label="库存"
          min-width="90"
          align="center"
          v-if="checkedCities.includes('库存')"
        />
        <el-table-column prop="sort" label="排序" min-width="70" align="center" v-if="checkedCities.includes('排序')" />

        <el-table-column label="添加时间" min-width="120" align="center" v-if="checkedCities.includes('操作时间')">
          <template slot-scope="scope">
            <span>{{ scope.row.addTime | formatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80" fixed="right" v-if="checkedCities.includes('状态')">
          <template slot-scope="scope">
            <el-switch
              v-if="checkPermi(['admin:product:up', 'admin:product:down'])"
              :disabled="Number(tableFrom.type) > 2"
              v-model="scope.row.isShow"
              :active-value="true"
              :inactive-value="false"
              active-text="上架"
              inactive-text="下架"
              @change="onchangeIsShow(scope.row)"
            />
            <span v-else>{{ scope.row.isShow ? '上架' : '下架' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" fixed="right" align="center" :render-header="renderHeader">
          <template slot-scope="scope">
            <router-link :to="{ path: '/store/list/creatProduct/' + scope.row.id + '/1' }">
              <el-button type="text" size="small" class="mr10" v-hasPermi="['admin:product:info']">详情</el-button>
            </router-link>
            <router-link :to="{ path: '/store/list/creatProduct/' + scope.row.id }">
              <el-button
                type="text"
                size="small"
                class="mr10"
                v-if="tableFrom.type !== '5' && tableFrom.type !== '1'"
                v-hasPermi="['admin:product:update']"
                >编辑</el-button
              >
            </router-link>
            <el-button
              v-if="tableFrom.type === '5'"
              type="text"
              size="small"
              @click="handleRestore(scope.row.id, scope.$index)"
              v-hasPermi="['admin:product:restore']"
              >恢复商品</el-button
            >
            <el-button
              type="text"
              size="small"
              @click="handleDelete(scope.row.id, tableFrom.type)"
              v-hasPermi="['admin:product:delete']"
              >{{ tableFrom.type === '5' ? '删除' : '加入回收站' }}</el-button
            >
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
    <el-dialog
      title="复制淘宝、天猫、京东、苏宁"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      width="1200px"
      class="taoBaoModal"
      :before-close="handleClose"
    >
      <tao-bao v-if="dialogVisible" @handleCloseMod="handleCloseMod"></tao-bao>
    </el-dialog>
  </div>
</template>

<script>
import {
  productLstApi,
  productDeleteApi,
  categoryApi,
  putOnShellApi,
  offShellApi,
  productHeadersApi,
  productExportApi,
  restoreApi,
  productExcelApi,
} from '@/api/store';
import { getToken } from '@/utils/auth';
import taoBao from './taoBao';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
import { Debounce } from '@/utils/validate.js';
export default {
  name: 'ProductList',
  components: { taoBao },
  data() {
    return {
      props: {
        children: 'child',
        label: 'name',
        value: 'id',
        emitPath: false,
      },
      // roterPre: roterPre,
      headeNum: [],
      listLoading: true,
      tableData: {
        data: [],
        total: 0,
      },
      tableFrom: {
        page: 1,
        limit: 20,
        cateId: '',
        keywords: '',
        type: '1',
      },
      categoryList: [],
      merCateList: [],
      objectUrl: process.env.VUE_APP_BASE_API,
      dialogVisible: false,
      card_select_show: false,
      checkAll: false,
      checkedCities: ['ID', '商品图', '商品名称', '商品售价', '销量', '库存', '排序', '状态', '操作时间'],
      columnData: ['ID', '商品图', '商品名称', '商品售价', '销量', '库存', '排序', '状态', '操作时间'],
      isIndeterminate: true,
    };
  },
  mounted() {
    this.goodHeade();
    this.getList();
    this.getCategorySelect();
    this.checkedCities = this.$cache.local.has('goods_stroge')
      ? this.$cache.local.getJSON('goods_stroge')
      : this.checkedCities;
  },
  methods: {
    checkPermi,
    //恢复商品
    handleRestore: Debounce(function (id) {
      this.$modalSure('恢复商品').then(() => {
        restoreApi(id).then((res) => {
          this.$message.success('操作成功');
          this.goodHeade();
          this.getList();
        });
      });
    }),
    seachList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    handleClose() {
      this.dialogVisible = false;
    },
    handleCloseMod(item) {
      this.dialogVisible = item;
      this.goodHeade();
      this.getList();
    },
    // 复制
    onCopy() {
      this.dialogVisible = true;
    },
    // 导出
    exports() {
      productExcelApi({
        cateId: this.tableFrom.cateId,
        keywords: this.tableFrom.keywords,
        type: this.tableFrom.type,
      }).then((res) => {
        window.location.href = res.fileName;
      });
    },
    // 获取商品表单头数量
    goodHeade() {
      productHeadersApi()
        .then((res) => {
          this.headeNum = res;
        })
        .catch((res) => {
          this.$message.error(res.message);
        });
    },
    // 商户分类；
    getCategorySelect() {
      categoryApi({ status: -1, type: 1 })
        .then((res) => {
          this.merCateList = res;
        })
        .catch((res) => {
          this.$message.error(res.message);
        });
    },
    // 列表
    getList() {
      this.listLoading = true;
      productLstApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list;
          this.tableData.total = res.total;
          this.listLoading = false;
        })
        .catch((res) => {
          this.listLoading = false;
          this.$message.error(res.message);
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
    // 删除
    handleDelete(id, type) {
      this.$modalSure(
        this.tableFrom.type === '5' ? `删除 id 为 ${id} 的商品吗？` : `将 id 为 ${id} 的商品加入回收站吗？`,
      ).then(() => {
        const deleteFlag = type == 5 ? 'delete' : 'recycle';
        productDeleteApi(id, deleteFlag).then(() => {
          this.$message.success('操作成功');
          if (this.tableData.data.length === 1 && this.tableFrom.page > 1)
            this.tableFrom.page = this.tableFrom.page - 1;
          this.getList();
          this.goodHeade();
        });
      });
    },
    onchangeIsShow(row) {
      row.isShow
        ? putOnShellApi(row.id)
            .then(() => {
              this.$message.success('上架成功');
              this.getList();
              this.goodHeade();
            })
            .catch(() => {
              row.isShow = !row.isShow;
            })
        : offShellApi(row.id)
            .then(() => {
              this.$message.success('下架成功');
              this.getList();
              this.goodHeade();
            })
            .catch(() => {
              row.isShow = !row.isShow;
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
      this.$cache.local.setJSON('goods_stroge', this.checkedCities);
      setTimeout(this.$modal.closeLoading(), 1000);
    },
  },
};
</script>

<style scoped lang="scss">
.el-table__body {
  width: 100%;
  table-layout: fixed !important;
}

.taoBaoModal {
  //  z-index: 3333 !important;
}

.demo-table-expand {
  ::v-deep label {
    width: 82px;
  }
}

.demo-table-expand {
  ::v-deep .el-form-item__content {
    width: 77%;
  }
}

.selWidth {
  width: 350px !important;
}

.seachTiele {
  line-height: 30px;
}

.relative {
  position: relative;
}

.card_abs {
  position: absolute;
  padding-bottom: 15px;
  top: 260px;
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

.mt-1 {
  margin-top: 6px;
}

::v-deep .el-checkbox__input.is-checked + .el-checkbox__label {
  color: #606266;
}
</style>
