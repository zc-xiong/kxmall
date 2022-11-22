<template>
  <div class="main">
    <el-select v-model="storageId" placeholder="请选择入库仓库" clearable filterable>
      <el-option
        v-for="item in entrepotOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"/>
    </el-select>
    <el-input
      v-model="inStockNumbers"
      placeholder="输入入库单号"
      style="width:250px"
      clearable
      suffix-icon="el-icon-search">
    </el-input>
    <el-select v-model="states" placeholder="请选择入库状态" clearable filterable>
      <el-option value="" label="全部"/>
      <el-option :value="0" label="未入库"/>
      <el-option :value="1" label="已入库"/>
    </el-select>
    <el-date-picker
      v-model="ingoingDay"
      format="yyyy 年 MM 月 dd 日"
      value-format="yyyy-MM-dd"
      type="date"
      placeholder="选择入库日期">
    </el-date-picker>
    <el-button type="primary" icon="el-icon-search" @click="doSearch">查找</el-button>
    <el-button type="primary" icon="el-icon-close" @click="doClear">清空条件</el-button>
    <el-button type="primary" icon="el-icon-plus" @click="showEdit(1,2)">创建</el-button>
    <el-table
      :data="tableData"
      border
      style="width: 100%; margin-top:20px">
      <el-table-column
        type="selection"
        align="center"
        width="55"/>
      <el-table-column
        prop="storeName"
        align="center"
        width="120"
        label="仓库">
      </el-table-column>
      <el-table-column
        align="center"
        prop="inStockNumbers"
        label="入库单号"/>
      <el-table-column
        align="center"
        label="入库状态">
        <template slot-scope="scope">
          <span>{{ scope.row.states | inStatus }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="ingoingPerson"
        align="center"
        label="入库人"/>
      <el-table-column
        align="center"
        label="入库时间">
        <template slot-scope="scope">
          <span>{{ scope.row.ingoingTime | formatDateAndTime }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="ingoingPerson"
        align="center"
        label="创建/修改人"/>
      <el-table-column
        prop="gmtUpdate"
        align="center"
        label="创建修改时间">
        <template slot-scope="scope">
          <span>{{ scope.row.gmtUpdate | formatDateAndTime }}</span>
        </template>
      </el-table-column>
      <el-table-column
        fixed="right"
        align="center"
        label="操作"
        width="300"
      >
        <template slot-scope="scope">
          <div v-if="scope.row.states == 0">
            <el-button type="primary" size="mini" @click="showEdit(scope.row,0)">详情</el-button>
            <el-button type="primary" size="mini" @click="showEdit(scope.row,1)">修改</el-button>
            <el-button type="primary" size="mini" @click="storeIn(scope.row)">入库</el-button>
            <el-button type="danger" size="mini" @click="storeDelete(scope.row)">删除</el-button>
          </div>
          <div v-else>
            <el-button type="primary" size="mini" @click="showEdit(scope.row,0)">详情</el-button>
          </div>

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
    <!-- 详情 编辑  创建 的弹层页面 -->
    <el-dialog v-if="showEditDialog" :title="showEditDialogText" :visible.sync="showEditDialog" top="7vh">
      <el-form :inline="true" class="demo-form-inline">
        <el-form-item label="入库仓库">
          <el-select v-model="showEditStockId" :disabled="showEditState!=2" placeholder="请选择入库仓库" clearable filterable @change="showEditStorageChange" @click.native="showEditStorageClick">
            <el-option
              v-for="item in entrepotOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="showEditStockRemark"
            type="textarea"
            autosize
            clearable
            placeholder="请输入内容"
            style="height:36px">
          </el-input>
        </el-form-item>
      </el-form>
      <el-row>
        <div v-if="showEditState">
          <el-button type="primary" @click="addGoodsCreate">添加</el-button>
          <el-button @click="deleteBatch">删除</el-button>
        </div>
        <div v-else>
          <el-button disabled type="primary" @click="addGoodsCreate">添加</el-button>
          <el-button disabled @click="deleteBatch">删除</el-button>
        </div>

      </el-row>
      <el-table
        :data="showEditStockDataLists"
        border
        height="500"
        style="width: 100%; margin-top:15px"
        @selection-change="getUserGoodsData">
        <el-table-column
          v-if="showEditState"
          type="selection"
          align="center"
          width="55"/>
        <el-table-column
          prop="category"
          align="center"
          width="120"
          label="商品类目"/>
        <el-table-column
          align="center"
          prop="barCode"
          label="商品条码"/>
        <el-table-column
          align="center"
          prop="spuName"
          label="商品名称"/>
        <el-table-column
          align="center"
          prop="skuName"
          label="商品规格"/>
        <el-table-column
          align="center"
          prop="stockNum"
          label="可用量"/>
        <el-table-column
          align="center"
          prop="inStockNum"
          label="入库数量">
          <template slot-scope="scope">
            <div v-if="showEditState != 0">
              <el-input v-model="scope.row.inStockNum" clearable placeholder="请输入内容"></el-input>
            </div>
            <div v-else>
              <span>{{ scope.row.inStockNum }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          v-if = "showEditState"
          fixed="right"
          align="center"
          label="操作"
        >
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="showEditDelete(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showEditDialog = false">取 消</el-button>
        <el-button type="primary" @click="showEditSureBtn">确 定</el-button>
      </div>
      <!-- 添加商品的弹出层  -->
      <el-dialog v-if="addGoodsShow" :visible.sync="addGoodsShow" title="添加商品" append-to-body top="7vh">
        <add-goods :userselectid="showEditStockUserSelectId" :storage-id="showEditStockId+''" @closeDialog="closeDialog" @batchAdd="batchAdd" @addOne="addOne"></add-goods>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script>
import { inStockList, inStockStoragAllName, inStockCreate, inStockDeleteAll, inStockSelectById, inStockUpdateInOfStock } from '@/api/in_stock'
import store from '@/store'
import addGoods from './addGoods'
export default {
  name: 'InStock',
  filters: {
    inStatus(status) {
      return status === 0 ? '未入库' : '已入库'
    }
  },
  components: {
    addGoods
  },
  data() {
    return {
      inStockNumbers: '', /* 入库订单号 */
      storageId: '', /* 入库仓库Id */
      ingoingDay: '', /* 入库日期 */
      states: '', /* 入库状态 */
      pageSize: 20,
      total: 0,
      showEditDialog: false, /* 是否显示编辑弹窗 */
      showEditDialogText: '入库单', /* 编辑弹层的描述文字 */
      showEditStockId: '', /* 编辑弹层的显示仓库 */
      showEditStockRemark: '', /* 编辑弹层的备注内容 */
      showEditStockLists: {}, /* 编辑弹层 的表格数据 如果数据有更新 也是返回这个数据  */
      showEditStockDataLists: [], /* 编辑层 表格渲染数据 */
      showEditStockUserSelect: [], /* 编辑弹层 用户选中的数据 */
      showEditStockUserSelectId: [], /* 编辑层 用户选择的商品的条码 */
      showEditState: '', /* 0 详情  1 编辑  2 创建 */
      addGoodsShow: false, /* 是否显示添加商品的弹层 */
      priceList: [],
      currentPage: 1,
      input: '',
      entrepotOptions: [],
      tableData: [],
      categoryOptions: [],
      category: ''
    }
  },
  created() {
    inStockStoragAllName().then(res => {
      /* 获取仓库 */
      res.data.data.forEach(item => {
        this.entrepotOptions.push({
          label: item.name,
          value: item.id
        })
      })
      this.getList(
        { page: 1, limit: this.pageSize }
      )
    })
    /*     categorySecondLevelTree().then(res => {
      // 获取二级类目
      console.log(res)
      this.categoryOptions = res.data.data
      this.categoryOptions.forEach(item => {
        item.children.forEach(val => {
          val.children = undefined
        })
      })
    })*/
  },
  methods: {
    async getList(obj) {
      const res = await inStockList(obj)
      this.total = res.data.data.total
      this.tableData = res.data.data.items
      this.tableData.map(item => {
        item.storeName = this.storeIdTurnName(item.storageId)
      })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getList({
        inStockNumbers: this.inStockNumbers,
        storageId: this.storageId,
        ingoingDay: this.ingoingDay,
        states: this.states,
        page: this.currentPage,
        limit: val
      })
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList({
        inStockNumbers: this.inStockNumbers,
        storageId: this.storageId,
        ingoingDay: this.ingoingDay,
        states: this.states,
        page: val,
        limit: this.pageSize
      })
    },
    doSearch() {
      /* 将页码初始化为1 */
      this.currentPage = 1
      this.getList({
        inStockNumbers: this.inStockNumbers,
        storageId: this.storageId,
        ingoingDay: this.ingoingDay,
        states: this.states,
        page: this.currentPage,
        limit: this.pageSize
      })
    },
    doClear() {
      this.inStockNumbers = ''
      this.storageId = ''
      this.ingoingDay = ''
      this.states = ''
      this.doSearch()
    },
    getEditText(state) {
      let text = '入库单'
      switch (state) {
        case 0:
          text = '入库单详情'
          break
        case 1:
          text = '编辑入库单'
          break
        case 2:
          text = '创建入库单'
          break
      }
      return text
    },
    async showEdit(data, state) {
      /* 点击详情 或 修改按钮 获取具体数据 */
      if (state < 2) {
        this.showEditStockLists = await inStockSelectById({ id: data.id, InStockNumbers: data.inStockNumbers })
        this.showEditStockDataLists = this.showEditStockLists.data.data.inStockSpuDOS
        this.showEditStockId = data.storageId
        this.showEditStockRemark = data.remarks
      } else {
        /* 如果是创建 则给个空的数据结构 */
        this.showEditStockLists = { data: { data: { inStockSpuDOS: [] }}}
        this.showEditStockDataLists = this.showEditStockLists.data.data.inStockSpuDOS
        this.showEditStockId = ''
        this.showEditStockRemark = ''
      }
      this.showEditState = state
      this.showEditDialogText = this.getEditText(state)
      this.showEditDialog = true
    },
    async showEditSureBtn() {
      /* 如果 是查看详情 则直接关闭弹窗 */
      if (this.showEditState === 1) {
        /* 如果是编辑 则将数据对象提交到更新接口 */
        // 去除 showEditStockDataLists 中的id
        this.showEditStockDataLists.map(item => {
          delete item.id
        })
        // const config = {
        //   id: this.showEditStockLists.data.data.id,
        //   inStockNumbers: this.showEditStockLists.data.data.inStockNumbers,
        //   storageId: this.showEditStockLists.data.data.storageId,
        //   ingiongPerson: this.showEditStockLists.data.data.ingiongPerson,
        //   remarks: this.showEditStockLists.data.data.remarks
        // }
        // const res = await inStockUpdate({ inStockSpuDOS: this.showEditStockDataLists, ...config })
        // console.log(res)
      }
      if (this.showEditState === 2) {
        const config = {
          storageId: this.showEditStockId,
          remarks: this.showEditStockRemark
        }
        const res = await inStockCreate({ inStockSpuDOS: this.showEditStockDataLists, ...config })
        if (res.data.errno === 200) {
          this.getList({
            inStockNumbers: this.inStockNumbers,
            storageId: this.storageId,
            ingoingDay: this.ingoingDay,
            states: this.states,
            page: this.currentPage,
            limit: this.pageSize
          })
        }
      }
      this.showEditDialog = false
    },
    storeIdTurnName(id) {
      /* 根据传进来的id 转为相应的名字 */
      let name = id
      this.entrepotOptions.map(item => {
        if (item.value === id) {
          name = item.label
        }
      })
      return name
    },
    addGoodsCreate() {
      // 如果仓库未选择 则提示先选仓库
      if (!this.showEditStockId) {
        this.$message({
          message: '请先选择仓库',
          type: 'warning'
        })
        return false
      }
      // 获取用户选择商品id(已选的商品需要过滤)
      this.showEditStockUserSelectId = this.leaveUserSelectId(this.showEditStockDataLists)
      this.addGoodsShow = true
    },
    closeDialog() {
      this.addGoodsShow = false
    },
    goodsWashData(data) {
      /* 对添加的商品信息进行处理 */
      const res = []
      data.map(item => {
        const result = {}
        /* 对item进行校验 放置对非对象取值而导致报错 */
        item.skuDTO === undefined ? item.skuDTO = {} : ''
        item.categoryDO === undefined ? item.skuDTO = {} : ''
        item.spuDO === undefined ? item.skuDTO = {} : ''
        result.barCode = item.skuDTO.barCode
        result.category = item.categoryDO.title
        result.skuId = item.skuId
        result.skuName = item.skuDTO.title
        result.spuName = item.spuDO.title
        result.stockNum = item.stock
        result.inStockNum = 0
        if (this.showEditState === 1) {
          /* 编辑状态下 还要增加一个入库单号 */
          result.inStockNumbers = this.showEditStockLists.data.data.inStockNumbers
        }
        res.push(result)
      })
      return res
    },
    leaveUserSelectId(data) {
      let res = []
      res = data.map(item => {
        return item.barCode
      })
      return res
    },
    batchAdd(data) {
      /* 批量添加 */
      const washData = this.goodsWashData(data)
      this.showEditStockDataLists.push(...washData)
    },
    addOne(data) {
      /* 单条添加 */
      const d = [data]
      const washData = this.goodsWashData(d)
      this.showEditStockDataLists.push(...washData)
    },
    getUserGoodsData(row) {
      const _this = this
      _this.showEditStockUserSelect = [] // 每次改动重置ID
      row.forEach((item) => {
        _this.showEditStockUserSelect.push(item)
      })
    },
    showEditDelete(index) {
      /* 删除对应的选项 */
      this.showEditStockDataLists.splice(index, 1)
    },
    showEditStorageClick() {
      if (this.showEditStockDataLists.length > 0) {
        this.$message({
          message: '改变仓库将会清空已选商品',
          type: 'warning'
        })
      }
    },
    showEditStorageChange(val) {
      // 仓库切换时 清空列表
      this.showEditStockDataLists = []
    },
    deleteBatch() {
      /* 批量删除 */
      this.showEditStockDataLists = this.leaveArray(this.showEditStockDataLists, this.showEditStockUserSelect, 'barCode')
    },
    leaveArray(o, dif, attr) {
      const res = []
      o.map(item => {
        const s = item[attr]
        let flag = true
        dif.map(i => {
          if (s === i[attr]) {
            flag = false
          }
        })
        if (flag) {
          res.push(item)
        }
      })
      return res
    },
    async storeIn(data) {
      this.$confirm('确定入库吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const token = store.getters.token
        const name = store.getters.roles[0]
        const res = await inStockUpdateInOfStock({ inStockNumbers: data.inStockNumbers, storageId: data.storageId, ingoingPerson: name, adminId: token })
        if (res.data.errno === 200) {
          this.$message({
            type: 'success',
            message: '入库成功!'
          })
          this.getList({
            inStockNumbers: this.inStockNumbers,
            storageId: this.storageId,
            ingoingDay: this.ingoingDay,
            states: this.states,
            page: this.currentPage,
            limit: this.pageSize
          })
        }
      }).catch(() => {

      })
    },
    storeDelete(data) {
      this.$confirm('确定删除吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const token = store.getters.token
        const res = await inStockDeleteAll({ adminId: token, id: data.id, inStockNumbers: data.inStockNumbers })
        if (res.data.errno === 200) {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.getList({
            inStockNumbers: this.inStockNumbers,
            storageId: this.storageId,
            ingoingDay: this.ingoingDay,
            states: this.states,
            page: this.currentPage,
            limit: this.pageSize
          })
        }
      }).catch(() => {

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

