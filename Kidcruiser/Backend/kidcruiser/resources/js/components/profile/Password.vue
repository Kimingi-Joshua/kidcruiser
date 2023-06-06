<template>
  <div class="container">
    <div class="row justify-content-md-center">
      <div class="col-md-9 col-xl-7">
        <div class="card-header px-0 mt-2 bg-transparent clearfix">
          <h4 class="float-left pt-2"><i class="card-icon fas fa-key"></i> Password</h4>
          <div class="card-header-actions mr-1">
            <a class="btn btn-primary" href="#" :disabled="submiting" @click.prevent="updatePasswordAuthUser">
              <i class="fas fa-spinner fa-spin" v-if="submiting"></i>
              <i class="fas fa-check" v-else></i>
              <span class="ml-1">Save</span>
            </a>
          </div>
        </div>
        <div class="card-body px-0">
          <form class="form-horizontal">
            <div class="form-group row justify-content-md-center">
              <label class="col-md-3">Current password</label>
              <div class="col-md-9">
                <passwordField :error="errors.current" :password="password.current" @input="(value) => password.current = value" />
                <small class="form-text text-muted">You must provide your current password in order to change it.</small>
              </div>
            </div>
            <div class="form-group row justify-content-md-center">
              <label class="col-md-3">New password</label>
              <div class="col-md-9">
                <passwordField :error="errors.password" :password="password.password" @input="(value) => password.password = value" />
              </div>
            </div>
            <div class="form-group row justify-content-md-center">
              <label class="col-md-3">Password confirmation</label>
              <div class="col-md-9">
                <passwordField :error="errors.password_confirmation" :password="password.password_confirmation" @input="(value) => password.password_confirmation = value" />
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import passwordField from '../password-field.vue';

export default {
  components: {
      passwordField
  },
  data () {
    return {
      password: {},
      errors: {},
      submiting: false,
      showPassword: false,
    }
  },
  methods: {
    toggleShow() {
      this.showPassword = !this.showPassword;
    },
    aaa(e)
    {
      console.log(e)
    },
    updatePasswordAuthUser () {
      if (!this.submiting) {
        this.submiting = true
        axios.put(`/api/profile/updatePasswordAuthUser`, this.password)
        .then(response => {
          this.password = {}
          this.errors = {}
          this.submiting = false
          this.$toasted.global.error('Password changed!');
        })
        .catch(error => {
          this.errors = error.response.data.errors
          this.submiting = false
          swal("Error", error.response.data.errors[0], "error")
        })
      }
    }
  }
}
</script>
