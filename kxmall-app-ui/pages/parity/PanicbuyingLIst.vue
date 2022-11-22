<template>
	<view style="background-color: #fff;">
		<view class="" style="background-color: #fff;padding: 8px 10px 0; ">
			<view v-for="(item,index) in tlist" :key="index"
			@click="navToDetail(item.id)"
			class=" padding-tb-sm padding-lr-xs flex" 
			style="width: 100%;height: 180rpx;border-bottom: solid #E6E6E6 1rpx;">
				<view class="" style="width: 144rpx;height: 144rpx;margin-right: 40rpx;position: relative;">
					<image :src="item.img" mode="aspectFit" class="round"
					style="width: 100%;height: 100%;"></image>
					<view class="xie" v-if="!item.stockDto.sales">
						<image src="https://lailemefresh.oss-cn-shanghai.aliyuncs.com/imgs/67h46tCyrXFfT4H.png" mode="" style="width: 100%;height: 100%;"></image>
					</view>
				</view>
				
				<view style="padding-top: 4rpx;padding-bottom: 2rpx;flex-grow: 1;">
					<view class="text-black padding-bottom-xs text-cut" 
					style="font-size: 30rpx;line-height: 42rpx;width: 300rpx;"
					>{{item.title}}</view>
					<view class="flex justify-between" style="width: 100%;padding-top: 12rpx;">
						<view style="flex-grow: 1;" class="flex align-center">
							<view class="text-black padding-right-sm"
							style="font-size: 30rpx;line-height: 42rpx;color: red;"
							>￥{{item.stockDto.price/100}}/{{item.unit}}</view>
							
							<view class="text-black padding-right-sm"
							style="font-size: 30rpx;line-height: 42rpx;text-decoration:line-through;color: #8D8E99;  "
							>￥{{item.originalPrice/100}}/{{item.unit}}</view>
						</view>
						<view class="btn"  :class="item.stockDto.sales?'':'hui'" @click.stop="addCart(index)" v-show="!item.isUse">
							<span v-text="item.stockDto.sales?'抢购':'今日已抢光'" ></span>
						</view>
						<view class="btn hui" v-show="item.isUse">
							<span >
								已抢
							</span>
						</view>
						<!-- <uni-number-box
							class="number-box"
							:min="0" 
							:value="item.num"
							:isMin="item.num===0"
							:index="index"
							@eventChange="numberChange"
							v-if="item.num!==0"
						></uni-number-box>
						<image @click.stop="addCart(index)" src="/static/add.png" mode="aspectFit"
						style="width: 44rpx;height: 44rpx;" v-if="item.num==0"></image> -->
					</view>
				</view>
			</view>
		</view>
		<view class="bg-white flex align-end"
		style="position: fixed;bottom: 0;width: 750rpx;height: 98rpx;padding-left: 38rpx;">
			<view @click="navCart" class="flex align-end" style="width: 346rpx;height: 98rpx;padding-bottom: 16rpx;">
				<view style="position: relative;">
					<image src="/static/tab-icon/tab3_2.png" mode="aspectFit"
					style="width: 56rpx;height: 52rpx;" ></image>
					<view v-if="cartNum+cartNums" class="badge round"
					>{{cartNum+cartNums}}</view>
				</view>
				<view style="font-size: 28rpx;line-height: 40rpx;color: #8D8E99;padding-left: 40rpx;">
					已选{{cartNum+cartNums}}件
				</view>
			</view>
			<view @click="navToCart"  style="font-size: 28rpx;line-height: 40rpx;width: 404rpx;height: 98rpx;"
			 class="flex align-center justify-center bg-green ">
				去购物车
			</view>
		</view>
	</view>
</template>

