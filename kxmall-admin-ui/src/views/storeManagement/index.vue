<template>
  <div class="main">
    <el-input v-model="name" placeholder="请输入仓库名称或id" style="width:200px" clearable/>
    <el-select v-model="status" placeholder="仓库状态" clearable filterable>
      <el-option :value="0" label="禁用"/>
      <el-option :value="1" label="正常"/>
    </el-select>
    <el-select v-model="openStatus" placeholder="营业状态" clearable filterable>
      <el-option :value="0" label="休息中"/>
      <el-option :value="1" label="营业中"/>
    </el-select>
    <el-select v-model="province" placeholder="请选择省" clearable filterable @change="getCityMethod">
      <el-option
        v-for="item in provinceList"
        :key="item.value"
        :label="item.label"
        :value="item.value"/>
    </el-select>
    <el-select v-model="city" placeholder="请选择市" clearable filterable @change="getCountyMethod">
      <el-option
        v-for="item in cityList"
        :key="item.value"
        :label="item.label"
        :value="item.value"/>
    </el-select>
    <el-select v-model="county" placeholder="请选择区" clearable filterable>
      <el-option
        v-for="item in countyList"
        :key="item.value"
        :label="item.label"
        :value="item.value"/>
    </el-select>
    <!--<el-cascader
      :props="propsList"
      v-model="addressList"
      placeholder="请选择省市区"
      clearable
    ></el-cascader>-->
    <el-button v-permission="['admin:storage:list']" type="primary" @click="doSearch">查找</el-button>
    <el-button v-permission="['admin:storage:create']" type="primary" @click="toAdd">新增</el-button>
    <el-table
      :data="tableData"
      border
      style="width: 100%; margin-top:20px">
      <el-table-column
        type="selection"
        align="center"
        width="55"/>
      <el-table-column
        prop="id"
        align="center"
        label="仓库id"/>
      <el-table-column
        align="center"
        show-overflow-tooltip
        prop="name"
        label="仓库名称"/>
      <el-table-column
        align="center"
        label="仓库状态">
        <template slot-scope="scope">
          <span>{{ scope.row.state | formatState }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="营业状态">
        <template slot-scope="scope">
          <span>{{ scope.row.operatingState | formatOperatingState }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="leaderName"
        align="center"
        label="负责人"/>
      <el-table-column
        prop="phone"
        align="center"
        label="联系电话"/>
      <el-table-column
        align="center"
        width="150"
        show-overflow-tooltip
        label="地址">
        <template slot-scope="scope">
          <span>{{ scope.row.province | formatProvince }}{{ scope.row.city | formatCity }}{{ scope.row.county | formatCounty }}{{ scope.row.address }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop=""
        align="center"
        label="营业时间">
        <template slot-scope="scope">
          <span>{{ scope.row.businessStartTime }}~{{ scope.row.businessStopTime }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop=""
        align="center"
        label="配送时间">
        <template slot-scope="scope">
          <span>{{ scope.row.deliveryStartTime }}~{{ scope.row.deliveryStopTime }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        width="150"
        label="创建时间">
        <template slot-scope="scope">
          <span>{{ scope.row.gmtCreate | formatDateAndTime }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        width="150"
        label="修改时间">
        <template slot-scope="scope">
          <span>{{ scope.row.gmtUpdate | formatDateAndTime }}</span>
        </template>
      </el-table-column>
      <el-table-column
        fixed="right"
        align="center"
        label="操作"
        width="300">
        <template slot-scope="scope">
          <el-button type="primary" size="small" @click="showDetail(scope.row.id)">详情</el-button>
          <el-button v-if="scope.row.state" type="primary" size="small" @click="edit(scope.row.id)">修改</el-button>
          <el-button v-if="scope.row.state" type="danger" size="small" @click="toAbort(scope.row.id)">禁用</el-button>
          <el-button v-else type="success" size="small" @click="toNomral(scope.row.id)">启用</el-button>
          <el-button v-if="scope.row.operatingState && scope.row.state" type="warning" size="small" @click="toRest(scope.row.id)">休息</el-button>
          <el-button v-if="(!scope.row.operatingState) && scope.row.state" type="success" size="small" @click="toOpen(scope.row.id)">营业</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      :visible.sync="detailBar"
      title="仓库详情"
      width="30%">
      <div>
        <el-form ref="form" :model="detailData" label-width="120px">
          <el-form-item label="前置仓名称">
            <span>{{ detailData.name }}</span>
          </el-form-item>
          <el-form-item label="前置仓状态">
            <span>{{ detailData.state?'正常':'禁用' }}</span>
          </el-form-item>
          <el-form-item label="营业状态">
            <span>{{ detailData.operatingState?'营业中':'休息中' }}</span>
          </el-form-item>
          <el-form-item label="营业时间">
            <span>{{ detailData.businessStartTime }}-{{ detailData.businessStopTime }}</span>
          </el-form-item>
          <el-form-item label="配送时间">
            <span>{{ detailData.deliveryStartTime }}-{{ detailData.deliveryStopTime }}</span>
          </el-form-item>
          <el-form-item label="负责人">
            <span>{{ detailData.leaderName }}</span>
          </el-form-item>
          <el-form-item label="联系电话">
            <span>{{ detailData.phone }}</span>
          </el-form-item>
          <el-form-item label="配送范围">
            <span>{{ detailData.deliveryRadius }}公里</span>
          </el-form-item>
          <el-form-item label="地址">
            <span>{{ detailData.province | formatProvince }}{{ detailData.city | formatCity }}{{ detailData.county | formatCounty }}{{ detailData.address }}</span>
          </el-form-item>
          <el-form-item label="经纬度">
            <span>{{ detailData.longitude }},{{ detailData.latitude }}</span>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
    <div class="page">
      <!-- <div class="leftbar">
        <el-checkbox v-model="checked">全选</el-checkbox>
        <el-dropdown trigger="click">
          <el-button type="primary" size="mini">
            批量操作<i class="el-icon-arrow-down el-icon--right"/>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item>
              <el-button type="text" style="padding:0">启用</el-button>/
              <el-button type="text" style="padding:0">禁用</el-button>
            </el-dropdown-item>
            <el-dropdown-item>休息/营业</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div> -->
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="totalPage"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"/>
    </div>
  </div>
</template>

<script>
import { getEntrepotList, updateStateToNomral, updateBusinessStateToOpen, updateBusinessStateToRest, updateStateToAbort, getEntrepotMessage } from '@/api/entrepot'
import { getProvinceAll, getCity, getCounty, getCityAll, getCountyAll } from '@/api/entrepot'
export default {
  name: 'Management',
  filters: {
    formatState(val) {
      if (val) {
        return '正常'
      } else {
        return '禁用'
      }
    },
    formatOperatingState(val) {
      if (val) {
        return '营业中'
      } else {
        return '休息中'
      }
    },
    formatProvince(val) {
      if (!val) {
        return ''
      }
      return JSON.parse(sessionStorage.getItem('provinceList')).filter(item => item.value === val)[0].label
    },
    formatCity(val) {
      if (!val) {
        return ''
      }
      return JSON.parse(sessionStorage.getItem('cityAll')).filter(item => item.value === val)[0].label
    },
    formatCounty(val) {
      if (!val) {
        return ''
      }
      return JSON.parse(sessionStorage.getItem('countyAll')).filter(item => item.value === val)[0].label
    }
  },
  data() {
    return {
      addressList: [],
      // propsList: {
      //   lazy: true,
      //   lazyLoad(node, resolve) {
      //     console.log(node)
      //     setTimeout(async() => {
      //       if (node.level === 0) {
      //         const nodes = (await getProvinceAll()).data.data
      //         resolve(nodes)
      //       } else if (node.level === 1) {
      //         const nodes = (await getCity(node.value)).data.data
      //         resolve(nodes)
      //       } else {
      //         const nodes = (await getCounty(node.value)).data.data
      //         nodes.forEach(val => {
      //           val.leaf = true
      //         })
      //         resolve(nodes)
      //       }
      //     }, 300)
      //   }
      // },
      detailData: {},
      detailBar: false,
      currentPage: 1,
      pageSize: 20,
      totalPage: 1,
      name: '',
      openStatus: '',
      status: '',
      tableData: [],
      provinceList: [],
      cityList: [],
      countyList: [],
      province: '',
      city: '',
      county: '',
      checked: false
    }
  },
  async created() {
    this.provinceList = (await getProvinceAll()).data.data
    sessionStorage.setItem('provinceList', JSON.stringify(this.provinceList))
    sessionStorage.setItem('cityAll', JSON.stringify((await getCityAll()).data.data))
    sessionStorage.setItem('countyAll', JSON.stringify((await getCountyAll()).data.data))
    this.changePage({ limit: this.pageSize, page: this.currentPage })
  },
  methods: {
    changePage(obj) {
      getEntrepotList(obj).then(res => {
        this.tableData = res.data.data.items
        this.totalPage = res.data.data.total
      })
    },
    toAdd() {
      this.$router.push('/entrepot/addWare')
    },
    edit(val) {
      this.$router.push({
        path: '/entrepot/editWare',
        query: {
          id: val
        }
      })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.changePage({
        limit: val,
        page: this.currentPage,
        state: this.status,
        operatingState: this.openStatus,
        name: this.name,
        province: this.addressList[0],
        city: this.city[1],
        county: this.county[2]
      })
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.changePage({
        limit: this.pageSize,
        page: val,
        state: this.status,
        operatingState: this.openStatus,
        name: this.name,
        province: this.addressList[0],
        city: this.city[1],
        county: this.county[2]
      })
    },
    async toNomral(id) {
      this.$confirm('是否启用此仓库', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const arr = []
        arr.push(id)
        await updateStateToNomral(JSON.stringify(arr))
        this.changePage({ limit: this.pageSize, page: this.currentPage })
      })
    },
    async toOpen(id) {
      this.$confirm('是否开启此仓库的营业', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const arr = []
        arr.push(id)
        await updateBusinessStateToOpen(JSON.stringify(arr))
        this.changePage({ limit: this.pageSize, page: this.currentPage })
      })
    },
    async toAbort(id) {
      this.$confirm('是否禁用此仓库', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const arr = []
        arr.push(id)
        await updateStateToAbort(JSON.stringify(arr))
        this.changePage({ limit: this.pageSize, page: this.currentPage })
      })
    },
    async toRest(id) {
      this.$confirm('是否关闭此仓库的营业', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const arr = []
        arr.push(id)
        await updateBusinessStateToRest(JSON.stringify(arr))
        this.changePage({ limit: this.pageSize, page: this.currentPage })
      })
    },
    async getCityMethod(val) {
      this.city = ''
      this.county = ''
      this.cityList = []
      this.countyList = []
      if (val) {
        this.cityList = (await getCity(val)).data.data
      }
    },
    async getCountyMethod(val) {
      this.county = ''
      this.countyList = []
      if (val) {
        this.countyList = (await getCounty(val)).data.data
      }
    },
    doSearch() {
      this.changePage({
        state: this.status,
        operatingState: this.openStatus,
        name: this.name,
        province: this.province,
        city: this.city,
        county: this.county,
        limit: this.pageSize,
        page: this.currentPage
      })
    },
    showDetail(val) {
      this.detailBar = true
      getEntrepotMessage(val).then(res => {
        console.log(res)
        this.detailData = res.data.data
      })
    }
  }
}
</script>
<style>
.main {
  padding: 20px;
}

.page {
  margin-top: 10px;
}

.leftbar {
  float: left;
  margin-right: 30px;
}
</style>

