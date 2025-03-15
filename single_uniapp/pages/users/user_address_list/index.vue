<template>
	<view :data-theme="theme">
		<view class='line'>
			<image :src="urlDomain+'crmebimage/perset/staticImg/line.jpg'" v-if="addressList.length"></image>
		</view>
		<view class='address-management' :class='addressList.length < 1 && page > 1 ? "fff":""'>
			<radio-group class="radio-group" @change="radioChange" v-if="addressList.length">
				<view class='item borRadius14' v-for="(item,index) in addressList" :key="index">
					<view class='address' @click='goOrder(item)'>
						<view class='consignee'>收货人：{{item.realName}}<text class='phone'>{{item.phone}}</text></view>
						<view>收货地址：{{item.province}}{{item.city}}{{item.district}}{{item.detail}}</view>
					</view>
					<view class='operation acea-row row-between-wrapper'>
						<!-- #ifndef MP -->
						<radio class="radio" :value="index.toString()" :checked="item.isDefault">
							<text>设为默认</text>
						</radio>
						<!-- #endif -->
						<!-- #ifdef MP -->
						<radio class="radio" :value="index" :checked="item.isDefault">
							<text>设为默认</text>
						</radio>
						<!-- #endif -->
						<view class='acea-row row-middle'>
							<view @click='editAddress(item.id)'><text class='iconfont icon-bianji'></text>编辑</view>
							<view @click='delAddress(index)'><text class='iconfont icon-shanchu'></text>删除</view>
						</view>
					</view>
				</view>
			</radio-group>
			<view class='loadingicon acea-row row-center-wrapper' v-if="addressList.length">
				<text class='loading iconfont icon-jiazai' :hidden='loading==false'></text>{{loadTitle}}
			</view>
			<view class='noCommodity' v-if="addressList.length < 1 && page > 1">
				<view class='pictrue'>
					<image :src="urlDomain+'crmebimage/perset/staticImg/noAddress.png'"></image>
				</view>
			</view>
			<view style='height:120rpx;'></view>
		</view>
		<view class='footer acea-row row-between-wrapper'>
			<!-- #ifdef APP-PLUS -->
			<view class='addressBnt bg_color on' @click='addAddress'><text
					class='iconfont icon-tianjiadizhi'></text>添加新地址</view>
			<!-- #endif -->
			<!-- #ifdef MP-->
			<view class='addressBnt bg_color' @click='addAddress'><text class='iconfont icon-tianjiadizhi'></text>添加新地址
			</view>
			<view class='addressBnt wxbnt' @click='getWxAddress'><text class='iconfont icon-weixin2'></text>导入微信地址
			</view>
			<!-- #endif -->
			<!-- #ifdef H5-->
			<view class='addressBnt bg_color' :class="this.$wechat.isWeixin()?'':'on'" @click='addAddress'><text
					class='iconfont icon-tianjiadizhi'></text>添加新地址</view>
			<view v-if="this.$wechat.isWeixin()" class='addressBnt wxbnt' @click='getAddress'><text
					class='iconfont icon-weixin2'></text>导入微信地址</view>
			<!-- #endif -->
		</view>
		<!-- #ifdef MP -->
		<atModel v-if="locationStatus" :locationType="true" @closeModel="modelCancel" @confirmModel="confirmModel"
			:content="locationContent"></atModel>
		<!-- #endif -->
	</view>
</template>

