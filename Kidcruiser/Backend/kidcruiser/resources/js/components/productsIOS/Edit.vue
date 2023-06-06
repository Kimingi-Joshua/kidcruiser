<template>
  <div class="container">
    <div class="row justify-content-md-center">
      <div class="col-md-9 col-xl-7">
        <div class="card-header px-0 mt-2 bg-transparent clearfix">
          <h4 class="float-left pt-2">Edit iOS Product</h4>
          <div class="card-header-actions mr-1">
            <a class="btn btn-primary" href="#"
            :disabled="submiting" @click.prevent="update">
              <i class="fas fa-spinner fa-spin" v-if="submiting"></i>
              <i class="fas fa-check" v-else></i>
              <span class="ml-1">Save</span>
            </a>
            <a
              class="card-header-action ml-1"
              href="#"
              :disabled="submitingDestroy"
              @click.prevent="destroy"
            >
              <i class="fas fa-spinner fa-spin" v-if="submitingDestroy"></i>
              <i class="far fa-trash-alt" v-else></i>
              <span class="d-md-down-none ml-1">Delete</span>
            </a>
          </div>
        </div>
        <div class="card-body px-0">
          <div class="row" v-if="!loading">
            <div class="form-group col-md-9">
              <label>Name</label>
              <input type="text" class="form-control" :class="{'is-invalid': errors.name}" 
              v-model="product.name" placeholder="Enter product name">
              <div class="invalid-feedback" v-if="errors.name">{{errors.name[0]}}</div>
            </div>
            <div class="form-group col-md-3">
              <label>ID</label>
              <input class="form-control" type="text" v-model="product.id" readonly>
            </div>
            <div class="col-md-12">
              <div class="form-group">
                <label>Price</label>
                <input type="text"
                class="form-control" :class="{'is-invalid': errors.price}" 
                v-model="product.price" 
                placeholder="product price as in iTunes">
                <div class="invalid-feedback" v-if="errors.price">{{errors.price[0]}}</div>
              </div>
            </div>
          </div>
          <div class="row" v-else>
            <div class="col-md-12">
              <content-placeholders>
                <content-placeholders-text/>
              </content-placeholders>
            </div>
          </div>
        </div>
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
      loading: true,
      submiting: false,
      submitingDestroy : false,
    }
  },
  mounted () {
    this.getProduct()
  },
  methods: {
    getProduct() {
      this.loading = true
      let str = window.location.pathname
      let res = str.split("/")
      axios.get(`/api/ios-products/${res[2]}`)
      .then(response => {
        this.product = response.data.product
      })
      .catch(error => {
        this.$toasted.global.error('Product does not exist!')
        location.href = '/plans'
      })
      .then(() => {
        this.loading = false
      })
    },
    update () {
      if (!this.submiting) {
        this.submiting = true
        axios.put(`/api/ios-products/update/${this.product.id}`, this.product)
        .then(response => {
          this.$toasted.global.error('Product updated!')
          this.submiting = false
          location.href = '/ios-products'
        })
        .catch(error => {
          this.submiting = false
          this.$toasted.global.error('Error in updating product')
          this.errors = error.response.data.errors
          swal("Error", error.response.data.errors.Product[0], "error")
        })
      }
    },
    destroy() {
      if (!this.submitingDestroy) {
        this.submitingDestroy = true;
        swal({
          title: "Are you sure?",
          text: "If you delete this product, you will not be able to recover it!",
          icon: "warning",
          buttons: true,
          dangerMode: true
        }).then(willDelete => {
          if (willDelete) {
            axios
              .delete(`/api/ios-products/${this.product.id}`)
              .then(response => {
                this.submitingDestroy = false;
                this.$toasted.global.error("Deleted product!");
                location.href = "/ios-products";
              })
              .catch(error => {
                this.submitingDestroy = false;
                this.errors = error.response.data.errors;
                if(error.response.data.errors.Product[0])
                {
                  swal("Error", error.response.data.errors.Product[0], "error")
                }
              });
          }
          else
            this.submitingDestroy = false;
          
        });
      }
    }
  }
}
</script>
