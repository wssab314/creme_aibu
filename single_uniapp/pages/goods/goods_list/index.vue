<template>
	<view :data-theme="theme">
		<view class='productList'>
			<view class='search bg_color acea-row row-between-wrapper'>
				<!-- #ifdef H5 -->
				<view class="iconfont icon-xiangzuo" @click="goback()"></view>
				<!-- #endif -->
				<view class='input acea-row row-between-wrapper'><text class='iconfont icon-sousuo'></text>
					<input placeholder='搜索商品名称' placeholder-class='placeholder' confirm-type='search' name="search"
						:value='where.keyword' @confirm="searchSubmit" maxlength="20"></input>
				</view>
				<view class='iconfont' :class='is_switch==true?"icon-pailie":"icon-tupianpailie"' @click='Changswitch'>
				</view>
			</view>
			<view class='nav acea-row row-middle'>
				<view class='item' :class='title ? "font_color":""' @click='set_where(1)'>{{title ? title:'默认'}}</view>
				<view class='item' @click='set_where(2)'>
					价格
					<image v-if="price==1" :src="urlDomain+'crmebimage/perset/staticImg/up.png'"></image>
					<image v-else-if="price==2" :src="urlDomain+'crmebimage/perset/staticImg/down.png'"></image>
					<image v-else :src="urlDomain+'crmebimage/perset/staticImg/horn.png'"></image>
				</view>
				<view class='item' @click='set_where(3)'>
					销量
					<image v-if="stock==1" :src="urlDomain+'crmebimage/perset/staticImg/up.png'"></image>
					<image v-else-if="stock==2" :src="urlDomain+'crmebimage/perset/staticImg/down.png'"></image>
					<image v-else :src="urlDomain+'crmebimage/perset/staticImg/horn.png'"></image>
				</view>
				<!-- down -->
				<view class='item' :class='nows ? "font_color":""' @click='set_where(4)'>新品</view>
			</view>
			<view :class='is_switch==true?"":"listBox"' v-if="productList.length>0">
				<view class='list acea-row row-between-wrapper' :class='is_switch==true?"":"on"'>
					<view class='item' :class='is_switch==true?"":"on"' hover-class='none'
						v-for="(item,index) in productList" :key="index" @click="godDetail(item)">
						<view class='pictrue' :class='is_switch==true?"":"on"'>
							<image :src='item.image' :class='is_switch==true?"":"on"'></image>
							<span class="pictrue_log_class"
								:class="is_switch === true ? 'pictrue_log_big' : 'pictrue_log'"
								v-if="item.activityH5 && item.activityH5.type === '1'">秒杀</span>
							<span class="pictrue_log_class"
								:class="is_switch === true ? 'pictrue_log_big' : 'pictrue_log'"
								v-if="item.activityH5 && item.activityH5.type === '2'">砍价</span>
							<span class="pictrue_log_class"
								:class="is_switch === true ? 'pictrue_log_big' : 'pictrue_log'"
								v-if="item.activityH5 && item.activityH5.type === '3'">拼团</span>
						</view>
						<view class='text' :class='is_switch==true?"":"on"'>
							<view class='name line1'>{{item.storeName}}</view>
							<view class='x-money' :class='is_switch==true?"":"on"'>￥<text
									class='num'>{{item.price}}</text></view>
							<view class='vip acea-row row-between-wrapper' :class='is_switch==true?"":"on"'>
								<view class='vip-money' v-if="item.vip_price && item.vip_price > 0">￥{{item.vip_price}}
									<image :src="urlDomain+'crmebimage/perset/staticImg/vip.png'"></image>
								</view>
								<view>已售{{Number(item.sales)}}{{item.unitName}}</view>
							</view>
						</view>
					</view>
				</view>
				<view class='loadingicon acea-row row-center-wrapper' v-if='productList.length > 0'>
					<text class='loading iconfont icon-jiazai' :hidden='loading==false'></text>{{loadTitle}}
				</view>
			</view>
		</view>
		<view class='noCommodity' v-if="productList.length==0 && where.page > 1">
			<view class='pictrue'>
				<image :src="urlDomain+'crmebimage/perset/staticImg/noShopper.png'"></image>
			</view>
			<recommend ref="recommendIndex"></recommend>
		</view>
	</view>
