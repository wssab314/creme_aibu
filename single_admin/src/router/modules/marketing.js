// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

/** When your routing table is too long, you can split it into small modules **/

import Layout from '@/layout';

const marketingRouter = {
  path: '/marketing',
  component: Layout,
  redirect: '/coupon/list',
  name: 'Marketing',
  meta: {
    title: '营销',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'coupon',
      component: () => import('@/views/marketing/coupon/index'),
      name: 'Coupon',
      meta: { title: '优惠券', icon: '' },
      children: [
        {
          path: 'template',
          component: () => import('@/views/marketing/coupon/couponTemplate/index'),
          name: 'couponTemplate',
          hidden: true,
          meta: { title: '优惠券模板', icon: '' },
        },
        {
          path: 'list/save/:id?',
          name: 'couponAdd',
          meta: {
            title: '优惠劵添加',
            noCache: true,
            activeMenu: `/marketing/coupon/list`,
          },
          hidden: true,
          component: () => import('@/views/marketing/coupon/list/creatCoupon'),
        },
        {
          path: 'list',
          component: () => import('@/views/marketing/coupon/list/index'),
          name: 'List',
          meta: { title: '优惠券列表', icon: '' },
        },
        {
          path: 'record',
          component: () => import('@/views/marketing/coupon/record/index'),
          name: 'Record',
          meta: { title: '领取记录', icon: '' },
        },
      ],
    },
    {
      path: 'bargain',
      component: () => import('@/views/marketing/bargain/index'),
      name: 'Bargain',
      meta: { title: '砍价管理', icon: '' },
      alwaysShow: true,
      hidden: true,
      children: [
        {
          path: 'bargainGoods',
          component: () => import('@/views/marketing/bargain/bargainGoods/index'),
          name: 'bargainGoods',
          meta: { title: '砍价商品', icon: '' },
        },
        {
          path: 'creatBargain/:id?',
          component: () => import('@/views/marketing/bargain/bargainGoods/creatBargain'),
          name: 'creatBargain',
          meta: { title: '砍价商品', icon: '', noCache: true, activeMenu: `/marketing/bargain/bargainGoods` },
        },
        {
          path: 'bargainList',
          component: () => import('@/views/marketing/bargain/bargainList/index'),
          name: 'bargainList',
          meta: { title: '砍价列表', icon: '' },
        },
      ],
    },
    {
      path: 'groupBuy',
      component: () => import('@/views/marketing/groupBuy/index'),
      name: 'groupBuy',
      meta: { title: '拼团管理', icon: '' },
      hidden: true,
      children: [
        {
          path: 'groupGoods',
          component: () => import('@/views/marketing/groupBuy/groupGoods/index'),
          name: 'groupGoods',
          meta: { title: '拼团商品', icon: '' },
        },
        {
          path: 'creatGroup/:id?/:type?',
          component: () => import('@/views/marketing/groupBuy/groupGoods/creatGroup'),
          name: 'creatGroup',
          meta: { title: '拼团商品', icon: '', noCache: true, activeMenu: `/marketing/groupBuy/groupGoods` },
        },
        {
          path: 'groupList',
          component: () => import('@/views/marketing/groupBuy/groupList/index'),
          name: 'groupList',
          meta: { title: '拼团列表', icon: '' },
        },
      ],
    },
    {
      path: 'seckill',
      component: () => import('@/views/marketing/seckill/index'),
      name: 'Seckill',
      meta: { title: '秒杀管理', icon: '' },
      children: [
        {
          path: 'config',
          component: () => import('@/views/marketing/seckill/seckillConfig/index'),
          name: 'SeckillConfig',
          meta: { title: '秒杀配置', icon: '' },
        },
        {
          path: 'list/:timeId?',
          component: () => import('@/views/marketing/seckill/seckillList/index'),
          name: 'SeckillList',
          meta: { title: '秒杀商品', icon: '', noCache: true, activeMenu: `/marketing/seckill/list` },
        },
        {
          path: 'creatSeckill/:name?/:timeId?/:id?',
          component: () => import('@/views/marketing/seckill/seckillList/creatSeckill'),
          name: 'CreatSeckill',
          meta: { title: '添加秒杀商品', icon: '', noCache: true, activeMenu: `/marketing/seckill/list` },
        },
      ],
    },
    {
      path: 'integral',
      component: () => import('@/views/marketing/integral/index'),
      name: 'Integral',
      meta: { title: '积分', icon: '' },
      children: [
        {
          path: 'integralconfig',
          component: () => import('@/views/marketing/integral/config/index'),
          name: 'integralConfig',
          meta: { title: '积分配置', icon: '' },
        },
        {
          path: 'integrallog',
          component: () => import('@/views/marketing/integral/integralLog/index'),
          name: 'integralLog',
          meta: { title: '积分日志', icon: '' },
        },
      ],
    },
    {
      path: 'videoChannel',
      component: () => import('@/views/marketing/integral/index'),
      name: 'videoChannel',
      meta: { title: '视频号管理', icon: '' },
      children: [
        {
          path: 'list',
          component: () => import('@/views/marketing/videoChannel/videoList/index'),
          name: 'VideoChannelList',
          meta: { title: '商品列表', icon: '', noCache: true },
        },
        {
          path: 'creatVideoChannel/:id?',
          component: () => import('@/views/marketing/videoChannel/draftList/creatVideoChannel'),
          name: 'CreatVideoChannel',
          meta: { title: '添加视频号商品', icon: '', noCache: true, activeMenu: `/marketing/videoChannel/draftList` },
        },
        {
          path: 'draftList',
          component: () => import('@/views/marketing/videoChannel/draftList/index'),
          name: 'draftList',
          meta: { title: '草稿列表', icon: '', noCache: true },
        },
      ],
    },
    {
      path: 'atmosphere',
      name: 'atmosphere',
      meta: {
        title: '活动氛围',
        noCache: true,
      },
      component: () => import('@/views/marketing/atmosphere/index'),
      children: [
        {
          path: 'list',
          name: `atmosphereList`,
          meta: {
            title: '氛围列表',
            noCache: true,
          },
          component: () => import('@/views/marketing/atmosphere/atmosphereList/list'),
        },
        {
          path: 'add/:id?',
          name: `addAtmosphere`,
          meta: {
            title: '添加活动氛围',
            noCache: true,
            activeMenu: `/marketing/atmosphere/list`,
          },
          component: () => import('@/views/marketing/atmosphere/atmosphereList/addAtmosphere'),
        },
      ],
    },
    {
      path: 'border',
      name: 'border',
      meta: { title: '活动边框', icon: '' },
      component: () => import('@/views/marketing/border/index'),
      children: [
        {
          path: 'list',
          name: `borderList`,
          meta: {
            title: '活动边框列表',
            noCache: true,
          },
          component: () => import('@/views/marketing/atmosphere/atmosphereList/list'),
        },
        {
          path: 'add/:id?',
          name: `addBorder`,
          meta: {
            title: '添加活动边框',
            noCache: true,
            activeMenu: `/marketing/border/list`,
          },
          component: () => import('@/views/marketing/atmosphere/atmosphereList/addAtmosphere'),
        },
      ],
    },
  ],
};

export default marketingRouter;
