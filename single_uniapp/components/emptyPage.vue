<template>
	<view class="empty-box">
		<image :src="urlDomain+'crmebimage/perset/staticImg/empty-box.png'"></image>
		<view class="txt">{{title}}</view>
	</view>
</template>

<script>
	export default{
		props: {
			title: {
				type: String,
				default: '暂无记录',
			},
		},
		data(){
			return{
				urlDomain: this.$Cache.get("imgHost"),
			}
		}
	}
	
</script>

<style lang="scss">
	.empty-box{
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		margin-top: 200rpx;
		padding-bottom: 60rpx;
		image{
			width: 414rpx;
			height: 240rpx;
		}
		.txt{
			font-size: 26rpx;
			color: #999;
		}
	}
</style>
