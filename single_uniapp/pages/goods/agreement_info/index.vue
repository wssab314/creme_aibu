<template>
	<view class="user_about" :data-theme="theme">
		<view>
			<view class="text cancelTxt" :class="{cancelTxt: type == 'useraccountcancelnoticeinfo'}">
				<jyf-parser :html="agreementData?agreementData:''" ref="article" :tag-style="tagStyle"></jyf-parser>
			</view>
		</view>
	</view>
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
	import {
		agreementInfo
	} from '@/api/api.js'
	import parser from "@/components/jyf-parser/jyf-parser";
	let app = getApp();
	export default {
		name: 'user_about',
		components: {
			"jyf-parser": parser,
		},
		data() {
			return {
				theme: app.globalData.theme,
				agreementData: '',
				loaded: false,
				tagStyle: {
					img: 'width:100%;display:block;',
					table: 'width:100%',
					video: 'width:100%'
				},
			}
		},
		onLoad: function(options) {
			this.type = options.from;
			this.setTitle(this.type);
			this.getCacheinfo();
		},
		methods: {
			getCacheinfo() {
				this.loaded = false;
				agreementInfo(this.type).then(res => {
					this.agreementData = res.data ? JSON.parse(res.data).agreement : ''
					this.loaded = true;
				})
			},
			setTitle(e) {
				switch (e) {
					case 'aboutusinfo':
						uni.setNavigationBarTitle({
							title: '关于我们协议'
						})
						break;
					case 'intelligentinfo':
						uni.setNavigationBarTitle({
							title: '平台资质证明'
						})
						break;
					case 'useraccountcancelinfo':
						uni.setNavigationBarTitle({
							title: '用户注销协议'
						})
						break;
					case 'useraccountcancelnoticeinfo':
						uni.setNavigationBarTitle({
							title: '用户注销声明'
						})
						break;
					case 'userinfo':
						uni.setNavigationBarTitle({
							title: '用户注册协议'
						})
						break;
					case 'coupon/agreement/info':
						uni.setNavigationBarTitle({
							title: '优惠券协议'
						})
						break;
					default:
						uni.setNavigationBarTitle({
							title: '用户隐私协议'
						})
						break;
				}
			}
		}
	}
</script>

<style lang="scss">
	.user_about {
		.text {
			font-size: 30rpx;
			font-weight: 400;
			padding: 30rpx;
			color: #282828;
		}

		.cancelTxt {
			overflow: hidden;
			overflow-y: auto;

			image {
				max-width: 100%;
			}
		}

		.cancel {
			position: fixed;
			bottom: 160rpx;
			left: 0;
			z-index: 1;
			width: 100%;

			.checkbox {
				text-align: center;
				font-size: 24rpx;
				font-weight: 400;

				span {
					margin-left: 5rpx;
				}

				.font {
					@include main_color(theme);
					font-style: normal;
				}

				.iconfont {
					font-size: 36rpx;
				}
			}

			.btn {
				width: 690rpx;
				height: 90rpx;
				@include linear-gradient(theme);
				border-radius: 45rpx;
				margin: 0 auto;
				margin-top: 30rpx;
				text-align: center;
				line-height: 90rpx;
				font-size: 32rpx;
				font-weight: 400;
				color: #FFFFFF;
			}
		}
	}

	.outMoal {
		width: 100%;
		height: 100%;
		background: rgba(0, 0, 0, 0.5);
		position: fixed;
		top: 0;
		left: 0;
		z-index: 2;
		display: flex;
		align-items: center;
		justify-content: center;

		.box {
			position: fixed;
			width: 590rpx;
			height: 258rpx;
			background: #FFFFFF;
			opacity: 1;
			border-radius: 20rpx;
			text-align: center;
			padding: 50rpx;

			.title {
				font-size: 30rpx;
				font-weight: 600;
				color: #282828;
			}

			.moalBtn {
				margin-top: 43rpx;
				display: flex;
				justify-content: space-between;

				.ok {
					width: 234rpx;
					height: 66rpx;
					@include coupons_border_color(theme);
					border-radius: 33rpx;
					font-size: 26rpx;
					line-height: 66rpx;
					@include main_color(theme);
				}

				.no {
					width: 234rpx;
					height: 66rpx;
					@include linear-gradient(theme);
					border-radius: 33rpx;
					font-size: 26rpx;
					color: #FFFFFF;
					line-height: 66rpx;
				}
			}
		}
	}

	.btn {
		width: 100%;
		height: 120rpx;
		background-color: #FFFFFF;
		position: fixed;
		bottom: 0;
		line-height: 120rpx;
		padding: 17rpx 0;

		.sure-btn {
			width: 690rpx;
			height: 86rpx;
			margin: 0 auto;
			line-height: 86rpx;
			color: #FFFFFF;
			border-radius: 43rpx;
			@include main_bg_color(theme);
			font-size: 32rpx;
		}
	}
</style>