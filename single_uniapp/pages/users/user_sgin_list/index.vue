<template>
	<view :data-theme="theme">
		<view class='sign-record'>
		   <view class='list pad30' v-for="(item,index) in signList" :key="index">
		      <view class='item'>
		         <view class='data'>{{item.month}}</view>
		         <view class='listn borRadius14'>
		            <view class='itemn acea-row row-between-wrapper' v-for="(itemn,indexn) in item.list" :key="indexn">
		               <view>
		                  <view class='name line1'>{{itemn.title}}</view>
		                  <view>{{itemn.createDay}}</view>
		               </view>
		               <view class='num font_color'>+{{itemn.number}}</view>
		            </view>
		         </view>
		      </view>
		   </view>
		    <view class='loadingicon acea-row row-center-wrapper'>
		        <text class='loading iconfont icon-jiazai' :hidden='loading==false'></text>{{loadtitle}}
		     </view>
		</view>
	</view>
</template>

<script>
	import { getSignMonthList } from '@/api/user.js';
	import { toLogin } from '@/libs/login.js';
	 import { mapGetters } from "vuex";
	 let app = getApp();
	export default {
		data() {
			return {
				loading:false,
				    loadend:false,
				    loadtitle:'加载更多',
				    page:1,
				    limit:8,
				    signList:[],
					theme:app.globalData.theme,
			};
		},
		computed: mapGetters(['isLogin']),
		watch:{
			isLogin:{
				handler:function(newV,oldV){
					if(newV){
						this.getSignMoneList();
					}
				},
				deep:true
			}
		},
		onLoad(){
			if(this.isLogin){
				this.getSignMoneList();
			}else{
				toLogin();
			}
		},
		onReachBottom: function () {
		    this.getSignMoneList();
		  },
		methods: {
			  /**
			     * 获取签到记录列表
			    */
			    getSignMoneList:function(){
			      let that=this;
			      if(that.loading) return;
			      if(that.loadend) return;
				  that.loading = true;
				  that.loadtitle = "";
			      getSignMonthList({ page: that.page, limit: that.limit }).then(res=>{
			        let list = res.data.list;
			        let loadend = list.length < that.limit;
			        that.signList = that.$util.SplitArray(list, that.signList);
					that.$set(that,'signList',that.signList);
					that.loadend = loadend;
					that.loading = false;
					that.loadtitle = loadend ? "哼😕~我也是有底线的~" : "加载更多"
			      }).catch(err=>{
					that.loading = false;
					that.loadtitle = '加载更多';
			      });
			    },
		}
	}
</script>

<style lang="scss">
	.font_color{
		@include main_color(theme);
	}
</style>
