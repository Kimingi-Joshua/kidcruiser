<template>
  <div class="container">
    <div class="row justify-content-md-center">
      <div class="col-md-9 col-xl-7" v-if="!loading">
        <div class="card-header px-0 mt-2 bg-transparent clearfix">
          <h4 class="float-left pt-2" v-if="plan.is_addon">Pay {{plan.addon_price}} ({{currency}}) as addon for "{{plan.name}}" plan</h4>
          <h4 class="float-left pt-2" v-else>Pay {{plan.price}} ({{currency}}) for "{{plan.name}}" plan</h4>
        </div>
      </div>
      <div class="col-md-9 col-xl-7" v-else>
        <content-placeholders>
          <content-placeholders-text />
        </content-placeholders>
      </div>
    </div>
    <div class="row justify-content-md-center py-4" v-show="!loading">
      <div class="col-sm-12 col-lg-6">
        <div class="card">
          <div class="card-footer py-2 d-flex justify-content-center align-items-center">
              <div id="paypal-button-container"></div>
          </div>
        </div>
      </div>
    </div>
    <div class="row justify-content-md-center py-4" v-show="loading">
      <div class="col-sm-12 col-lg-6">
        <content-placeholders :rounded="true">
          <content-placeholders-img />
        </content-placeholders>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      card: "",
      currency: {},
      error_message: "",
      plan: {},
      loading: true,
      submiting: false,
      submiting_pay: false,
      errors: ""
    };
  },
  mounted() {
    this.getPlan();
  },
  methods: {
    getPlan() {
      this.loading = true;
      let str = window.location.pathname;
      let res = str.split("/");
      axios
        .get(`/api/profile/getPlan/${res[2]}`)
        .then(response => {
          console.log(response.data);
          this.plan = response.data.plan;
          this.currency = response.data.currency;
        })
        .catch(error => {
          this.$toasted.global.error("Plan does not exist!");
          location.href = "/plan";
        })
        .then(() => {
          this.loading = false;
        });
    },
  }
};
</script>