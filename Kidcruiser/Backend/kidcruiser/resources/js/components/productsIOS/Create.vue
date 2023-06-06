<template>
  <div class="container">
    <div class="row justify-content-md-center">
      <div class="col-md-9 col-xl-7" v-if="!loading">
        <div class="card-header px-0 mt-2 bg-transparent clearfix">
          <h4 class="float-left pt-2">New iOS Product</h4>
          <div class="card-header-actions mr-1">
            <a class="btn btn-primary" href="#" :disabled="submiting" @click.prevent="create">
              <i class="fas fa-spinner fa-spin" v-if="submiting"></i>
              <i class="fas fa-check" v-else></i>
              <span class="ml-1">Save</span>
            </a>
          </div>
        </div>
        <div class="card-body px-0">
          <div class="row">
            <div class="form-group col-md-12">
              <label>Name</label>
              <input type="text" class="form-control" :class="{'is-invalid': errors.name}" 
              v-model="product.name" placeholder="Enter product name as in iTunes">
              <div class="invalid-feedback" v-if="errors.name">{{errors.name[0]}}</div>
            </div>
            <div class="col-md-12">
              <div class="form-group">
                <label>Price</label>
                <input type="text"
                class="form-control" :class="{'is-invalid': errors.price}" v-model="product.price" 
                placeholder="Enter price name as in iTunes">
                <div class="invalid-feedback" v-if="errors.price">{{errors.price[0]}}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-9 col-xl-7" v-if="loading">
        <content-placeholders>
          <content-placeholders-text/>
        </content-placeholders>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data () {
    return {
      product: {},
      errors: {},
      submiting: false,
      loading: true,
    }
  },
  mounted () {
      axios.post(`/api/ios-products/filter`)
      .then(response => {
        this.products = response.data.products.data
        this.loading = false
      })
  },
  methods: {
    create () {
      if (!this.submiting) {
        this.submiting = true
        axios.post(`/api/ios-products/store`, this.product)
        .then(response => {
          console.log(response);
          this.$toasted.global.error('Created product!')
          location.href = '/ios-products'
        })
        .catch(error => {
          this.$toasted.global.error('Error in creating product!')
          this.errors = error.response.data.errors
          this.submiting = false
          if(error.response.data.errors.Product[0])
          {
            swal("Error", error.response.data.errors.Product[0], "error")
          }
        })
      }
    },
  }
}
</script>
