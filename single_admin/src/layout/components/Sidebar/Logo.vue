<template>
  <div class="sidebar-logo-container" :class="{ collapse: collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img v-if="siteLogoSquare" :src="siteLogoSquare" class="sidebar-logo-small" />
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <img v-if="siteLogoLeftTop" :src="siteLogoLeftTop" class="sidebar-logo-big" />
      </router-link>
    </transition>
  </div>
</template>

<script>
import * as systemConfigApi from '@/api/systemConfig.js';
export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true,
    },
  },
  data() {
    return {
      title: 'Vue Element Admin',
      siteLogoLeftTop: '', //左上角logo大
      siteLogoSquare: '', //左上角logo小
    };
  },
  mounted() {
    this.getLogo();
  },
  methods: {
    //获取左上角菜单logo
    getLogo() {
      systemConfigApi.getSiteLogoApi().then((data) => {
        this.siteLogoLeftTop = data.siteLogoLeftTop;
        this.siteLogoSquare = data.siteLogoSquare;
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebar-logo-big {
  width: auto;
  height: 40px;
  vertical-align: middle;
  margin-right: 12px;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 65px;
  line-height: 65px;
  text-align: center;
  overflow: hidden;

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;

    & .sidebar-logo-small {
      width: 35px;
      height: 35px;
      vertical-align: middle;
    }

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: #fff;
      font-weight: 600;
      line-height: 50px;
      font-size: 14px;
      font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
      vertical-align: middle;
    }
  }

  &.collapse {
    .sidebar-logo {
      margin-right: 0px;
    }
  }
}
</style>
