<template>
  <div class="container">
    <div class="row justify-content-md-center">
      <div class="col-md-9 col-xl-7">
        <div class="card-header px-0 mt-2 bg-transparent clearfix">
          <h4 class="float-left pt-2">
            <i class="card-icon fas fa-charging-station"></i> Activation
          </h4>
          <div class="card-header-actions mr-1">
            <a
              class="btn btn-primary"
              href="#"
              :disabled="submiting"
              @click.prevent="activate"
            >
              <i class="fas fa-spinner fa-spin" v-if="submiting"></i>
              <i class="fas fa-check" v-else></i>
              <span class="ml-1">Activate</span>
            </a>
          </div>
        </div>
        <div class="card-body px-0">
          <form class="form-horizontal" v-show="!loading">
            <hr class="my-3" />
            <div class="form-group row justify-content-md-center">
              <label class="col-md-3"
                >Secure Key
                <a href="https://auth.creativeapps.info/" target="_blank"
                  ><span class="badge badge-info">?</span></a
                ></label
              >
              <div class="col-md-9">
                <input
                  class="form-control"
                  :class="{ 'is-invalid': errors.secure_key }"
                  type="text"
                  v-model="secure_key"
                  placeholder="Enter secure key"
                />
                <div class="invalid-feedback" v-if="errors.secure_key">
                  {{ errors.secure_key[0] }}
                </div>
              </div>
            </div>
          </form>
          <div class="row justify-content-md-center" v-show="loading">
            <div class="col-md-12">
              <content-placeholders>
                <content-placeholders-text />
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
  data() {
    return {
      secure_key: null,
      errors: {},
      loading: false,
      submiting: false,
    };
  },
  components: {},
  mounted() {
      this.getAuth();
  },
  methods: {
    refresh(milliseconds) {
        console.log("Refreshing");
        setTimeout("location.reload(true);", milliseconds);
    },
    activate() {
      if (!this.submiting) {
        this.submiting = true;
        axios
          .post("/api/activation/activate", {'secure_key' : this.secure_key})
          .then(response => {
              console.log(response);
            this.$toasted.global.error("Thank you for activating your item!");
            this.submiting = false;

            this.refresh(500); //refreshes after .5 second
            
          })
          .catch((error) => {
            this.submiting = false;
            this.$toasted.global.error("Error in activating your license!");
            console.log(error);
            this.errors = error.response.data;
          });
      }
    },
    getAuth() {
      this.loading = true;
      axios
        .get("/api/activation/")
        .then((response) => {
            this.secure_key = response.data.secure_key;
            console.log(this.secure_key);
          this.loading = false;
        })
        .catch((error) => {
          this.loading = false;
          this.$toasted.global.error("Error in retrieving your license!");
          console.log(error);
          this.errors = error.response.data;
        });
    },
  },
};
</script>
