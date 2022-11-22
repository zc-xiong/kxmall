<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input v-model="listQuery.title" clearable class="filter-item" style="width: 200px;" placeholder="输入商品名称" />
      <el-input v-model="listQuery.barcode" clearable class="filter-item" style="width: 200px;" placeholder="输入商品条形码" />
      <el-select v-model="listQuery.status" style="width: 200px" clearable filterable class="filter-item" placeholder="选择商品状态" >
        <el-option v-for="(item,index) in goodsStatusMap" :key="index" :label="item.name" :value="item.value" />
      </el-select>
      <el-cascader :options="categoryOptions" :props="{ checkStrictly: true }" placeholder="请选择类目" clearable class="filter-item" filterable style="width: 200px;" @change="handleCategoryOption" />
      <el-button v-permission="['operation:goods:list']" class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">查找</el-button>
      <el-button v-permission="['operation:goods:create']" class="filter-item" type="primary" icon="el-icon-edit" @click="handleCreate">添加</el-button>
      <el-button v-permission="['operation:goods:batchdelete']" :disabled="selectedIds.length === 0" class="filter-item" type="danger" icon="el-icon-delete" @click="handleBatchDelete">批量删除</el-button>
      <el-button class="filter-item" type="primary" @click="importData">导出</el-button>
    </div>
    <a
      v-show="false"
      ref="download"
      href="http://114.67.94.213:8080/stock/export" />
    <!-- 导出弹窗 -->
    <el-dialog
      :visible.sync="importShow"
      title="请选择要导出的内容"
      width="30%">
      <el-checkbox-group v-model="importList">
        <el-checkbox v-for="(item,index) in importGroup" :key="index" :label="item.label">{{ item.value }}</el-checkbox>
      </el-checkbox-group>
      <span slot="footer" class="dialog-footer">
        <el-button @click="importShow = false">取 消</el-button>
        <el-button type="primary" @click="doImport">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 查询结果 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      size="small"
      element-loading-text="正在查询中。。。"
      border
      fit
      highlight-current-row
      @selection-change="handleSelectionChange"
    >

      <el-table-column align="center" type="selection" width="55" />

      <el-table-column type="expand" label="sku信息">
        <template slot-scope="props">
          <el-table
            v-loading="listLoading"
            :data="props.row.skuList"
            size="small"
            border
          >
            <el-table-column align="center" label="商品条码" prop="barCode" />
            <el-table-column align="center" label="商品规格" prop="title" />
<!--            <el-table-column align="center" label="商品库存" prop="stock" />-->
<!--            <el-table-column align="center" label="商品冻结库存" prop="freezeStock" />-->
            <el-table-column align="center" label="原价" prop="originalPrice" >
              <template slot-scope="scope">{{ scope.row.originalPrice /100 }}</template>
            </el-table-column>
