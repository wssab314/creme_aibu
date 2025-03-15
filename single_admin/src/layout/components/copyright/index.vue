<template>
  <div class="footers acea-row">
    <div class="title mb15" v-text="copyrightNew"></div>
    <el-divider direction="vertical"></el-divider>
    <el-link v-for="item in links" :key="item.key" :href="item.href" target="_blank" class="mr15 mb20">{{
      item.title
    }}</el-link>
  </div>
</template>
<script>
// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------
import { copyrightInfoApi } from '@/api/authInformation';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'i-copyright',
  data() {
    return {
      links: [
        {
          title: '官网',
          key: '1',
          href: 'https://www.crmeb.com',
          blankTarget: true,
        },
        {
          title: '社区',
          key: '2',
          href: 'https://q.crmeb.net/?categoryId=122&sequence=0',
          blankTarget: true,
        },
        {
          title: '文档',
          key: '3',
          href: 'https://help.crmeb.net/crmeb_java/1748037',
          blankTarget: true,
        },
      ],
      copyright: 'Copyright © 2023 西安众邦网络科技有限公司',
      copyrightNew: '',
    };
  },
  mounted() {
    if (checkPermi(['platform:copyright:get:info'])) this.getVersion();
  },
  methods: {
    getVersion() {
      copyrightInfoApi().then((res) => {
        const data = res || {};
        this.copyrightNew = data.companyName ? data.companyName : this.copyright;
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.footers {
  text-align: center;
  font-size: 14px;
  color: #808695;
  justify-content: center;
  .title {
    font-size: 14px;
    color: #808695;
  }
}
.ivu-global-footer {
  /* margin: 48px 0 24px 0; */
  /* padding: 0 16px; */
  margin: 15px 0px;
  text-align: center;
  box-sizing: border-box;
  margin-left: 210px;
}
</style>