</template>

<script>
	import {getProductslist,getProductHot} from '@/api/store.js';
	import recommend from '@/components/recommend';
	import {mapGetters} from "vuex";
	import {goShopDetail} from '@/libs/order.js'
	import animationType from '@/utils/animationType.js'
	let app = getApp();
	export default {
		computed: mapGetters(['uid']),
		components: {
			recommend
		},
		data() {
			return {
				urlDomain: this.$Cache.get("imgHost"),
				productList: [],
				is_switch: true,
				where: {
					keyword: '',
					priceOrder: '',
					salesOrder: '',
					news: 0,
					page: 1,
					limit: 20,
					cid: 0,
				},
				price: 0,
				stock: 0,
				nows: false,
				loadend: false,
				loading: false,
				loadTitle: '加载更多',
				title: '',
				theme:app.globalData.theme
			};
		},
		onLoad: function(options) {
			this.$set(this.where, 'cid', options.cid || 0);
			this.title = options.title || '';
			this.$set(this.where, 'keyword', options.searchValue || '');
			this.get_product_list();
		},
		methods: {
			goback() {
				// #ifdef H5
				return history.back();
				// #endif
				// #ifndef H5
				return uni.navigateBack({
					delta: 1,
				})
				// #endif
			},
			// 去详情页
			godDetail(item) {
				goShopDetail(item, this.uid).then(res => {
					uni.navigateTo({
						animationType: animationType.type,						animationDuration: animationType.duration,
						url: `/pages/goods/goods_details/index?id=${item.id}`
					})
				})
			},
			Changswitch: function() {
				let that = this;
				that.is_switch = !that.is_switch
			},
			searchSubmit: function(e) {
				let that = this;
				that.$set(that.where, 'keyword', e.detail.value);
				that.loadend = false;
				that.$set(that.where, 'page', 1)
				this.get_product_list(true);
			},
			//点击事件处理
			set_where: function(e) {
				switch (e) {
					case 1:
						return;
						break;
					case 2:
						if (this.price == 0) this.price = 1;
						else if (this.price == 1) this.price = 2;
						else if (this.price == 2) this.price = 0;
						this.stock = 0;
						break;
					case 3:
						if (this.stock == 0) this.stock = 1;
						else if (this.stock == 1) this.stock = 2;
						else if (this.stock == 2) this.stock = 0;
						this.price = 0
						break;
					case 4:
						this.nows = !this.nows;
						break;
				}
				this.loadend = false;
				this.$set(this.where, 'page', 1);
				this.get_product_list(true);
			},
			//设置where条件
			setWhere: function() {
				if (this.price == 0) this.where.priceOrder = '';
				else if (this.price == 1) this.where.priceOrder = 'asc';
				else if (this.price == 2) this.where.priceOrder = 'desc';
				if (this.stock == 0) this.where.salesOrder = '';
				else if (this.stock == 1) this.where.salesOrder = 'asc';
				else if (this.stock == 2) this.where.salesOrder = 'desc';
				this.where.news = this.nows ? 1 : 0;
			},
			//查找产品
			get_product_list: function(isPage) {
				let that = this;
				that.setWhere();
				if (that.loadend) return;
				if (that.loading) return;
				if (isPage === true) that.$set(that, 'productList', []);
				that.loading = true;
				that.loadTitle = '';
				getProductslist(that.where).then(res => {
					let list = res.data.list;
					let productList = that.$util.SplitArray(list, that.productList);
					let loadend = list.length < that.where.limit;
					that.loadend = loadend;
					that.loading = false;
					that.loadTitle = loadend ? '已全部加载' : '加载更多';
					that.$set(that, 'productList', productList);
					that.$set(that.where, 'page', that.where.page + 1);
					if (that.productList.length === 0) {
						this.get_host_product();
					} 
				}).catch(err => {
					that.loading = false;
					that.loadTitle = '加载更多';
				});
			},
		},
		onReachBottom() {
			if (this.productList.length > 0) {
				this.get_product_list();
			} else {
				this.$refs.recommendIndex.get_host_product();
			}
		}
	}
