<template>
	<view style="touch-action: none;">
		<view class="home" style="position:fixed;" :style="{ top: top + 'px'}" id="right-nav" @touchmove.stop.prevent="setTouchMove">
			<view class="homeCon bg-color-red" :class="homeActive === true ? 'on' : ''" v-if="homeActive">
				<navigator hover-class='none' url='/pages/index/index' open-type='switchTab' class='iconfont icon-shouye-xianxing'></navigator>
				<navigator hover-class='none' url='/pages/order_addcart/order_addcart' open-type='switchTab' class='iconfont icon-caigou-xianxing'></navigator>
				<navigator hover-class='none' url='/pages/user/index' open-type='switchTab' class='iconfont icon-yonghu1'></navigator>
			</view>
			<view @click="open" class="pictrueBox">
				<view class="pictrue">
					<image :src="
              homeActive === true
                ? `${urlDomain}crmebimage/perset/staticImg/close.gif`
                : `${urlDomain}/crmebimage/perset/staticImg/open.gif`
            "
					 class="image" />
				</view>
			</view>
		</view>
	</view>
</template>
<script>
	import {
		mapGetters
	} from "vuex";
	export default {
		name: "Home",
		props: {},
		data: function() {
			return {
				urlDomain: this.$Cache.get("imgHost"),
				top: "500"
			};
		},
		computed: mapGetters(["homeActive"]),
		methods: {
			setTouchMove(e) {
				var that = this;
				if (e.touches[0].clientY < 545 && e.touches[0].clientY > 66) {
					that.top = e.touches[0].clientY
					// that.setData({
					// 	top: e.touches[0].clientY
					// })
				}
			},
			open: function() {
				this.homeActive ?
					this.$store.commit("CLOSE_HOME") :
					this.$store.commit("OPEN_HOME");
			}
		},
		created() {
		}
	};
</script>

<style scoped lang="scss">
	.pictrueBox {
		width: 130rpx;
		height: 120rpx;
	}

	/*返回主页按钮*/
	.home {
		position: fixed;
		color: white;
		text-align: center;
		z-index: 9999;
		right: 15rpx;
		display: flex;
	}

	.home .homeCon {
		border-radius: 50rpx;
		opacity: 0;
		height: 0;
		color: $theme-color;
		width: 0;
	}

	.home .homeCon.on {
		opacity: 1;
		animation: bounceInRight 0.5s cubic-bezier(0.215, 0.610, 0.355, 1.000);
		width: 300rpx;
		height: 86rpx;
		margin-bottom: 20rpx;
		display: flex;
		justify-content: center;
		align-items: center;
		/* background: #f44939 !important; */
		@include main_bg_color(theme);
	}

	.home .homeCon .iconfont {
		font-size: 48rpx;
		color: #fff;
		display: inline-block;
		margin: 0 auto;
	}

	.home .pictrue {
		width: 86rpx;
		height: 86rpx;
		border-radius: 50%;
		margin: 0 auto;
	}

	.home .pictrue .image {
		@include main_bg_color(theme);
		width: 100%;
		height: 100%;
		border-radius: 50%;
		transform: rotate(90deg);
		ms-transform: rotate(90deg);
		moz-transform: rotate(90deg);
		webkit-transform: rotate(90deg);
		o-transform: rotate(90deg);
	}
</style>
