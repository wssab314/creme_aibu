// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2021 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

import request from "@/utils/request.js";
/**
 * 公共接口 ，优惠券接口 , 行业此讯 , 手机号码注册
 * 
*/

/**
 * 获取主页数据 无需授权
 * 
*/
export function getIndexData()
{
  return request.get("index",{},{ noAuth : true});
}

/**
 * 获取登录授权login
 * 
*/
export function getLogo()
{
  return request.get('wechat/getLogo', {}, { noAuth : true});
}


/**
 * 保存form_id
 * @param string formId 
 */
export function setFormId(formId) {
  return request.post("wechat/set_form_id", { formId: formId});
}

/**
 * 领取优惠卷
 * @param int couponId
 * 
*/
export function setCouponReceive(couponId){
  return request.post('coupon/receive', { couponId: couponId});
}
/**
 * 优惠券列表
 * @param object data
*/
export function getCoupons(data){
  return request.get('coupons',data,{noAuth:true})
}

/**
 * 我的优惠券
 * @param int types 0全部  1未使用 2已使用
*/
export function getUserCoupons(data){
  return request.get('coupon/list',data)
}

/**
 * 文章分类列表
 * 
*/
export function getArticleCategoryList(){
  return request.get('article/category/list',{},{noAuth:true})
}

/**
 * 文章列表
 * @param int cid
 * 
*/
export function getArticleList(cid,data){
  return request.get('article/list/' + cid, data,{noAuth:true})
}

/**
 * 文章 热门列表
 * 
*/
export function getArticleHotList(){
  return request.get('article/hot/list',{},{noAuth:true});
}

/**
 * 文章 轮播列表
 * 
*/
export function getArticleBannerList(){
  return request.get('article/banner/list',{},{noAuth:true})
}

/**
 * 文章详情
 * @param int id 
 * 
*/
export function getArticleDetails(id){
  return request.get('article/info',id,{noAuth:true});
}

/**
 * 手机号+验证码登录接口
 * @param object data
*/
export function loginMobile(data){
  return request.post('login/mobile',data,{noAuth:true})
}

/**
 * 获取短信KEY
 * @param object phone
*/
export function verifyCode(){
  return request.get('verify_code', {},{noAuth:true})
}

/**
 * 验证码发送
 * @param object phone
*/
export function registerVerify(phone){
  return request.post('sendCode', { phone: phone },{noAuth:true},1)
}

/**
 * 手机号注册
 * @param object data
 * 
*/
export function phoneRegister(data){
  return request.post('register',data,{noAuth:true});
}

/**
 * 手机号修改密码
 * @param object data
 * 
*/
export function phoneRegisterReset(data){
  return request.post('register/reset',data,{noAuth:true})
}

/**
 * 手机号+密码登录
 * @param object data
 * 
*/
export function phoneLogin(data){
  return request.post('login',data,{noAuth:true})
}

/**
 * 切换H5登录
 * @param object data
*/
// #ifdef MP
export function switchH5Login(){
  return request.post('switch_h5', { 'from':'routine'});
}
// #endif

/*
 * h5切换公众号登录
 * */
// #ifdef H5
export function switchH5Login() {
  return request.post("switch_h5", { 'from': "wechat" });
}
// #endif

/**
 * 换绑手机号
 * 
*/
export function bindingPhone(data){
  return request.post('update/binding',data);
}

/**
 * 换绑手机号校验
 * 
*/
export function bindingVerify(data){
  return request.post('update/binding/verify',data);
}

/**
 * 退出登錄
 * 
*/
export function logout(){
  return request.get('logout');
}

/**
 * 获取订阅消息id
 */
export function getTemlIds(data)
{
  return request.get('wechat/program/my/temp/list', data , { noAuth:true});
}

/**
 * 首页拼团数据
 */
export function pink()
{
  return request.get('pink', {}, { noAuth:true});
}

/**
 * 获取城市信息
 */
export function getCity() {
  return request.get('city/list', { }, { noAuth: true });
}

/**
 * 获取小程序直播列表
 */
export function getLiveList(page,limit) {
  return request.get('wechat/live', { page, limit}, { noAuth: true });
}

/**
 * 获取小程序二维码
 */
export function getQrcode(data) {
  return request.post('qrcode/get',data,{ noAuth: true });
}

/**
 * 获取主题换色配置
 */
export function getTheme() {
  return request.get('index/color/config',{},{noAuth:true});
}
 
/**
 * 获取主题换色配置
 */
export function getAppVersion() {
  return request.get('index/get/version',{},{noAuth:true});
}

/**
 * 获取全局本地图片域名
 */
export function getImageDomain() {
  return request.get('image/domain',{},{noAuth:true});
}

/**
 * 商品排行榜
*/
export function productRank(){
  return request.get('product/leaderboard',{},{noAuth:true});
}

/**
 * 校验token是否有效
*/
export function tokenIsExistApi(){
  return request.post(`token/is/exist`,{},{noAuth:true});
}

/**
 * 获取登录配置
*/
export function loginConfigApi(){
  return request.get(`login/config`,{},{noAuth:true});
}

/**
 * 获取底部导航信息
*/
export function getBottomNavigationApi(){
  return request.get(`get/bottom/navigation`,{},{noAuth:true});
}
/**
 * 协议详情
*/
export function agreementInfo(info){
  return request.get(`agreement/${info}`,{},{noAuth:true});
}
/**
 * 首页装修
*/
export function pagediyInfoApi(id){
  return request.get(`pagediy/info/${id}`,{},{noAuth:true});
}
/**
 * 首页 第二级商品分类
 *
*/
export function getCategoryTwo(id)
{
  return request.get(`categorybypid/${id}`,{},{ noAuth : true});
}
/**
 * 获取备案设置
 *
*/
export function getConfigCopyright(id)
{
  return request.get(`config/get/copyright`,{},{ noAuth : true},{},true);
}