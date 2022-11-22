<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-select
        v-model="listQuery.recommendType"
        style="width: 200px"
        class="filter-item"
        clearable
        filterable
        placeholder="请选择推荐类型"
      >
        <el-option v-for="(key,index) in recommendTypeMap" :key="index" :label="key.value" :value="key.label" />
      </el-select>
      <el-button
        v-permission="['admin:recommend:queryRecommendByType']"
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="queryRecommendBtn"
      >查找</el-button>
      <el-button
        v-permission="['admin:recommend:addRecommendBatch']"
        class="filter-item"
        type="primary"
        icon="el-icon-edit"
        @click="createDialogBtn"
      >添加</el-button>

    </div>

    <!-- 查询结果 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      size="small"
      element-loading-text="正在查询中。。。"
      border
      fit
      highlight-current-row
    >
      <el-table-column align="center" label="推荐ID" prop="id" sortable />

      <el-table-column align="center" label="推荐商品" prop="spuTitle" />

      <el-table-column align="center" label="推荐类型" prop="recommendType">
        <template slot-scope="scope">
          <el-tag>{{ scope.row.recommendType | recommendTypeFilter }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-permission="['admin:recommend:deleteRecommend']"
            type="danger"
            size="mini"
            @click="deleteRecommendBtn(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="listQuery.pageNo"
      :limit.sync="listQuery.limit"
      @pagination="getList"
    />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="dataForm"
        status-icon
        label-position="left"
        label-width="100px"
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="隐藏的推荐id" prop="id" hidden>ue
          <el-input v-model="dataForm.id" clearable />
        </el-form-item>

        <el-form-item label="推荐类型" prop="recommendType">
          <el-select v-model="dataForm.recommendType" placeholder="请选择" clearable filterable>
            <el-option v-for="(key, index) in recommendTypeMap" :key="index" :label="key.value" :value="key.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐商品" prop="spuIds">
          <el-cascader
            :options="options"
            v-model="linkunio"
            :props="{ multiple: true }"
            collapse-tags
            placeholder="关联商品"
            filterable
            clearable
            @change="handleLink"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button v-if="dialogStatus=='create'" type="primary" @click="createRecommendBtn">确定</el-button>
        <!-- <el-button v-else type="primary" @click="updateRecommendBtn">确定</el-button> -->
      </div>
    </el-dialog>

</div></template>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #20a0ff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 145px;
  height: 145px;
  display: block;
}
</style>

<script>
import { listRecommend, createRecommendBatch, deleteRecommend, getRecommendTypeEnums } from '@/api/recommend'
import { spuTree } from '@/api/goods'
import { uploadPath } from '@/api/storage'
import { getToken } from '@/utils/auth'
import { clearTreeEmptyChildren } from '@/utils/index'
import Pagination from '@/components/Pagination'
import ElOption from '../../../node_modules/element-ui/packages/select/src/option' // Secondary package based on el-pagination

const getOptions = async() => {
  const res = (await getRecommendTypeEnums()).data.data
  const arr = res.map(item => {
    return {
      label: item.code,
      value: item.msg
    }
  })
  sessionStorage.setItem('recommendType', JSON.stringify(arr))
}
getOptions()
export default {
  name: 'Recommend',
  components: {
    ElOption,
    Pagination
  },
  filters: {
    recommendTypeFilter(code) {
      return JSON.parse(sessionStorage.getItem('recommendType'))[code - 1].value
    }
  },
  data() {
    return {
      uploadPath,
      linkunio: undefined,
      options: [],
      value: [],
      list: [],
      total: 0,
      listLoading: true,
      recommendTypeMap: [],
      listQuery: {
        pageNo: 1,
        limit: 20,
        recommendType: undefined
      },
      dataForm: {
        id: undefined,
        recommendType: undefined,
        spuIds: []
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑',
        create: '创建'
      },
      rules: {
        recommendType: [{ required: true, message: '推荐类型', trigger: 'blur' }],
        spuIds: [{ required: true, message: '推荐商品', trigger: 'blur' }]
      },
      downloadLoading: false
    }
  },
  computed: {
    headers() {
      return {
        accessToken: getToken()
      }
    }
  },
  created() {
    getRecommendTypeEnums().then(res => {
      console.log(res)
      this.recommendTypeMap = res.data.data.map(item => {
        return {
          label: item.code,
          value: item.msg
        }
      })
    })
    this.getList()
    this.refreshOptions()
  },
  methods: {
    getList() {
      this.listLoading = true
      listRecommend(this.listQuery)
        .then(response => {
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
    // 选择时赋值spuIds
    handleLink(e) {
      if (e !== undefined) {
        this.dataForm.spuIds = []
        e.forEach(tag => {
          const t = tag[tag.length - 1]
          this.dataForm.spuIds.push(t.substring(2))
        })
      }
    },
    queryRecommendBtn() {
      this.listQuery.page = 1
      this.getList()
    },
    resetForm() {
      this.dataForm = {
        id: undefined,
        recommendType: undefined,
        spuIds: []
      }
    },
    refreshOptions() {
      if (this.options.length === 0) {
        spuTree().then(response => {
          this.options = clearTreeEmptyChildren(response.data.data)
        })
      }
    },
    createDialogBtn() {
      this.resetForm()
      this.refreshOptions()
      this.dialogStatus = 'create'
      this.linkunio = undefined
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    createRecommendBtn() {
      this.$refs['dataForm'].validate(valid => {
        if (valid && this.checkGoods()) {
          createRecommendBatch(this.dataForm)
            .then(response => {
            // 需要后台返回值
            // this.list.unshift(response.data.data)
              this.getList()
              this.dialogFormVisible = false
              this.$notify.success({
                title: '成功',
                message: '创建成功'
              })
            })
            .catch(response => {
              console.log(response)
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              })
            })
        }
      })
    },
    checkGoods() {
      console.log(this.linkunio)
      if (this.linkunio === undefined || this.linkunio === null) { // || this.linkunio.length <= 3
        this.$notify.error({
          title: '提示',
          message: '请选择商品'
        })
        return false
      }
      return true
    },
    // // 点击编辑按钮时的处理
    // updateRecommend(row) {
    //   this.dataForm = Object.assign({}, row)
    //   this.refreshOptions()
    //   this.dialogStatus = 'update'
    //   this.dialogFormVisible = true
    //   this.$nextTick(() => {
    //     this.$refs['dataForm'].clearValidate()
    //   })
    // },
    // updateData() {
    //   this.$refs['dataForm'].validate(valid => {
    //     if (valid && this.checkAdType()) {
    //       updateRecommend(this.dataForm)
    //         .then(() => {
    //           for (const v of this.list) {
    //             if (v.id === this.dataForm.id) {
    //               const index = this.list.indexOf(v)
    //               this.list.splice(index, 1, this.dataForm)
    //               break
    //             }
    //           }
    //           this.dialogFormVisible = false
    //           this.$notify.success({
    //             title: '成功',
    //             message: '更新广告成功'
    //           })
    //         })
    //         .catch(response => {
    //           this.$notify.error({
    //             title: '失败',
    //             message: response.data.errmsg
    //           })
    //         })
    //     }
    //   })
    // },
    deleteRecommendBtn(row) {
      this.$confirm('此操作将永久删除该商品推荐--' + row.spuTitle + '--, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteRecommend(row.id, row.recommendType)
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
    }
  }
}
</script>
