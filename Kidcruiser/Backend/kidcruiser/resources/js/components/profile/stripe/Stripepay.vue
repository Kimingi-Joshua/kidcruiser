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
          <div class="card-header text-value-sm text-dark py-2">Enter your credit card information</div>
          <div class="card-body">
              <div ref="payment_element">
                <!-- Elements will create form elements here -->
              </div>
              <div class="card-footer py-2 d-flex justify-content-center align-items-center">
                <button class="btn btn-primary" v-on:click="purchase">
                  Pay
                  <i class="fas fa-spinner fa-spin" v-if="submiting"></i>
                </button>
              </div>
              <div v-html="error_message">
                <!-- Display error message to your customers here -->
              </div>
            <!-- Used to display form errors. -->
            <div class="invalid mt-2" role="alert" v-if="errors">{{errors}}</div>
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
    stripe_publishable_key: String,
    client_secret: String
  },
  data() {
    return {
      stripe: "",
      options:{},
      elements:{},
      paymentElement: "",
      currency: {},
      error_message: "",
      plan: {},
      loading: true,
      submiting: false,
      errors: ""
    };
  },
  mounted() {
    this.loading = true;
    this.stripe = Stripe(this.stripe_publishable_key);
    this.options = {
      clientSecret: this.client_secret,
    };
    this.elements = this.stripe.elements(this.options);
    // Create and mount the Payment Element
    this.paymentElement = this.elements.create('payment');
    this.paymentElement.mount(this.$refs.payment_element);
    this.getPlan();
  },
  methods: {
    async purchase() {
      if (!this.submiting) {
        this.submiting = true;
        let self = this;
        let elements = this.elements;
        let returnurl = window.location.origin+'/plan';
        const {error} = await this.stripe.confirmPayment(
          {
            //`Elements` instance that was used to create the Payment Element
            elements,
            confirmParams: {
              return_url: returnurl,
            },
          }
        );

        if (error) {
          // This point will only be reached if there is an immediate error when
          // confirming the payment. Show error to your customer (for example, payment
          // details incomplete)
          console.log(error)
          this.error_message = error.message;
          this.submiting = false;
        }
        // this.stripe.createPaymentMethod({
        //   type: 'card',
        //   card: this.card,
        // }).then(function(result) {
        //   if (result.error) {
        //     self.hasCardErrors = true;
        //     self.errors = result.error.message;
        //     self.submiting = false;
        //     self.$toasted.global.error("Error!");
        //   } else {
        //     self.errors = "";
        //     //submit paymentMethodId to server
        //     self.submitPaymentMethod(result.paymentMethod.id);
        //   }
        // });
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
  }
};
</script>