<!--            <el-table-column align="center" label="现价" prop="price" >-->
<!--              <template slot-scope="scope">{{ scope.row.price /100 }}</template>-->
<!--            </el-table-column>-->
<!--            <el-table-column align="center" label="VIP价格" prop="vipPrice" >-->
<!--              <template slot-scope="scope">{{ scope.row.vipPrice /100 }}</template>-->
<!--            </el-table-column>-->
          </el-table>
        </template>
      </el-table-column>

      <el-table-column align="center" label="商品ID" prop="id" />

      <el-table-column align="center" min-width="100" label="名称" prop="title" />

      <el-table-column align="center" property="img" label="图片">
        <template slot-scope="scope">
          <img :src="scope.row.img" width="40" >
        </template>
      </el-table-column>

      <el-table-column align="center" label="商品详细介绍" prop="detail">
        <template slot-scope="scope">
          <el-dialog :visible.sync="detailDialogVisible" title="商品详情">
            <div v-html="goodsDetail" />
          </el-dialog>
          <el-button type="primary" size="mini" @click="showDetail(scope.row)">查看</el-button>
        </template>
      </el-table-column>

      <el-table-column align="center" label="类目ID" prop="categoryId" />

      <el-table-column align="center" label="单位" prop="unit" />

      <el-table-column align="center" label="是否在售" prop="status">
        <template slot-scope="scope">
          <el-tag
            :type="scope.row.status == 1 ? 'success' : 'info' "
          >{{ scope.row.status == 1 ? '在售' : '未售' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        max-width="300"
        min-width="300"
        label="描述"
        prop="description"
      />

      <el-table-column align="center" label="操作" width="250" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-permission="['operation:goods:edit']" type="primary" size="mini" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button v-permission="['operation:goods:freezeOrActivation']" v-if=" scope.row.status == 1" type="warning" size="mini" @click="freezeOrActivationBtn(scope.row,'freeze')">下架</el-button>
          <el-button v-permission="['operation:goods:freezeOrActivation']" v-else type="success" size="mini" @click="freezeOrActivationBtn(scope.row,'activation')">上架</el-button>
          <el-button v-permission="['operation:goods:delete']" type="danger" style="margin:5px" size="mini" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="listQuery.page"
      :limit.sync="listQuery.limit"
      @pagination="getList"
    />

    <el-tooltip placement="top" content="返回顶部">
      <back-to-top :visibility-height="100" />
    </el-tooltip>
  </div>
</template>

<script>
import axios from 'axios'
import { listGoods, deleteGoods, detailGoods, freezeOrActivtion, batchDeleteGoods } from '@/api/goods'
import BackToTop from '@/components/BackToTop'
import { categoryTree } from '@/api/category'
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination

export default {
  name: 'GoodsList',
  components: { BackToTop, Pagination },
  data() {
    return {
      importGroup: [],
      importList: [],
      importShow: false,
      list: [],
      total: 0,
      listLoading: true,
      categoryOptions: [],
      goodsStatusMap: [{ value: 0, name: '下架' }, { value: 1, name: '正常' }],
      listQuery: {
        page: 1,
        limit: 20,
        title: undefined,
        status: undefined,
        barcode: undefined,
        categoryId: undefined
      },
      goodsDetail: '',
      detailDialogVisible: false,
      selectedIds: []
    }
  },
  created() {
    this.getList()
    this.refreshCategoryOptions()
  },
  methods: {
    importData() {
      this.importShow = true
      axios.get('http://114.67.94.213:8080/goods/attr').then(res => {
        this.importGroup = Object.keys(res.data).map(item => {
          return {
            label: item,
            value: res.data[item]
          }
        })
      })
    },
    doImport() {
      // this.$refs.download.href = `http://114.67.94.213:8080/goods/export?titles=${this.importList.join(',')}&categoryId=${this.listQuery.categoryId}&barcode=${this.listQuery.barcode}&status=${this.listQuery.status}`
      this.$refs.download.href = `http://114.67.94.213:8080/goods/export?titles=${this.importList.join(',')}`
      this.$refs.download.click()
      this.importShow = false
    },
    getList() {
      this.listLoading = true
      listGoods(this.listQuery)
        .then(response => {
          response.data.data.items.forEach(item => {
            item.price = item.price / 100
            item.originalPrice = item.originalPrice / 100
            item.vipPrice = item.vipPrice / 100
          })
          this.list = response.data.data.items
          this.total = response.data.data.total
          this.listLoading = false
        })
        .catch(() => {
          this.list = []
          this.total = 0
          this.listLoading = false
        })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleCreate() {
      this.$router.push({ path: '/goods/upsert' })
    },
    handleUpdate(row) {
      this.$router.push({ path: '/goods/upsert', query: { id: row.id }})
    },
    showDetail(row) {
      if (!this.detailDialogVisible) {
        detailGoods(row.id)
          .then(response => {
            this.goodsDetail = response.data.data.detail.replace(/<img/g, "<img style='max-width:100%;height:auto;line-height:0px'")
            this.detailDialogVisible = true
          })
      }
    },
    handleDelete(row) {
      this.$confirm('此操作将永久删除商品--' + row.title + '--, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteGoods(row.id)
          .then(response => {
            this.$notify.success({
              title: '成功',
              message: '删除成功'
            })
            const index = this.list.indexOf(row)
            this.list.splice(index, 1)
          })
          .catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          })
      }).catch(() => {
        return false
      })
    },
    refreshCategoryOptions() {
      if (this.categoryOptions.length === 0) {
        categoryTree().then(response => {
          this.categoryOptions = response.data.data
        })
      }
    },
    handleCategoryOption(e) {
      if (e !== undefined) {
        const tag = e[e.length - 1]
        this.listQuery.categoryId = tag // 回调指定分类
      } else {
        this.listQuery.categoryId = undefined
      }
    },
    freezeOrActivationBtn(row, title) {
      var status = 0
      var sign = ''
      if (title === 'activation') {
        status = 1
        sign = '激活'
      } else if (title === 'freeze') {
        status = 0
        sign = '冻结'
      } else {
        return false
      }

      this.$confirm('此操作将' + sign + '商品--' + row.title + '--, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).catch(() => {
        return false
      }).then(() => {
        freezeOrActivtion(row.id, status).then(response => {
          this.$notify.success({
            title: '成功',
            message: sign + '成功'
          })
          const index = this.list.indexOf(row)
          this.list.splice(index, 1, response.data.data)
        }).catch(response => {
          this.$notify.error({
            title: '失败',
            message: response.data.errmsg
          })
        })
      })
    },
    handleSelectionChange(e) {
      const temp = []
      e.forEach(item => {
        temp.push(item.id)
      })
      this.selectedIds = temp
    },
    handleBatchDelete(e) {
      // 执行批量删除
      this.$confirm('此操作将永久删除选中的' + this.selectedIds.length + '件商品, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        batchDeleteGoods(this.selectedIds)
          .then(response => {
            this.$notify.success({
              title: '成功',
              message: '删除成功'
            })
            this.getList()
          })
          .catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          })
      }).catch(() => {
        return false
      })
    }
  }
}
</script>
