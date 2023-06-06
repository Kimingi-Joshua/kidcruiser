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
          <div class="card-header text-value-sm text-dark py-2">Click Pay to enter your credit card information</div>
          <div class="card-footer py-2 d-flex justify-content-center align-items-center">
            <button class="btn btn-primary" v-on:click="purchase">
              Pay 
              <i class="fas fa-spinner fa-spin" v-if="submiting_pay"></i>
              </button>
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
  props: {
    razorpay_key_id: String,
    order_id: String,
    email: String
  },
  data() {
    return {
      razorpay: {},
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
    async purchase() {
      if (!this.submiting) {
        this.submiting = true;
        let self = this;

        var options = {
          key: this.razorpay_key_id, // Enter the Key ID generated from the Dashboard
          amount: this.plan.price * 100, // Amount is in currency subunits. Default currency is INR. Hence, 50000 refers to 50000 paise or INR 500.
          currency: this.currency,
          order_id: this.order_id,
          handler: function(response) {
            //send to server to verify and complete
            //alert(response.razorpay_payment_id);
            self.submitRazorpayPaymentId(response.razorpay_payment_id)
          },
          prefill: {
            email: this.email
          }
        };

        this.razorpay = new Razorpay(options);

        this.razorpay.open();
      }
    },
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
    submitRazorpayPaymentId(payment_id) {
      this.submiting_pay = true;
      axios
        .post("/api/profile/updatePayment", {
          payment_id: payment_id,
          plan: this.plan.id,
          order_id: this.order_id,
        })
        .then(response => {
          this.$toasted.global.error("Payment succeeded!");
          this.submiting = false;
          this.submiting_pay = false;
          location.href = "/plan";
        })
        .catch(error => {
          this.$toasted.global.error("Errors in payment!");
          this.submiting = false;
          this.submiting_pay = false;
          if (error.response.data.errors.Payment[0]) {
            swal(
              "Payment Error",
              error.response.data.errors.Payment[0],
              "error"
            );
          }
        });
    }
  }
};
</script>