</script>

<style scoped lang="scss">
	.bg_color{
		@include main_bg_color(theme);
	}
	.font_color{
		@include main_color(theme);
	}
	.x-money{
		@include price_color(theme);
	}
	.iconfont {
		color: #fff;
	}
    .listBox{
		padding: 20px 15px;
		margin-top: 154rpx;
	}
	.productList .search {
		width: 100%;
		height: 86rpx;
		padding-left: 23rpx;
		box-sizing: border-box;
		position: fixed;
		left: 0;
		top: 0;
		z-index: 9;
	}

	.productList .search .input {
		// width: 640rpx;
		height: 60rpx;
		background-color: #fff;
		border-radius: 50rpx;
		padding: 0 20rpx;
		box-sizing: border-box;
	}

	.productList .search .input input {
		/* #ifdef H5 */
		width: 528rpx;
		/* #endif */
		/* #ifndef H5 */
		width: 548rpx;
		/* #endif */
		height: 100%;
		font-size: 26rpx;
	}

	.productList .search .input .placeholder {
		color: #999;
	}

	.productList .search .input .iconfont {
		font-size: 35rpx;
		color: #555;
	}

	.productList .search .icon-pailie,
	.productList .search .icon-tupianpailie {
		color: #fff;
		width: 62rpx;
		font-size: 40rpx;
		height: 86rpx;
		line-height: 86rpx;
	}

	.productList .nav {
		height: 86rpx;
		color: #454545;
		position: fixed;
		left: 0;
		width: 100%;
		font-size: 28rpx;
		background-color: #fff;
		margin-top: 86rpx;
		top: 0;
		z-index: 9;
	}

	.productList .nav .item {
		width: 25%;
		text-align: center;
	}

	.productList .nav .item.font-color {
		font-weight: bold;
	}

	.productList .nav .item image {
		width: 15rpx;
		height: 19rpx;
		margin-left: 10rpx;
	}

	.productList .list {
		padding: 0 30rpx;
		margin-top: 192rpx;

	}

	.productList .list.on {
		border-radius: 14rpx;
		margin-top: 0 !important;
		background-color: #fff;
		padding: 40rpx 0 0 0;
		// margin: 20rpx 0;
		// background-color: #fff;
	}

	.productList .list .item {
		width: 335rpx;
		background-color: #fff;
		border-radius: 14rpx;
		margin-bottom: 20rpx;
	}

	.productList .list .item.on {
		width: 100%;
		display: flex;
		padding: 0 24rpx 50rpx 24rpx;
		margin: 0;
		border-radius: 14rpx;
	}

	.productList .list .item .pictrue {
		position: relative;
		width: 100%;
		height: 335rpx;
	}

	.productList .list .item .pictrue.on {
		width: 180rpx;
		height: 180rpx;
	}

	.productList .list .item .pictrue image {
		width: 100%;
		height: 100%;
		border-radius: 20rpx 20rpx 0 0;
	}

	.productList .list .item .pictrue image.on {
		border-radius: 6rpx;
	}

	.productList .list .item .text {
		padding: 18rpx 20rpx;
		font-size: 30rpx;
		color: #222;
	}

	.productList .list .item .text.on {
		width: 456rpx;
		padding: 0 0 0 20rpx;
	}

	.productList .list .item .text .money {
		font-size: 26rpx;
		font-weight: bold;
		margin-top: 8rpx;
	}

	.productList .list .item .text .money.on {
		margin-top: 50rpx;
	}

	.productList .list .item .text .money .num {
		font-size: 34rpx;
	}

	.productList .list .item .text .vip {
		font-size: 22rpx;
		color: #aaa;
		margin-top: 7rpx;
	}

	.productList .list .item .text .vip.on {
		margin-top: 12rpx;
	}

	.productList .list .item .text .vip .vip-money {
		font-size: 24rpx;
		color: #282828;
		font-weight: bold;
	}

	.productList .list .item .text .vip .vip-money image {
		width: 46rpx;
		height: 21rpx;
		margin-left: 4rpx;
	}

	.noCommodity {
		background-color: #fff;
		padding-bottom: 30rpx;
		margin-top: 172rpx;
	}
</style>