<script>
	import uniNumberBox from '@/components/uni-number-box.vue'
	export default {
		data() {
			return {
				slist: [{
					id: 2,
					title: '平价菜场',
				}, {
					id: 3,
					title: '鲜肉蛋',
				}, {
					id:4,
					title: '新鲜水果',
				},
				{
					id:5,
					title: '活鱼河虾',
				}],
				currentIded: 32,
				sTitle: '',
				tlist:[],
				cartNum:0,
				cartNums:0,
				recommendType:2,
				enumsList:[],
				curentTabView:0,
				iste:true,
				banner:{
					imgUrl:''
				},
				isUse:false,
				isnumber:0,
				number:0,
				total:0,
				pageNo:1
				
			}
		},
		components: {
			uniNumberBox
		},
		onReachBottom() {
			// if(this.tlist.length >= this.total){
			// 	return
			// }
			this.getRecommendByStorage()
		},
		onLoad(option) {
			// this.$api.request('activity', 'activityGoodsList', {
			// 	activityType: 2,
			// 	storageId: this.$store.state.storageId
			// }, failres => {
			// 	this.logining = false
			// 	this.$api.msg(failres.errmsg)
			// }).then(res => {
			// 	// console.log(res )
			// 	// this.pageNo++
			// 	this.tlist = res.data
			// 	for (let i = 0; i < this.tlist.length; i++) {
			// 		this.tlist[i].isUse=0
			// 	}
			// 	// console.log(that.tlist)
			// })
			this.getRecommendByStorage('refresh')
			this.countCart()
			this.getUserCoupons()
			uni.setNavigationBarTitle({
			    title: option.title
			});
			this.$api.request('advertisement','getActiveAd',{
				adType:10
			}).then(res=>{
				this.banner = res.data[0]
			})
			
			
		},
		
		watch:{
			
		},
		methods: {
			//请求数据
		 loadData(){
				const that = this
				that.$api.request('cart', 'getCartList',{
					storageId:this.$store.state.storageId
				}).then(res => {
					//修改为for i循环，以找出库存不足的商品索引并去掉，存入库存不足商品组
					// var stockless = [];//库存不足商品索引
					var lessIds = []
					for (var i = 0; i < res.data.length; i++) {
						console.log(res.data[i].id)
						for (let j = 0; j < that.tlist.length; j++) {
							console.log(res.data[i].couponId,that.tlist[j].couponId)
							console.log(res.data[i].skuId===that.tlist[j].skuId)
							if(res.data[i].couponId===that.tlist[j].couponId&&res.data[i].skuId===that.tlist[j].skuId){
								that.tlist[j].isUse=1
								that.number++
								console.log(i)
							}
						}
					}
					// console.log('__________________________________')
					// console.log(stockless)
					// for (var i = 0; i < stockless.length; i++) {
					// 	that.stockLessList.push(res.data[stockless[i]])
					// 	res.data.splice(stockless[i],1)
					// }
					// stockless.forEach(item => {
					// 	that.stockLessList.push(res.data[item])
					// })
					that.cartList = res.data
					
					// that.calcTotal();  //计算总价
					// this.stockLessIds = lessIds.join(',')
				})
			},
			naviageToPage(page,title) {
				const that = this
				if(title){
					console.log(title,'jinlai')
					 title?title:'' 
					 uni.navigateTo({
					 	url: page
					 })
				}else{
					console.log(title)
					uni.navigateTo({
						url: page
					})
				}
				// console.log(that.categoryButtomList[index].title)
				
			},
			getRecommendTypeEnums(){
				this.$api.request('admin.recommend','getRecommendTypeEnums').then(res=>{
					console.log(res )
					this.enumsList=res.data
					
				}).catch(err=>{
					this.$api.msg('请求失败，请稍后再试')
				})
			},
			getRecommendByStorage(loadMore=''){
				
				let that=this
				console.log(this.recommendType,'this.recommendType')
				if (loadMore === 'refresh') {
					this.pageNo = 1
				}
				// this.$api.request('activity','activityGoodsList',{
				// 	activityType:2,
				// 	storageId: this.$store.state.storageId,
				// 	recommendType:this.recommendType,
				// 	pageNo:this.pageNo
				// }).then(res=>{
				// 	console.log(res )
				// 	this.pageNo++
				// 	let tempList = res.data.items
				// 	if (loadMore === 'refresh') {
				// 		this.tlist = [];
				// 	}
				// 	this.tlist = this.tlist.concat(tempList)
				// 	this.total = res.total
				// 	for (let i = 0; i < this.tlist.length; i++) {
				// 		this.tlist[i].num=0
				// 	}
				// }).catch(err=>{
				// 	this.$api.msg('请求失败，请稍后再试')
				// })
				that.$api.request('activity', 'activityGoodsList', {
					activityType: 2,
					storageId: this.$store.state.storageId,
					pageNo:this.pageNo
				}, failres => {
					that.logining = false
					that.$api.msg(failres.errmsg)
				}).then(res => {
					// console.log(res )
					
					that.pageNo++
					that.tlist = res.data
					for (let i = 0; i < that.tlist.length; i++) {
						that.tlist[i].isUse=0
						if(i==that.tlist.length-1){
							console.log('sss进来了')
							that.loadData()
						}
					}
					// console.log(that.tlist)
				})
			},
			
			getUserCoupons(){
				let that=this
				this.$api.request('coupon','getUserCoupons',{couponType:2},  failres => {
					this.logining = false
					this.$api.msg(failres.errmsg)
				}).then(res => {
					this.isnumber=res.data.length
					this.CouponsList=res.data
					console.log(this.CouponsList)
					// console.log('执行了',res.data)
				
				})
				// console.log('sssascsdf')
				
			},
			//统计购物车数量
			countCart(){
				this.$api.request('cart','countCart',{
					storageId:this.$store.state.storageId
				}).then(res=>{
					this.cartNum = res.data
				}).catch(err=>{
					this.$api.msg('请求失败，请稍后再试')
				})
				
				
			},
			navToDetail(id){
				uni.navigateTo({
					url:`../product/detail?id=${id}`
				})
			},
			navToCart(id){
				// console.log()
				uni.switchTab({
					url:`../cart/cart`
				})
			},
			cartNumFn(){ 
				let cartNum=0
				this.tlist.forEach((item,i)=>{
					cartNum+=item.num
				})
				this.cartNums=cartNum
			},
			
			
			addCart(index){
				const that = this
				console.log('判断抢购卷',this.number,this.isnumber)
				if(this.tlist[index].stockDto.sales==0){
					return
				}
				if(this.number==this.isnumber){
					that.$api.msg('抢购卷已用光')
					return
				}
				console.log(this.tlist[index].stockDto.sales)
				// this.tlist[index].num++
				this.cartNumFn()
				that.$api.request('cart', 'addCartItem', {
					skuId: that.tlist[index].skuId,
					activityId:that.tlist[index].activityId,
					couponId:that.tlist[index].couponId,
					num: 1,
					
				}).then(res => {
					that.$api.msg('添加购物车成功')
					var cartNum = this.$store.state.cartNum+1
					that.tlist[index].isUse=1
					let list=that.tlist
					
					that.tlist=[]
					that.tlist.push(...list)
					that.tlist[index].num++
					that.number++
					// console.log(that.tlist[index])
					that.countCart()
					that.$store.commit('addCart',cartNum)
					uni.setTabBarBadge({
						index:2,
						text:cartNum+''
					})
				})
			},
			//数量
			numberChange(data){
				const that = this
				// console.log(data)
				this.tlist[data.index].num=data.number
				this.cartNumFn()
				
				that.$api.request('cart', 'addCartItem', {
					skuId: this.tlist[data.index].skuId,
					activityId:this.tlist[data.index].activityId,
					couponId:this.tlist[data.index].couponId,
					num: 1
				}).then(res => {
					that.$api.msg('添加购物车成功')
					var cartNum = this.$store.state.cartNum+1
					that.$store.commit('addCart',cartNum)
					uni.setTabBarBadge({
						index:2,
						text:cartNum+''
					})
				})
				// this.addCart(data.index)
			},
			//二级分类点击
			sTabTap(item) {
				console.log(this.enumsList)
				for (let i = 0; i < this.enumsList.length; i++) {
					if(this.enumsList[i].msg==item.title){
						this.recommendType=this.enumsList[i].code
						console.log(this.recommendType,this.enumsList[i].code)
					}
				}
				this.curentTabView='id'+item.id
				this.currentIded = item.id
				this.sTitle = item.title //二级分类不用显示这个东西  
				
				this.getRecommendByStorage()
				// this.$api.request('goods', 'getGoodsPageByStorage',{
				// 	storageId :this.$store.state.storageId,
				// 	categoryId:item.id
				// }).then( res => {
				// 	uni.hideLoading()
				// 	this.tlist = res.data.items
				// 	this.count = res.data.totalPageNo
				// })
			},
		}
	}
