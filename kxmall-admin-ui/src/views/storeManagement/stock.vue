<template>
  <div class="main">
    <el-select v-model="query.storageId" style="width:130px" placeholder="请选择前置仓">
      <el-option
        v-for="item in storageOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"/>
    </el-select>
    <el-cascader
      :options="categoryOptions"
      :props="{ checkStrictly: true }"
      placeholder="请选择类目"
      clearable
      class="filter-item"
      filterable
      style="width: 130px;"
      @change="onSelectCategory" />
    <el-input
      v-model="query.name"
      style="width:240px"
      clearable
      placeholder="请输入商品条码/商品ID/商品名称"/>
    <span class="gap"></span>

    <el-select v-model="query.type" style="width:90px" placeholder="请选择销售状态" clearable filterable>
      <el-option
        v-for="item in typeOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"/>
    </el-select>
    <el-input
      v-model="query.minNum"
      style="width:110px"
      clearable
      placeholder="输入最小值"/>
    <span class="line">—</span>
    <el-input
      v-model="query.maxNum"
      style="width:110px"
      clearable
      placeholder="输入最大值"/>

    <el-button type="primary" @click="onSearch">查询</el-button>

    <el-checkbox v-model="query.showType" @change="onSearch">只显示库存预警</el-checkbox>

    <!-- 表格内容 -->
    <el-table
      :data="list"
      border
      style="width: 100%; margin-top:20px">
      <el-table-column
        type="selection"
        align="center"
        width="55"/>
      <el-table-column
        prop="storageDto.name"
        align="center"
        width="120"
        label="前置仓"/>
      <el-table-column
        prop="categoryDto.title"
        align="center"
        width="200"
        label="商品类目"/>
      <el-table-column
        align="center"
        prop="skuDto.barCode"
        label="商品条码"/>
      <el-table-column
        prop="id"
        align="center"
        label="商品ID"/>
      <el-table-column
        prop="title"
        align="center"
        label="商品名称"/>
      <el-table-column
        prop="skuDto.title"
        align="center"
        label="商品规格"/>
      <el-table-column
        prop="stockDto.nowStock"
        align="center"
        label="现货量"/>
      <el-table-column
        class-name="cell-warning"
        prop="stockDto.stock"
        align="center"
        label="可售量">
        <template slot-scope="scope">
          <div :class="{'stock--warning': scope.row.stockDto.stock < scope.row.stockDto.warningNum}">{{ scope.row.stockDto.stock }}</div>
        </template>
      </el-table-column>
      <el-table-column
        prop="stockDto.frezzStock"
        align="center"
        label="锁定量"/>
      <el-table-column
        align="center"
        label="预警量">
        <template slot-scope="scope">
          <el-button type="text" @click="onEditWarnVaue(scope.row, scope.$index)">{{ scope.row.stockDto.warningNum }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="page">
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="query.page"
        :limit.sync="query.limit"
        @pagination="getList"
      />
    </div>

    <!-- 编辑库存预警值 -->
    <el-dialog
      :visible.sync="warningDialog.visible"
      title="预警量设置"
      width="30%">
      <el-form
        ref="warningDialog"
        :model="warningDialog.info.stockDto"
        :rules="warningDialog.rules"
        label-position="right"
        label-width="80px">
        <el-form-item prop="warningNum" label="预警值">
          <el-input v-model="warningDialog.info.stockDto.warningNum" clearable autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="warningDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="onSaveWarningValue">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { fetchWarningList, editWarning } from '@/api/stock'
import { categoryTree } from '@/api/category'
import { getEntrepotList } from '@/api/entrepot'
import { deepClone } from '@/utils'
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination

export default {
  name: 'Stock',
  components: { Pagination },
  data() {
    return {
      // 查询条件
      query: {
        storageId: '',
        categoryId: '',
        name: '',
        type: 1,
        minNum: undefined,
        maxNum: undefined,
        showType: false,
        limit: 20,
        page: 1
      },
      // 表格列表内容
      list: [],
      total: 0,
      // 设置预警值弹窗
      warningDialog: {
        visible: false,
        info: {
          stockDto: {}
        },
        index: 0,
        rules: {
          warningNum: { required: true, message: '请输入预警量', trigger: 'blur' }
        }
      },
      // 类目列表
      categoryList: [],
      // 仓库列表
      storageList: []
    }
  },

  computed: {
    storageOptions() {
      return this.storageList.map((storage) => ({ label: storage.name, value: storage.id }))
    },
    categoryOptions() {
      return this.categoryList
    },
    typeOptions() {
      return [
        { label: '现货量', value: 1 },
        { label: '可售量', value: 2 },
        { label: '锁定量', value: 3 }
      ]
    }
  },

  async created() {
    // 获取类目列表
    const { data: { data: categoryList }} = await categoryTree()
    this.categoryList = categoryList
    this.categoryList.forEach(item => {
      item.children.forEach(val => {
        val.children = undefined
      })
    })
    // 获取前置仓列表
    const { data: { data: { items: storageList }}} = await getEntrepotList()
    this.storageList = storageList

    // 获取预警值列表
    const { data: { data: { items, total }}} = await fetchWarningList(this.query)
    this.list = items
    this.total = total
  },
  methods: {
    onSearch() {
      this.query.page = 1
      this.getList()
    },
    getList() {
      fetchWarningList(this.query).then(response => {
        this.list = response.data.data.items
        this.total = response.data.data.total
      })
    },
    onEditWarnVaue(info, index) {
      this.warningDialog.visible = true
      this.warningDialog.info = deepClone(info)
      this.warningDialog.index = index
    },
    onSaveWarningValue() {
      this.$refs.warningDialog.validate(async(valid) => {
        if (valid) {
          try {
            await editWarning({
              storageId: this.storageList.find(storage => storage.name === this.warningDialog.info.storageDto.name).id,
              spuId: this.warningDialog.info.id,
              num: this.warningDialog.info.stockDto.warningNum
            })
            this.list.splice(this.warningDialog.index, 1, this.warningDialog.info)
            this.warningDialog.visible = false
          } catch (response) {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          }
        } else {
          return false
        }
      })
    },
    onSelectCategory(value) {
      debugger
      this.query.categoryId = value[value.length - 1]
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

.stock--warning {
  background: #f00;
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: center
}
.cell-warning .cell {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: center
}

.gap {
  display: inline-block;
  width: 10px
}

.line {
  display: inline-flex;
  height: 36px;
  color: rgb(210, 211, 215);
}

</style>

