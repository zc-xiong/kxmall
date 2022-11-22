<template>
  <div class="main">
    <el-select v-model="entrepot" placeholder="请选择前置仓" clearable>
      <el-option
        v-for="item in entrepotOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"/>
    </el-select>
    <el-cascader
      :props="{ checkStrictly: true }"
      v-model="category"
      :options="categoryOptions"
      clearable
      filterable
      placeholder="请选择类目"/>
    <el-input
      v-model="name"
      style="width:300px"
      clearable
      placeholder="请输入商品条码/商品ID/商品名称"/>
    <el-select v-model="value" placeholder="请选择销售状态" clearable filterable>
      <el-option :value="1" label="在售"/>
      <el-option :value="0" label="下架"/>
    </el-select>
    <el-button type="primary" icon="el-icon-search" @click="doSearch">查询</el-button>
    <el-button type="primary" @click="toAdd">导入商品</el-button>
    <el-table
      :data="tableData"
      border
      style="width: 100%; margin-top:20px">
      <el-table-column
        type="selection"
        align="center"
        width="55"/>
      <el-table-column
        prop="storageDO.name"
        align="center"
        width="120"
        label="前置仓"/>
      <el-table-column
        align="center"
        width="200"
        label="商品类目">
        <template slot-scope="scope">
          <span>{{ scope.row.categoryDOs | getType }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        prop="skuDTO.barCode"
        label="商品条码"/>
      <el-table-column
        prop="spuDO.id"
        align="center"
        label="商品ID"/>
      <el-table-column
        prop="spuDO.title"
        align="center"
        show-overflow-tooltip
        label="商品名称"/>
      <el-table-column
        prop="skuDTO.title"
        align="center"
        show-overflow-tooltip
        label="商品规格"/>
      <el-table-column
        align="center"
        label="销售状态">
        <template slot-scope="scope">
          <span>{{ scope.row.status | formatStatus }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="stock"
        align="center"
        label="可售量"/>
      <el-table-column
        prop="skuDTO.unit"
        align="center"
        label="单位"/>
      <el-table-column
        align="center"
        label="原价">
        <template slot-scope="scope">
          <span type="text">{{ scope.row.spuDO | formatPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="当前售价">
        <template slot-scope="scope">
          <el-button v-if="editPrice[scope.$index]" type="text" @click="priceEdit(scope.$index)">{{ scope.row.price | formatMoney }}</el-button>
          <el-input v-else v-model="priceList[scope.$index]" clearable @blur="changePrice(scope.$index,scope.row.id)" />
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="折扣">
        <template slot-scope="scope">
          <el-button v-if="editDiscount[scope.$index]" type="text">{{ scope.row.skuDTO | formatDiscount }}</el-button>
          <el-input v-else clearable />
        </template>
      </el-table-column>
      <el-table-column
        width="150"
        align="center"
        label="导入时间">
        <template slot-scope="scope">
          <span>{{ scope.row.gmtCreate | formatDateAndTime }}</span>
        </template>
      </el-table-column>
      <el-table-column
        width="150"
        align="center"
        label="修改时间">
        <template slot-scope="scope">
          <span>{{ scope.row.gmtUpdate | formatDateAndTime }}</span>
        </template>
      </el-table-column>
      <el-table-column
        fixed="right"
        align="center"
        label="操作"
        width="150">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status" type="primary" size="small" @click="putaway(scope.row.id,1)">下架</el-button>
          <el-button v-else type="primary" size="small" @click="putaway(scope.row.id,0)">上架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="page">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"/>
    </div>
  </div>
</template>

<script>
import { getGoodsList, soldOut, updatePrice, getEntrepotList } from '@/api/entrepot'
import { categorySecondLevelTree } from '@/api/category'
export default {
  name: 'Control',
  filters: {
    getType(val) {
      if (!val) {
        return ''
      }
      const arr = val.map(item => item.title)
      const str = arr.join('>')
      return str
    },
    formatStatus(val) {
      if (val) {
        return '在售'
      } else {
        return '下架'
      }
    },
    formatPrice(val) {
      if (!val) {
        return ''
      }
      if (!val.originalPrice) {
        return ''
      }
      return val.originalPrice / 100
    },
    formatMoney(val) {
      if (!val) {
        return ''
      }
      return val / 100
    },
    formatDiscount(val) {
      if (!val) {
        return ''
      }
      return val.discount
    }
  },
  data() {
    return {
      name: '',
      pageSize: 10,
      total: 0,
      priceList: [],
      editPrice: [],
      editDiscount: [],
      currentPage: 1,
      input: '',
      options: [],
      entrepotOptions: [],
      value: '',
      tableData: [],
      entrepot: '',
      categoryOptions: [],
      category: []
    }
  },
  created() {
    getEntrepotList().then(res => {
      res.data.data.items.forEach(item => {
        this.entrepotOptions.push({
          label: item.name,
          value: item.id
        })
      })
    })
    categorySecondLevelTree().then(res => {
      console.log(res)
      this.categoryOptions = res.data.data
      this.categoryOptions.forEach(item => {
        item.children.forEach(val => {
          val.children = undefined
        })
      })
    })
    this.getList(
      { page: 1, limit: this.pageSize }
    )
    // const res = await getGoodsList()
    // console.log(res)
    // this.tableData = res.data.data.items
    // this.priceList = this.tableData.map(val => {
    //   if (!val.skuDTO) {
    //     return ''
    //   }
    //   return val.skuDTO.price / 100
    // })
    // this.editDiscount = this.tableData.map(() => true)
    // this.editPrice = this.tableData.map(() => true)
  },
  methods: {
    async getList(obj) {
      const res = await getGoodsList(obj)
      console.log(res)
      this.total = res.data.data.total
      this.tableData = res.data.data.items
      this.priceList = this.tableData.map(val => {
        if (!val.price) {
          return ''
        }
        return val.price / 100
      })
      this.editDiscount = this.tableData.map(() => true)
      this.editPrice = this.tableData.map(() => true)
    },
    toAdd() {
      this.$router.push('/entrepot/import')
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getList({
        storageId: this.entrepot,
        categoryId: this.category[1],
        name: this.name,
        status: this.value,
        page: this.currentPage,
        limit: val
      })
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList({
        storageId: this.entrepot,
        categoryId: this.category[1],
        name: this.name,
        status: this.value,
        page: val,
        limit: this.pageSize
      })
    },
    async putaway(val, type) {
      if (!type) {
        this.$confirm('是否将此商品上架', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async() => {
          await soldOut({ stockId: val, status: 1 })
          this.getList({
            storageId: this.entrepot,
            categoryId: this.category[1],
            name: this.name,
            status: this.value,
            page: this.currentPage,
            limit: this.pageSize
          })
        })
          .catch(() => {
            return false
          })
      } else {
        this.$confirm('是否将此商品下架', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async() => {
          await soldOut({ stockId: val, status: 0 })
          this.getList({
            storageId: this.entrepot,
            categoryId: this.category[1],
            name: this.name,
            status: this.value,
            page: this.currentPage,
            limit: this.pageSize
          })
        })
          .catch(() => {
            return false
          })
      }
    },
    async changePrice(index, id) {
      const res = await updatePrice({ price: this.priceList[index] * 100, stockId: id })
      this.$set(this.editPrice, index, true)
      this.$set(this.tableData[index], 'price', this.priceList[index] * 100)
      this.$set(this.tableData[index].skuDTO, 'discount', this.priceList[index] * 1000 / this.tableData[index].spuDO.originalPrice)
      console.log(res)
    },
    priceEdit(index) {
      this.$set(this.editPrice, index, false)
    },
    doSearch() {
      this.getList({
        storageId: this.entrepot,
        categoryId: this.category[1],
        name: this.name,
        status: this.value,
        page: this.currentPage,
        limit: this.pageSize
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
</style>