<script>
	import {
		getAddressList,
		setAddressDefault,
		delAddress,
		editAddress,
		postAddress
	} from '@/api/user.js';
	import {
		toLogin
	} from '@/libs/login.js';
	import atModel from '@/components/accredit/index.vue';
	import {
		mapGetters
	} from "vuex";
	let app = getApp();
	export default {
		components: {
			atModel
		},
		data() {
			return {
				urlDomain: this.$Cache.get("imgHost"),
				addressList: [],
				cartId: '',
				pinkId: 0,
				couponId: 0,
				loading: false,
				loadend: false,
				loadTitle: '加载更多',
				page: 1,
				limit: 20,
				bargain: false, //是否是砍价
				combination: false, //是否是拼团
				secKill: false, //是否是秒杀
				theme: app.globalData.theme,
				locationContent: '授权位置信息，提供完整服务',
				locationStatus: false
			};
		},
		computed: mapGetters(['isLogin']),
		watch: {
			isLogin: {
				handler: function(newV, oldV) {
					if (newV) {
						this.getUserAddress(true);
					}
				},
				deep: true
			}
		},
		onLoad(options) {
			if (this.isLogin) {
				this.preOrderNo = options.preOrderNo || 0;
				this.getAddressList(true);
			} else {
				toLogin();
			}
		},
		onShow: function() {
			let that = this;
			that.getAddressList(true);
		},
		methods: {
			modelCancel() {
				this.locationStatus = false;
			},
			confirmModel() {
				uni.getLocation({
					type: 'gcj02',
					altitude: true,
					geocode: true,
					success: function(res) {
						try {
							uni.setStorageSync('user_latitude', res.latitude);
							uni.setStorageSync('user_longitude', res.longitude);
						} catch {}
					}
				});
				this.locationStatus = false;
			},
			/*
			 * 导入微信地址（小程序）
			 */
			getWxAddress: function() {
				let that = this;
				uni.authorize({
					scope: 'scope.address',
					success: function(res) {
						uni.chooseAddress({
							success: function(res) {
								let addressP = {};
								addressP.province = res.provinceName;
								addressP.city = res.cityName;
								addressP.district = res.countyName;
								addressP.cityId = 0;
								editAddress({
									address: addressP,
									isDefault: false,
									realName: res.userName,
									postCode: res.postalCode,
									phone: res.telNumber,
									detail: res.detailInfo,
									id: 0
									//type: 1//区别城市id（导入微信地址无城市id需要后台自己查找）;
								}).then(res => {
									setTimeout(() => {
										that.getAddressList(true);
										that.$util.Tips({
											title: "添加成功",
											icon: 'success'
										});
									}, 0)
								}).catch(err => {
									return that.$util.Tips({
										title: err
									});
								});
							},
							fail: function(res) {
								if (res.errMsg == 'chooseAddress:cancel') return that.$util
									.Tips({
										title: '取消选择'
									});
							},
						})
					},
					fail: function(res) {
						uni.showModal({
							title: '您已拒绝导入微信地址权限',
							content: '是否进入权限管理，调整授权？',
							success(res) {
								if (res.confirm) {
									uni.openSetting({
										success: function(res) {
											console.log(res.authSetting)
										}
									});
								} else if (res.cancel) {
									return that.$util.Tips({
										title: '已取消！'
									});
								}
							}
						})
					}
				})
			},
			/*
			 * 导入微信地址（公众号）
			 */
			getAddress() {
				let that = this;
				that.$wechat.openAddress().then(userInfo => {
					// open();
					editAddress({
							realName: userInfo.userName,
							phone: userInfo.telNumber,
							address: {
								province: userInfo.provinceName,
								city: userInfo.cityName,
								district: userInfo.countryName,
								cityId: 0
							},
							detail: userInfo.detailInfo,
							postCode: userInfo.postalCode,
							isDefault: false,
						})
						.then(() => {
							setTimeout(() => {
								that.getAddressList(true);
								that.$util.Tips({
									title: "添加成功",
									icon: 'success'
								});
							}, 0)
						})
						.catch(err => {
							// close();
							return that.$util.Tips({
								title: err || "添加失败"
							});
						});
				}).catch(err => {
					that.$util.Tips({
						title: err.errMsg || "添加失败"
					});
				});
			},
			/**
			 * 获取地址列表
			 * 
			 */
			getAddressList: function(isPage) {
				let that = this;
				if (isPage) {
					that.loadend = false;
					that.page = 1;
					that.$set(that, 'addressList', []);
				};
				if (that.loading) return;
				if (that.loadend) return;
				that.loading = true;
				that.loadTitle = '';
				getAddressList({
					page: that.page,
					limit: that.limit
				}).then(res => {
					let list = res.data.list;
					let loadend = list.length < that.limit;
					that.addressList = that.$util.SplitArray(list, that.addressList);
					that.$set(that, 'addressList', that.addressList);
					that.loadend = loadend;
					that.loadTitle = loadend ? '哼😕~我也是有底线的~' : '加载更多';
					that.page = that.page + 1;
					that.loading = false;
				}).catch(err => {
					that.loading = false;
					that.loadTitle = '加载更多';
				});
			},
			/**
			 * 设置默认地址
			 */
			radioChange: function(e) {
				let index = parseInt(e.detail.value),
					that = this;
				let address = this.addressList[index];
				if (address == undefined) return that.$util.Tips({
					title: '您设置的默认地址不存在!'
				});
				setAddressDefault(address.id).then(res => {
					for (let i = 0, len = that.addressList.length; i < len; i++) {
						if (i == index) that.addressList[i].isDefault = true;
						else that.addressList[i].isDefault = false;
					}
					that.$util.Tips({
						title: '设置成功',
						icon: 'success'
					}, function() {
						that.$set(that, 'addressList', that.addressList);
					});
				}).catch(err => {
					return that.$util.Tips({
						title: err
					});
				});
			},
			/**
			 * 编辑地址
			 */
			editAddress: function(id) {
				let cartId = this.cartId,
					pinkId = this.pinkId,
					couponId = this.couponId;
				this.cartId = '';
				this.pinkId = '';
				this.couponId = '';
				uni.navigateTo({
					url: '/pages/users/user_address/index?id=' + id + '&cartId=' + cartId + '&pinkId=' +
						pinkId + '&couponId=' +
						couponId + '&secKill' + this.secKill + '&combination=' + this.combination +
						'&bargain=' + this.bargain
				})
			},
			/**
			 * 删除地址
			 */
			delAddress: function(index) {
				let that = this,
					address = this.addressList[index];
				if (address == undefined) return that.$util.Tips({
					title: '您删除的地址不存在!'
				});
				uni.showModal({
					content: '确定删除该地址',
					cancelText: "取消", // 取消按钮的文字  
					confirmText: "确定", // 确认按钮文字  
					showCancel: true, // 是否显示取消按钮，默认为 true
					confirmColor: '#f55850',
					success: (res) => {
						if (res.confirm) {
							delAddress(address.id).then(res => {
								that.addressList.splice(index, 1);
								that.$set(that, 'addressList', that.addressList);
								that.$util.Tips({
									title: '删除成功',
									icon: 'success'
								});
							}).catch(err => {
								return that.$util.Tips({
									title: err
								});
							});
						} else {

						}
					},
				})
			},
			/**
			 * 新增地址
			 */
			addAddress: function() {
				let cartId = this.cartId,
					pinkId = this.pinkId,
					couponId = this.couponId;
				this.cartId = '';
				this.pinkId = '';
				this.couponId = '';
				uni.navigateTo({
					url: '/pages/users/user_address/index?preOrderNo=' + this.preOrderNo
				})
			},
			goOrder: function(item) {
				if (this.preOrderNo) {
					uni.redirectTo({
						url: '/pages/order/order_confirm/index?is_address=1&preOrderNo=' + this.preOrderNo +
							'&addressId=' + item.id
					})
				}
			},
		},
		onReachBottom: function() {
			this.getAddressList();
		}
	}
