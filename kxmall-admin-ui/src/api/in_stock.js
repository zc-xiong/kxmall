import request from '@/utils/request'
import Qs from 'qs'

export function inStockList(data) {
  /* 商品出库列表*/
  return request({
    method: 'get',
    params: {
      _gp: 'admin.goodsInStock',
      _mt: 'list',
      ...data
    }
  })
}

export function getGoodsList(data) {
  /* 获取商品*/
  return request({
    method: 'post',
    data: Qs.stringify({
      _gp: 'admin.stock',
      _mt: 'list',
      ...data
    })
  })
}

export function inStockCreate(data) {
  /* 商品出库 添加*/
  return request({
    method: 'post',
    data: Qs.stringify({
      _gp: 'admin.goodsInStock',
      _mt: 'create',
      goodsInStockDTO: JSON.stringify(data)
    })
  })
}

export function inStockDeleteAll(data) {
  /* 商品出库 删除*/
  return request({
    method: 'get',
    params: {
      _gp: 'admin.goodsInStock',
      _mt: 'delete',
      ...data
    }
  })
}

export function inStockSelectById(data) {
  /* 商品出库 获取详情信息*/
  return request({
    method: 'get',
    params: {
      _gp: 'admin.goodsInStock',
      _mt: 'selectById',
      ...data
    }
  })
}

export function inStockUpdate(data) {
  /* 商品出库 更新商品信息*/
  return request({
    method: 'post',
    data: Qs.stringify({
      _gp: 'admin.goodsInStock',
      _mt: 'update',
      goodsInStockDTO: JSON.stringify(data)
    })
  })
}

export function inStockUpdateInOfStock(data) {
  /* 商品出库  出库*/
  return request({
    method: 'get',
    params: {
      _gp: 'admin.goodsInStock',
      _mt: 'updateInOfStock',
      ...data
    }
  })
}

export function inStockStoragAllName(data) {
  /* 商品出库  获取所有仓库*/
  return request({
    method: 'get',
    params: {
      _gp: 'admin.goodsInStock',
      _mt: 'storagAllName',
      ...data
    }
  })
}