</script>

<style lang='scss'>
	/* page,
	.content {
		background-color: #fff;
		
		
	} */
	.actived {
		font-size: 28rpx;
		color: #2AAC34;
		position: relative;
	}
	.badge{
		background-color: #FF473C;
		width: 32rpx;
		height: 32rpx;
		color: #fff;
		font-size: 22rpx;line-height: 32rpx;text-align: center;
		position: absolute;
		left: 42rpx;
		bottom: 32rpx;
	}
	.actived:after {
		display: block;
		content: '';
		width: 54rpx;
		height: 8rpx;
		background-color: #2AAC34;
		position: absolute;
		bottom: -10rpx;
		left: 50%;
		transform: translateX(-50%);
		border-radius: 2px;
	}
	.number-box{
		width: 73px;
	}
	.xie{
		position: absolute;
		top: 0;
		
		left: 0;
		width: 100%;
		height: 100%;
		/* background-color: red; */
		
	}
	.btn{
		border-width: 0px;
		   /* position: absolute;
		    left: 0px;
		    top: 0px; */
		    width: 80px;
		    height: 33px;
		    background: inherit;
		    background-color: rgba(22, 155, 213, 1);
		    border: none;
		    border-radius: 5px;
		    -moz-box-shadow: none;
		    -webkit-box-shadow: none;
		    box-shadow: none;
		    font-family: 'PingFangSC-Regular', 'PingFang SC';
		    font-weight: 400;
		    font-style: normal;
			line-height: 33px;
			text-align: center;
			color: #fff;
			font-size: 13px;
	}
	.hui{
		background-color: rgba(204, 204, 204, 1);
	}
</style>