</script>

<style lang="scss" scoped>
	.address-management {
		padding: 20rpx 30rpx;
	}

	.address-management.fff {
		background-color: #fff;
		height: 1300rpx
	}

	.bg_color {
		@include main_bg_color(theme);
	}

	.line {
		width: 100%;
		height: 3rpx;

		image {
			width: 100%;
			height: 100%;
			display: block;
		}
	}

	.address-management .item {
		background-color: #fff;
		padding: 0 20rpx;
		margin-bottom: 20rpx;
	}

	.address-management .item .address {
		padding: 35rpx 0;
		border-bottom: 1rpx solid #eee;
		font-size: 28rpx;
		color: #282828;
	}

	.address-management .item .address .consignee {
		font-size: 28rpx;
		font-weight: bold;
		margin-bottom: 8rpx;
	}

	.address-management .item .address .consignee .phone {
		margin-left: 25rpx;
	}

	.address-management .item .operation {
		height: 83rpx;
		font-size: 28rpx;
		color: #282828;
	}

	.address-management .item .operation .radio text {
		margin-left: 13rpx;
	}

	.address-management .item .operation .iconfont {
		color: #2c2c2c;
		font-size: 35rpx;
		vertical-align: -2rpx;
		margin-right: 10rpx;
	}

	.address-management .item .operation .iconfont.icon-shanchu {
		margin-left: 35rpx;
		font-size: 38rpx;
	}

	.footer {
		position: fixed;
		width: 100%;
		background-color: #fff;
		bottom: 0;
		height: 106rpx;
		padding: 0 30rpx;
		box-sizing: border-box;
	}

	.footer .addressBnt {
		width: 330rpx;
		height: 76rpx;
		border-radius: 50rpx;
		text-align: center;
		line-height: 76rpx;
		font-size: 30rpx;
		color: #fff;
	}

	.footer .addressBnt.on {
		width: 690rpx;
		margin: 0 auto;
	}

	.footer .addressBnt .iconfont {
		font-size: 35rpx;
		margin-right: 8rpx;
		vertical-align: -1rpx;
	}

	.footer .addressBnt.wxbnt {
		@include left_color(theme);
	}

	/deep/ radio .wx-radio-input.wx-radio-input-checked {
		@include main_bg_color(theme);
		@include coupons_border_color(theme);
	}

	/deep/ radio .uni-radio-input.uni-radio-input-checked {
		@include main_bg_color(theme);
		border: none !important;
	}
</style>