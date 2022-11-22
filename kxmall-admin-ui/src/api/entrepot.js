import request from '@/utils/request'
import Qs from 'qs'
export function getEntrepotList(data) {
  return request({
    method: 'get',
    params: {
      _gp: 'admin.storage',
      _mt: 'list',
      ...data
    }
  })
}

export function getEntrepotMessage(id) {
  const data = {
    id
  }
  return request({
    method: 'get',
    params: {
      _gp: 'admin.storage',
      _mt: 'selectById',
      ...data
    }
  })
}

export function getOption() {
  return request({
    method: 'get',
    params: {
      _gp: 'admin.storage',
      _mt: 'options'
    }
  })
}

export function addEntrepot(data) {
  return request({
    method: 'get',
    params: {
      _gp: 'admin.storage',
      _mt: 'create',
      storage: data
    }
  })
}
export function updateEntrepot(data) {
  return request({
    method: 'get',
    params: {
      _gp: 'admin.storage',
      _mt: 'update',
      storage: data
    }
  })
}

export function deleteEntrepot(storageId) {
  const data = {
    storageId
  }
  return request({
    method: 'get',
    params: {
      _gp: 'admin.storage',
      _mt: 'delete',
      ...data
    }
  })
}

export function updateStateToNomral(ids) {
  return request({
    method: 'post',
    data: Qs.stringify({
      _gp: 'admin.storage',
      _mt: 'updateStateToNomral',
      ids
    })
  })
}

export function updateBusinessStateToOpen(ids) {
  return request({
    method: 'post',
    data: Qs.stringify({
      _gp: 'admin.storage',
      _mt: 'updateBusinessStateToOpen',
      ids
    })
  })
}

export function updateStateToAbort(ids) {
  return request({
    method: 'post',
    data: Qs.stringify({
      _gp: 'admin.storage',
      _mt: 'updateStateToAbort',
      ids
    })
  })
}

export function updateBusinessStateToRest(ids) {
  return request({
    method: 'post',
    data: Qs.stringify({
      _gp: 'admin.storage',
      _mt: 'updateBusinessStateToRest',
      ids
    })
  })
}

export function getGoodsList(data) {
  return request({
    method: 'get',
    params: {
      _gp: 'admin.stock',
      _mt: 'list',
      ...data
    }
  })
}

export function soldOut(data) {
  return request({
    method: 'get',
    params: {
      _gp: 'admin.stock',
      _mt: 'freezeOrActivation',
      ...data
    }
  })
}

export function updatePrice(data) {
  return request({
    method: 'get',
    params: {
      _gp: 'admin.stock',
      _mt: 'updatePrice',
      ...data
    }
  })
}

export function getProvinceAll() {
  return request({
    method: 'get',
    params: {
      _gp: 'region',
      _mt: 'getProvinceAll'
    }
  })
}

export function getCity(provinceId) {
  const data = {
    provinceId
  }
  return request({
    method: 'get',
    params: {
      _gp: 'region',
      _mt: 'getCity',
      ...data
    }
  })
}

export function getCounty(cityId) {
  const data = {
    cityId
  }
  return request({
    method: 'get',
    params: {
      _gp: 'region',
      _mt: 'getCounty',
      ...data
    }
  })
}

export function getCityAll() {
  return request({
    method: 'get',
    params: {
      _gp: 'region',
      _mt: 'getCityAll'
    }
  })
}

export function getCountyAll() {
  return request({
    method: 'get',
    params: {
      _gp: 'region',
      _mt: 'getCountyAll'
    }
  })
}
