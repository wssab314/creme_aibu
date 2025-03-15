<template>
  <div class="navbar">
    <hamburger
      id="hamburger-container"
      :is-active="sidebar.opened"
      class="hamburger-container"
      @toggleClick="toggleSideBar"
    />
    <breadcrumb id="breadcrumb-container" class="breadcrumb-container" v-if="!topNav" />
    <top-nav id="topmenu-container" class="topmenu-container" v-if="topNav" />
    <div class="right-menu">
      <template v-if="device !== 'mobile'">
        <search id="header-search" class="right-menu-item" />
        <!-- <error-log class="errLog-container right-menu-item hover-effect" /> -->
        <screenfull id="screenfull" class="right-menu-item hover-effect" />
        <span
          class="iconfont iconios-notifications-outline right-menu-item"
          style="font-size: 22px; opacity: 0.5"
        ></span>
      </template>
      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
        <div class="avatar-wrapper">{{ JavaInfo.realName }}<i class="el-icon-arrow-down el-icon--right"></i></div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/">
            <el-dropdown-item>主页</el-dropdown-item>
          </router-link>
          <router-link :to="{ path: '/maintain/user' }" v-if="!isPhone">
            <el-dropdown-item>个人中心</el-dropdown-item>
          </router-link>
          <router-link :to="{ path: '/maintain/update' }" v-if="!isPhone">
            <el-dropdown-item>修改密码</el-dropdown-item>
          </router-link>
          <el-dropdown-item @click.native="setting = true">
            <span>布局设置</span>
          </el-dropdown-item>
          <el-dropdown-item @click.native="logout">
            <span>退出</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import TopNav from '@/components/TopNav';
import Breadcrumb from '@/components/Breadcrumb';
import Hamburger from '@/components/Hamburger';
import ErrorLog from '@/components/ErrorLog';
import Screenfull from '@/components/Screenfull';
import Search from '@/components/HeaderSearch';
import { unbindApi } from '@/api/wxApi';
import Cookies from 'js-cookie';
export default {
  components: {
    TopNav,
    Breadcrumb,
    Hamburger,
    ErrorLog,
    Screenfull,
    Search,
  },
  data() {
    return {
      isPhone: this.$wechat.isPhone(),
      JavaInfo: JSON.parse(Cookies.get('JavaInfo')),
    };
  },
  watch: {
    show(value) {
      if (value && !this.clickNotClose) {
        this.addEventClick();
      }
      if (value) {
        addClass(document.body, 'showRightPanel');
      } else {
        removeClass(document.body, 'showRightPanel');
      }
    },
  },
  computed: {
    ...mapGetters(['sidebar', 'avatar', 'device']),
    topNav: {
      get() {
        return this.$store.state.settings.topNav;
      },
    },
    setting: {
      get() {
        return this.$store.state.settings.showSettings;
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'showSettings',
          value: val,
        });
      },
    },
  },
  methods: {
    onUnbundling() {
      this.$modalSure('解绑微信吗').then(() => {
        unbindApi().then(() => {
          this.$message.success('解绑成功');
        });
      });
    },
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar');
    },
    async logout() {
      await this.$store.dispatch('user/logout');
      this.$router.push(`/login?redirect=${this.$route.fullPath}`);
    },
  },
};
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  // box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        position: relative;
        font-size: 14px;
        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
