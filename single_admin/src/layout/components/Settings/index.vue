<template>
  <div class="drawer-container">
    <div>
      <h3 class="drawer-title">主题风格设置</h3>
      <div class="setting-drawer-block-checbox">
        <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-dark')">
          <img src="@/assets/imgs/dark.svg" alt="dark" />
          <div v-if="sideTheme === 'theme-dark'" class="setting-drawer-block-checbox-selectIcon" style="display: block">
            <i aria-label="图标: check" class="anticon anticon-check">
              <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true"
                focusable="false" class="">
                <path
                  d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z" />
              </svg>
            </i>
          </div>
        </div>
        <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-light')">
          <img src="@/assets/imgs/light.svg" alt="light" />
          <div v-if="sideTheme === 'theme-light'" class="setting-drawer-block-checbox-selectIcon"
            style="display: block">
            <i aria-label="图标: check" class="anticon anticon-check">
              <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true"
                focusable="false" class="">
                <path
                  d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z" />
              </svg>
            </i>
          </div>
        </div>
      </div>
      <el-divider />
      <div class="drawer-item">
        <span>主题颜色</span>
        <theme-picker style="float: right; height: 26px; margin: -3px 8px 0 0" @change="themeChange" />
      </div>

      <div class="drawer-item">
        <span>开启 TopNav</span>
        <el-switch v-model="topNav" class="drawer-switch" />
      </div>

      <div class="drawer-item" v-if="topNav">
        <span>开启 Icon</span>
        <el-switch v-model="navIcon" class="drawer-switch" />
      </div>

      <div class="drawer-item">
        <span>开启 Tags-Views</span>
        <el-switch v-model="tagsView" class="drawer-switch" />
      </div>

      <div class="drawer-item">
        <span>固定 Header</span>
        <el-switch v-model="fixedHeader" class="drawer-switch" />
      </div>

      <div class="drawer-item">
        <span>显示 Logo</span>
        <el-switch v-model="sidebarLogo" class="drawer-switch" />
      </div>

      <el-divider />
      <el-button type="primary" plain icon="el-icon-document-add" @click="saveSetting">保存配置</el-button>
      <el-button plain icon=" el-icon-refresh" @click="resetSetting">重置配置</el-button>
    </div>
  </div>
</template>

<script>
import ThemePicker from '@/components/ThemePicker';

export default {
  components: { ThemePicker },
  data() {
    return {
      sideTheme: this.$store.state.settings.sideTheme,
      routers: this.$store.state.permission.routes,
    };
  },
  computed: {
    theme: {
      get() {
        return this.$store.state.settings.theme;
      },
    },
    fixedHeader: {
      get() {
        return this.$store.state.settings.fixedHeader;
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'fixedHeader',
          value: val,
        });
      },
    },
    topNav: {
      get() {
        //回调函数 当需要读取当前属性值是执行，根据相关数据计算并返回当前属性的值
        return this.$store.state.settings.topNav;
      },
      set(val) {
        //监视当前属性值的变化，当属性值发生变化时执行，更新相关的属性数据
        //val就是topNav的最新属性值
        this.$store.dispatch('settings/changeSetting', {
          key: 'topNav',
          value: val,
        });
        if (val) {
          let key = this.$route.path.split('/')[1];
          //通过截取当前路由的第一级目录跟顶部一级菜单选中项的val值做匹配
          key = '/' + key;
          this.routers.map((item) => {
            if (key == item.url && item.child) {
              //如果匹配，就给侧边导航赋值为选中项的子级数组
              this.$store.commit('permission/SET_SIDEBAR_ROUTERS', item.child);
            } else if (key == item.url && !item.child) {
              //如果遍历以后val值等于item的url，但是有没有子级，就把它子级赋值给侧边导航的数组，这里针对dashboard控制台
              this.$store.commit('permission/SET_SIDEBAR_ROUTERS', [item]);
            }
          });
        }
        if (!val) {
          //关闭的时候侧边导航的取值还是取默认的routes数组
          this.$store.commit('permission/SET_SIDEBAR_ROUTERS', this.$store.state.permission.routes);
        }
      },
    },
    navIcon: {
      get() {
        return this.$store.state.settings.navIcon;
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          //dispatch：含有异步操作，例如向后台提交数据，写法： this.$store.dispatch('action方法名',值)
          //commit：同步操作，写法：this.$store.commit('mutations方法名',值)
          key: 'navIcon',
          value: val,
        });
      },
    },
    tagsView: {
      get() {
        return this.$store.state.settings.tagsView;
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'tagsView',
          value: val,
        });
      },
    },
    sidebarLogo: {
      get() {
        return this.$store.state.settings.sidebarLogo;
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'sidebarLogo',
          value: val,
        });
      },
    },
  },
  methods: {
    themeChange(val) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'theme',
        value: val,
      });
    },
    handleTheme(val) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'sideTheme',
        value: val,
      });
      this.sideTheme = val;
    },
    saveSetting() {
      this.$modal.loading('正在保存到本地，请稍候...');
      //将设置写入缓存
      this.$cache.local.setJSON('layout-setting', {
        topNav: this.topNav,
        tagsView: this.tagsView,
        fixedHeader: this.fixedHeader,
        sidebarLogo: this.sidebarLogo,
        dynamicTitle: this.dynamicTitle,
        sideTheme: this.sideTheme,
        theme: this.theme,
        navIcon: this.navIcon,
      });
      setTimeout(this.$modal.closeLoading(), 1000);
    },
    resetSetting() {
      this.$modal.loading('正在清除设置缓存并刷新，请稍候...');
      this.$cache.local.remove('layout-setting');
      setTimeout('window.location.reload()', 1000);
    },
  },
};
</script>

<style lang="scss" scoped>
::v-deep .el-switch {
  width: 40px;
}

.drawer-container {
  padding: 24px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;

  .drawer-title {
    margin-bottom: 12px;
    color: rgba(0, 0, 0, 0.85);
    font-size: 14px;
    line-height: 22px;
  }

  .drawer-item {
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
    padding: 12px 0;
  }

  .drawer-switch {
    float: right;
  }

  .setting-drawer-block-checbox {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-top: 10px;
    margin-bottom: 20px;

    .setting-drawer-block-checbox-item {
      position: relative;
      margin-right: 16px;
      border-radius: 2px;
      cursor: pointer;

      img {
        width: 48px;
        height: 48px;
      }

      .setting-drawer-block-checbox-selectIcon {
        position: absolute;
        top: 0;
        right: 0;
        width: 100%;
        height: 100%;
        padding-top: 15px;
        padding-left: 24px;
        color: #1890ff;
        font-weight: 700;
        font-size: 14px;
      }
    }
  }
}
</style>
