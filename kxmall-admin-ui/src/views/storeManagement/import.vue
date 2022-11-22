<template>
  <div class="main">
    <a
      v-show="false"
      ref="download"
      href="" />
    <a
      v-show="false"
      ref="clear"
      href="" />
    <el-button
      type="primary"
      @click="importExcel">excel模板导出</el-button>
    <el-button type="primary" @click="dialogVisible=true">excel导入</el-button>
    <el-button type="primary" @click="exportError">清空错误商品并导出</el-button>
    <el-table
      :data="tableData"
      border
      style="width: 100%; margin-top:20px">
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
        label="商品名称"/>
      <el-table-column
        prop="skuDTO.title"
        align="center"
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
        prop="skuDTO.unit.op"
        label="原价">
        <template slot-scope="scope">
          <span type="text">{{ scope.row.spuDO.originalPrice/100 }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="当前售价">
        <template slot-scope="scope">
          <span>{{ scope.row.price/100 }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="折扣">
        <template slot-scope="scope">
          <span>{{ scope.row.skuDTO.discount }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column
        prop="address"
        align="center"
        label="备注" /> -->
    </el-table>
    <!-- <div class="page">
      <el-button
        size="200"
        style="width:150px">取消</el-button>
      <el-button
        size="200"
        style="width:150px"
        type="primary">保存</el-button>
    </div> -->
    <el-dialog
      :visible.sync="dialogVisible"
      :before-close="handleClose"
      width="400px">
      <el-upload
        :multiple="false"
        :auto-upload="true"
        :show-file-list="true"
        :drag="true"
        :limit="1"
        :on-exceed="handleExceed"
        :http-request="uploadFile"
        class="image-uploader"
        accept=".xls,.xlsx"
        action="">
        <i class="el-icon-upload" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div
          slot="tip"
          class="el-upload__tip">一次只能上传一个文件，仅限xls/xlsx格式，单文件不超过1MB</div>
      </el-upload>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
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
    }
  },
  data() {
    return {
      dialogVisible: false,
      tableData: []
    }
  },
  methods: {
    importExcel() {
      this.$message.success('已添加到下载任务')
      this.$refs.download.href = process.env.HOST + `/stock/export`
      setTimeout(
        this.$refs.download.click(), 300
      )
    },
    exportError() {
      this.$refs.clear.href = process.env.HOST + `/stock/exportErrorData`
      this.$refs.clear.click()
    },

    // 上传文件之前的钩子
    // beforeUpload(file) {
    //   console.log('beforeUpload')
    //   console.log(file.type)
    //   const isText = file.type === 'application/vnd.ms-excel'
    //   const isTextComputer = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    //   return (isText | isTextComputer)
    // },
    // 上传文件个数超过定义的数量
    handleExceed(files, fileList) {
      this.$message.warning(`当前限制选择 1 个文件，请删除后继续上传`)
    },
    // 上传文件
    uploadFile(item) {
      console.log(item)
      const fileObj = item.file
      // FormData 对象
      const form = new FormData()
      console.log(fileObj)
      // 文件对象
      form.append('file', fileObj)
      console.log(JSON.stringify(form))
      console.log(form)
      axios.post(process.env.HOST + '/stock/insertBatch', form).then(res => {
        console.log(res)
        this.tableData = res.data.data
        this.$message.info('文件：' + fileObj.name + '上传成功')
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
  margin-top: 100px;
}

</style>
