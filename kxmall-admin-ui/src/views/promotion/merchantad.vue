<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-select
        v-model="listQuery.adType"
        style="width: 200px"
        class="filter-item"
        placeholder="请选择广告类型"
      >
        <el-option v-for="(key,index) in adTypeMap" :key="index" :label="key.name" :value="key.value" />
      </el-select>
      <el-select
        v-model="listQuery.status"
        style="width: 200px"
        class="filter-item"
        clearable
        filterable
        placeholder="请选择广告状态"
      >
        <el-option v-for="(key,index) in adStatusMap" :key="index" :label="key.name" :value="key.value" />
      </el-select>
      <el-button
        v-permission="['admin:advertisement:queryAdvertisement']"
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="handleFilter"
      >查找</el-button>

      <el-button
        v-permission="['admin:advertisement:addAdvertisement']"
        class="filter-item"
        type="primary"
        icon="el-icon-edit"
        @click="handleCreate"
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
      <el-table-column align="center" label="广告ID" prop="id" sortable />

      <el-table-column align="center" label="广告标题" prop="title" />

      <el-table-column align="center" label="广告类型" prop="adType">
        <template slot-scope="scope">
          <el-tag>{{ scope.row.adType | adTypeFilter }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="广告状态" prop="status">
        <template slot-scope="scope">
          <el-tag>{{ scope.row.status | adStatusFilter }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="广告图片" prop="imgUrl">
        <template slot-scope="scope">
          <img v-if="scope.row.imgUrl" :src="scope.row.imgUrl" width="80" >
        </template>
      </el-table-column>

      <el-table-column align="center" label="活动链接" prop="url" />

      <el-table-column align="center" label="操作" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-permission="['admin:advertisement:updateAdvertisement']"
            type="primary"
            size="mini"
            @click="handleUpdate(scope.row)"
          >编辑</el-button>
          <el-button
            v-permission="['admin:advertisement:deleteAdvertisement']"
            type="danger"
            size="mini"
            @click="handleDelete(scope.row)"
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
        <el-form-item label="隐藏的广告id" prop="id" hidden>
          <el-input v-model="dataForm.id" clearable />
        </el-form-item>
        <el-form-item label="广告标题" prop="title">
          <el-input v-model="dataForm.title" clearable />
        </el-form-item>
        <el-form-item label="广告图片" prop="imgUrl">
          <el-upload
            :headers="headers"
            :action="uploadPath"
            :show-file-list="false"
            :on-success="uploadSuccessHandle"
            :before-upload="onBeforeUpload"
            class="avatar-uploader"
            accept=".jpg, .jpeg, .png, .gif"
          >
            <img v-if="dataForm.imgUrl" ref="adImg" :src="dataForm.imgUrl" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon" />
          </el-upload>
        </el-form-item>
        <el-form-item label="广告类型" prop="adType">
          <el-select v-model="dataForm.adType" clearable filterable placeholder="请选择">
            <el-option v-for="(key, index) in adTypeMap" :key="index" :label="key.name" :value="key.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="广告状态" prop="status">
          <el-select v-model="dataForm.status" placeholder="请选择" clearable filterable>
            <el-option v-for="(key, index) in adStatusMap" :key="index" :label="key.name" :value="key.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动链接">
          <el-input v-model="userLinkUnion" placeholder="请输入活动连接" clearable></el-input>
        </el-form-item>
        <el-form-item label="站外链接">
          <el-input v-model="dataForm.outUrl" clearable />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button v-if="dialogStatus=='create'" type="primary" @click="createData">确定</el-button>
        <el-button v-else type="primary" @click="updateData">确定</el-button>
      </div>
    </el-dialog>

    <canvas ref="canvas" hidden/>
  </div>
</template>

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
.cascaderInput{
  width:260px;
  position:absolute;
  top:1px;
  left:3px;
}
.cascaderInput input{
  border:none;
}
</style>

<script>
import { listAd, createAd, updateAd, deleteAd, getImageColor } from '@/api/merchantad'
import { spuTree } from '@/api/goods'
import { uploadPath } from '@/api/storage'
import { getToken } from '@/utils/auth'
import { clearTreeEmptyChildren } from '@/utils/index'
import Pagination from '@/components/Pagination'
import ElOption from 'element-ui/packages/select/src/option' // Secondary package based on el-pagination

const adTypeMap = [
  { value: 1, unionType: 3, name: '首页轮播' },
  { value: 2, unionType: 1, name: '分类精选' },
  { value: 3, unionType: 3, name: '广告横幅' },
  { value: 4, unionType: 1, name: '精品类目' },
  { value: 5, unionType: 1, name: '活动弹窗' },
  { value: 6, unionType: 1, name: '首页图片' },
  { value: 7, unionType: 1, name: '分类图片' },
  { value: 8, unionType: 1, name: '个人中心' },
  { value: 9, unionType: 1, name: '特价列表' }
]
const adStatusMap = [{ value: 0, name: '冻结' }, { value: 1, name: '激活' }, { value: '', name: '全部' }]

export default {
  name: 'Ad',
  components: {
    ElOption,
    Pagination
  },
  filters: {
    adTypeFilter(code) {
      return adTypeMap[code - 1]['name']
    },
    adStatusFilter(code) {
      return adStatusMap[code]['name']
    }
  },
  data() {
    return {
      uploadPath,
      options: [],
      value: [],
      list: [],
      total: 0,
      listLoading: true,
      linkUnion: undefined,
      adTypeMap,
      adStatusMap,
      listQuery: {
        pageNo: 1,
        limit: 20,
        status: undefined,
        adType: undefined
      },
      dataForm: {
        id: undefined,
        adType: undefined,
        title: undefined,
        url: '',
        imgUrl: undefined,
        status: undefined,
        color: undefined,
        outUrl: ''
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑',
        create: '创建'
      },
      rules: {
        title: [{ required: true, message: '广告标题不能为空', trigger: 'blur' }],
        imgUrl: [{ required: true, message: '广告图片不能为空', trigger: 'blur' }],
        adType: [{ required: true, message: '请选择广告类型', trigger: 'blur' }],
        status: [{ required: true, message: '请选择广告状态', trigger: 'blur' }]
      },
      downloadLoading: false,
      userLinkUnion: '',
      checkUserValue: '',
      useUser: false,
      optionArray: []
    }
  },
  computed: {
    headers() {
      return {
        accessToken: getToken()
      }
    }
  },
  watch: {
    linkUnion(newV, oldV) {
      if (newV.length === 0) {
        this.userLinkUnion = ''
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      listAd(this.listQuery)
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
    handleLink(e) {
      if (e === undefined || e === null || e.length === 0) {
        return
      }
      const tag = e[e.length - 1]
      let url = ''
      if (tag.startsWith('C')) {
        if (e.length < 2) {
          this.$notify.error({
            title: '提示',
            message: '请关联第二级类目或者商品'
          })
          return
        }
        url = '/pages/product/list?tid=' + e[1].substring(2)
      } else {
        url = '/pages/product/detail?id=' + (e[2].substring(2))
      }
      this.dataForm.url = url
      // 将修改的值反映到 userLinkUnion中
      this.userLinkUnion = this.getOptionName(e[0]) + ' / ' + this.getOptionName(e[1])
      this.checkUserValue = this.userLinkUnion
      this.useUser = false
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetForm() {
      this.dataForm = {
        id: undefined,
        adType: this.listQuery.adType,
        title: undefined,
        url: '',
        imgUrl: undefined,
        status: undefined,
        color: undefined,
        userLinkUnion: '',
        outUrl: ''
      }
      this.linkUnion = undefined
    },
    userValueChange(e) {
      // 用户自己输入了值 且值不等于上次通过下拉框选择的值
      if (e !== this.checkUserValue) {
        this.useUser = true
        this.dataForm.url = this.userLinkUnion
      }
    },
    leaveOption(datas) {
      const res = []
      function leave(data) {
        data.forEach(item => {
          if (item.children && item.children.length > 0) {
            leave(item.children)
          }
          res.push({ label: item.label, value: item.value })
        })
      }
      leave(datas)
      return res
    },
    getOptionName(id) {
      let res = ''
      this.optionArray.forEach(item => {
        if (item.value === id) {
          res = item.label
        }
      })
      return res
    },
    refreshOptions() {
      if (this.options.length === 0) {
        spuTree().then(response => {
          this.options = clearTreeEmptyChildren(response.data.data)
          this.optionArray = this.leaveOption(this.options)
        })
      }
    },
    handleCreate() {
      this.resetForm()
      this.refreshOptions()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    uploadUrl: function(response) {
      this.dataForm.imgUrl = response.url
    },
    createData() {
      this.dataForm.url = this.userLinkUnion
      this.$refs['dataForm'].validate(valid => {
        if (valid && this.checkAdType()) {
          createAd(this.dataForm)
            .then(response => {
              this.getList()
              this.dialogFormVisible = false
              this.$notify.success({
                title: '成功',
                message: '创建成功'
              })
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              })
            })
        }
      })
    },
    checkAdType() {
      // 如果是用户自己填的活动链接 则不需要经过验证
      // if (this.useUser) return true
      // // 检测关联选项是否是三级目录或商品
      // if (this.linkUnion === undefined || this.linkUnion === null || this.linkUnion.length < 2) {
      //   this.$notify.error({
      //     title: '失败',
      //     message: '请关联二级目录或者商品'
      //   })
      //   return false
      // }
      //
      // for (let i = 0; i < this.adTypeMap.length; i++) {
      //   const item = this.adTypeMap[i]
      //   if (item.value === this.dataForm.adType) {
      //     if (item.unionType === 1 && this.linkUnion.length === 4) {
      //       this.$notify.error({
      //         title: '失败',
      //         message: '此类广告只能关联二级类目'
      //       })
      //       return false
      //     } else if (this.unionType === 2 && this.linkUnion.length === 3) {
      //       this.$notify.error({
      //         title: '失败',
      //         message: '此类广告只能关联商品'
      //       })
      //       return false
      //     }
      //   }
      // }
      return true
    },
    // 点击编辑按钮时的处理
    handleUpdate(row) {
      this.dataForm = Object.assign({}, row)
      this.refreshOptions()
      this.dialogStatus = 'update'
      if (this.dataForm.url.indexOf('tid') >= 0) {
        this.linkUnion = 'C_' + this.dataForm.url.replace(/[^0-9]/ig, '')
      } else { this.linkUnion = 'G_' + this.dataForm.url.replace(/[^0-9]/ig, '') }
      setTimeout(() => {
        // 分类的获取是异步的
        const name = this.getOptionName(this.linkUnion)
        if (name) {
          this.userLinkUnion = name
        } else {
          this.userLinkUnion = this.dataForm.url
        }
      }, 300)
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    updateData() {
      this.$refs['dataForm'].validate(valid => {
        if (valid && this.checkAdType()) {
          updateAd(this.dataForm)
            .then(() => {
              for (const v of this.list) {
                if (v.id === this.dataForm.id) {
                  const index = this.list.indexOf(v)
                  this.list.splice(index, 1, this.dataForm)
                  break
                }
              }
              this.dialogFormVisible = false
              this.$notify.success({
                title: '成功',
                message: '更新广告成功'
              })
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              })
            })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('此操作将永久删除该广告--' + row.title + '--, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAd(row.id, row.adType)
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
    // 上传图片了处理图片
    uploadSuccessHandle(e, file) {
      const that = this
      this.dataForm.imgUrl = e.url
      this.dialogFormVisible = false
      this.dialogFormVisible = true
      const img = new Image()
      // 加载完成执行
      img.src = e.url
      img.setAttribute('crossOrigin', 'anonymous')
      img.src = e.url + '?time=' + new Date().valueOf()
      img.onload = function(e) {
        var canvas = that.$refs.canvas
        that.dataForm.color = getImageColor(canvas, img)
      }
    },
    onBeforeUpload(file) {
      const isIMAGE = file.type === 'image/jpeg' || 'image/gif' || 'image/png' || 'image/jpg'
      const isLt1M = file.size / 1024 / 1024 < 1

      if (!isIMAGE) {
        this.$message.error('上传文件只能是图片格式!')
      }
      if (!isLt1M) {
        this.$message.error('上传文件大小不能超过 1MB!')
      }
      return isIMAGE && isLt1M
    }
  }
}
</script>
