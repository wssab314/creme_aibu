<template>
  <section class="app-main">
    <transition name="fade-transform" mode="out-in">
      <keep-alive :include="cachedViews">
        <router-view :key="key" />
      </keep-alive>
    </transition>
  </section>
</template>

<script>
export default {
  name: 'AppMain',
  data() {
    return {};
  },
  computed: {
    cachedViews() {
      return this.$store.state.tagsView.cachedViews;
    },
    key() {
      return this.$route.path;
    },
  },
};
</script>

<style lang="scss" scoped>
.footers {
  text-align: center;
  font-size: 14px;
  color: #808695;
  .title {
    font-size: 14px;
    color: #808695;
  }
}

.fixed-header + .app-main {
  padding-top: 50px;
}

.hasTagsView {
  .app-main {
    background: #f5f5f5;
    /* 84 = navbar + tags-view = 50 + 34 */
    min-height: calc(100vh - 36px);
    overflow-y: auto;
  }

  .fixed-header + .app-main {
    padding-top: 80px;
  }
}
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
</style>
