<template>
	<view class='product-bg'>
		<swiper :indicator-dots="indicatorDots" :indicator-active-color="indicatorBg" :autoplay="autoplay" :circular="circular"
		 :interval="interval" :duration="duration" @change="change" v-if="isPlay"> 
		 <!-- #ifndef APP-PLUS -->
			<swiper-item v-if="videoline">
				<view class="item">
					<view v-show="!controls" style="width:100%;height:100% ">
						<video id="myVideo" :src='videoline' objectFit="cover" controls style="width:100%;height:100% "
						 show-center-play-btn show-mute-btn="true" auto-pause-if-navigate :custom-cache="false" :enable-progress-gesture="false" :poster="imgUrls[0]" @pause="videoPause"></video>
					</view>
					<view class="poster" v-show="controls">
						<image class="image" :src="imgUrls[0]"></image>
					</view>
					<view class="stop" v-show="controls" @tap="bindPause">
						<image class="image" :src="urlDomain+'crmebimage/perset/staticImg/stop.png'"></image>
					</view>
				</view>
			</swiper-item>
			<!-- #endif -->
			<!-- #ifdef APP-PLUS -->
			<swiper-item v-if="videoline">
				<view class="item">
					<view class="poster" v-show="controls">
						<image class="image" :src="imgUrls[0]"></image>
					</view>
					<view class="stop" v-show="controls" @tap="bindPause">
						<image class="image" :src="urlDomain+'crmebimage/perset/staticImg/stop.png'"></image>
					</view>
				</view>
			</swiper-item>
			<!-- #endif -->
			<block v-for="(item,index) in imgUrls" :key='index'>
				<swiper-item v-if="videoline?index>=1:index>=0">
					<image :src="item" class="slide-image" />
				</swiper-item>
			</block>
		</swiper>
		<!-- #ifdef APP-PLUS -->
		<view v-if="!isPlay" style="width: 100%; height: 750rpx;">
			<video id="myVideo" :src='videoline' objectFit="cover" controls style="width:100%;height:100% "
			 show-center-play-btn show-mute-btn="true" autoplay="true" auto-pause-if-navigate :custom-cache="false" :enable-progress-gesture="false" :poster="imgUrls[0]" @pause="videoPause"></video>
		</view>
		<!-- #endif -->
	</view>
</template>

<script>
	import {setThemeColor} from '@/utils/setTheme.js'
	export default {
		props: {
			imgUrls: {
				type: Array,
				default: function() {
					return [];
				}
			},
			videoline: {
				type: String,
				value: ""
			}
		},
		data() {
			return {
				urlDomain: this.$Cache.get("imgHost"),
				indicatorDots: true,
				circular: true,
				autoplay: true,
				interval: 3000,
				duration: 500,
				currents: "1",
				controls: true,
				isPlay:true,
				videoContext:'',
				indicatorBg:'#e93323',
			};
		},
		created(){
			let that = this;
			that.indicatorBg = setThemeColor();
		},
		mounted() {
			if(this.videoline){
				this.imgUrls.shift()
			}
			// #ifndef APP-PLUS
			this.videoContext = uni.createVideoContext('myVideo', this);
			// #endif
		},
		methods: {
			videoPause(e){
				// #ifdef APP-PLUS
				this.isPlay= true
				this.autoplay = true
				// #endif
			},
			bindPause: function() {
				
				// #ifndef APP-PLUS
				this.videoContext.play();
				this.$set(this, 'controls', false)
				this.autoplay = false
				// #endif
				// #ifdef APP-PLUS
				this.isPlay= false
				this.videoContext = uni.createVideoContext('myVideo', this);
				this.videoContext.play();
				// #endif
			},
			change: function(e) {
				this.$set(this, 'currents', e.detail.current + 1);
			}
		}
	}
</script>

<style scoped lang="scss">
	.product-bg {
		width: 100%;
		height: 750rpx;
		position: relative;
	}

	.product-bg swiper {
		width: 100%;
		height: 100%;
		position: relative;
	}

	.product-bg .slide-image {
		width: 100%;
		height: 100%;
	}

	.product-bg .pages {
		position: absolute;
		background-color: #fff;
		height: 34rpx;
		padding: 0 10rpx;
		border-radius: 3rpx;
		right: 30rpx;
		bottom: 30rpx;
		line-height: 34rpx;
		font-size: 24rpx;
		color: #050505;
	}

	#myVideo {
		width: 100%;
		height: 100%
	}

	.product-bg .item {
		position: relative;
		width: 100%;
		height: 100%;
	}

	.product-bg .item .poster {
		position: absolute;
		top: 0;
		left: 0;
		height: 750rpx;
		width: 100%;
		z-index: 9;
	}

	.product-bg .item .poster .image {
		width: 100%;
		height: 100%;
	}

	.product-bg .item .stop {
		position: absolute;
		top: 50%;
		left: 50%;
		width: 136rpx;
		height: 136rpx;
		margin-top: -68rpx;
		margin-left: -68rpx;
		z-index: 9;
	}

	.product-bg .item .stop .image {
		width: 100%;
		height: 100%;
	}
</style>