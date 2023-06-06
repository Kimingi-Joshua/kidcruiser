<template>
  <div class="container">
    <div class="card-header px-0 mt-2 bg-transparent clearfix">
      <h4 class="float-left pt-2">
        <i class="card-icon fas fa-trophy"></i> Current plan
      </h4>
    </div>
    <div class="row justify-content-md-center py-4">
      <div class="col-sm-6 col-xl-4" v-if="!loading">
        <div class="card">
          <div class="card-body p-3 d-flex align-items-center">
            <div>
              <div class="text-value-sm text-dark">{{ school.plan.name }}</div>
              <div
                v-if="school.plan.is_pay_as_you_go == 1"
                class="text-muted font-weight-bold py-1 pt-3"
              >
                Price per {{ billing_cycle }} (per child):
                {{ school.plan.price + " " + currency }}
              </div>
              <div v-else class="text-muted font-weight-bold py-1 pt-3">
                Price per {{ billing_cycle }}:
                {{
                  school.plan.is_free == 1
                    ? "Free"
                    : school.plan.price + " " + currency
                }}
              </div>

              <div
                v-if="school.plan.is_pay_as_you_go != 1"
                class="text-muted font-weight-bold py-1"
              >
                Maximum number of drivers:
                {{
                  school.plan.allowed_drivers == -1
                    ? "Unlimited"
                    : school.plan.allowed_drivers
                }}
              </div>

              <div
                v-if="school.plan.is_pay_as_you_go != 1"
                class="text-muted font-weight-bold py-1"
              >
                Maximum number of seats:
                {{
                  school.plan.allowed_children == -1
                    ? "Unlimited"
                    : school.plan.allowed_children
                }}
              </div>

              <div
                class="text-muted font-weight-bold py-1"
                v-if="
                  school.plan.is_free != 1 && school.plan.is_pay_as_you_go != 1
                "
              >
                Renews on: {{ school.plan_renews_at }}
              </div>
            </div>
          </div>
          <div class="card-footer px-3 py-2">
            <div
              class="
                btn-block
                text-success
                d-flex
                justify-content-center
                align-items-center
              "
            >
              <i class="fas fa-check"></i>
              <span class="font-weight-bold px-2">Current plan</span>
            </div>
            <div
              v-if="checkRenew()"
              class="
                card-footer
                p-4
                d-flex
                justify-content-center
                align-items-center
              "
            >
              <button
                v-if="is_payment_enabled"
                class="btn btn-block btn-primary p-4"
                v-on:click="selectPlan(school.plan.id, true)"
              >
                Renew
                <i class="fas fa-spinner fa-spin" v-if="submiting"></i>
              </button>
              <button
                v-else
                class="btn btn-block btn-secondary p-4"
                v-on:click="selectPlan(school.plan.id)"
              >
                How to renew
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="card col-sm-6 col-xl-4" v-if="!loading && school.plan.is_custom == 1">
        <div class="card-body p-3">
          <div>
            <div class="text-value-sm text-dark">Adjust your custom plan</div>
          </div>
          <div class="text-muted font-weight-bold py-1 pt-3"
          >
          You can add more drivers and seats to your custom plan. Just click on the button below and we will take care of the rest.
          </div>
        </div>

        <div
          class="
            card-footer
            p-4
            d-flex
            justify-content-center
            align-items-center
          "
        >
          <button
            v-if="
              is_payment_enabled
            "
            class="btn btn-block btn-primary p-4"
            v-on:click="addonCustomPlan()"
          >
            Add drivers and seats
          </button>
          <button
            v-else
            class="btn btn-block btn-secondary p-4"
          >
            No payments available
          </button>
        </div>
      </div>
      <div class="col-sm-6 col-xl-4" v-if="loading">
        <content-placeholders :rounded="true">
          <content-placeholders-img />
        </content-placeholders>
      </div>
    </div>

    <div class="card-header px-0 mt-2 bg-transparent clearfix">
      <h4 class="float-left">
        <i class="card-icon fas fa-pencil-alt"></i> Change plan
      </h4>
    </div>
    <h6 class="p-4"> You can either create a custom plan or choose one from below</h6>
    <div>
    <div class="row justify-content-md-center p-3">
    <div class="card col-sm-6 col-xl-4">
      <div class="card-body p-3">
        <div>
          <div class="text-value-sm text-dark">Create custom plan</div>
        </div>
        <div class="text-muted font-weight-bold py-1 pt-3"
        >
              <div
                class="text-muted font-weight-bold py-1"
              >
                Price per driver:
                {{price_per_driver + " " + currency}}
              </div>
              <div
                class="text-muted font-weight-bold py-1"
              >
                Price per seat (child):
                {{price_per_seat + " " + currency}}
              </div>
        </div>
      </div>

      <div
        class="
          card-footer
          p-4
          d-flex
          justify-content-center
          align-items-center
        "
      >
        <button
          v-if="
            is_payment_enabled
          "
          class="btn btn-block btn-primary p-4"
          v-on:click="designCustomPlan()"
        >
          Design custom plan
        </button>
        <button
          v-else
          class="btn btn-block btn-secondary p-4"
        >
          No payments available
        </button>
      </div>
    </div>
        
    </div>
    <hr/>
    </div>
    <div class="row justify-content-md-center py-4">
      <div
        v-for="plan in plans"
        v-if="!loading"
        v-bind:class="{ 'col-sm-6 col-xl-4': school.plan_id != plan.id }"
      >
        <div class="card" v-if="school.plan_id != plan.id">
          <div class="card-body p-3 d-flex align-items-center">
            <div>
              <div class="text-value-sm text-dark">{{ plan.name }}</div>
              <div
                v-if="plan.is_pay_as_you_go == 1"
                class="text-muted font-weight-bold py-1 pt-3"
              >
                Price per {{ billing_cycle }} (per child):
                {{ plan.price + " " + currency }}
              </div>
              <div v-else class="text-muted font-weight-bold py-1 pt-3">
                Price per {{ billing_cycle }}:
                {{ plan.price == 0 ? "Free" : plan.price + " " + currency }}
              </div>
              <div
                v-if="plan.is_pay_as_you_go != 1"
                class="text-muted font-weight-bold py-1"
              >
                Maximum number of drivers:
                {{
                  plan.allowed_drivers == -1
                    ? "Unlimited"
                    : plan.allowed_drivers
                }}
              </div>
              <div
                v-if="plan.is_pay_as_you_go != 1"
                class="text-muted font-weight-bold py-1"
              >
                Maximum number of seats:
                {{
                  plan.allowed_children == -1
                    ? "Unlimited"
                    : plan.allowed_children
                }}
              </div>
            </div>
          </div>
          <div
            class="
              card-footer
              p-4
              d-flex
              justify-content-center
              align-items-center
            "
          >
            <button
              v-if="
                is_payment_enabled ||
                plan.is_free == 1
              "
              class="btn btn-block btn-primary p-4"
              v-on:click="selectPlan(plan.id)"
            >
              Select
              <i class="fas fa-spinner fa-spin" v-if="submiting"></i>
            </button>
            <button
              v-else
              class="btn btn-block btn-secondary p-4"
              v-on:click="selectPlan(plan.id)"
            >
              How to change
            </button>
          </div>
        </div>
      </div>
      <div class="col-sm-6 col-xl-4" v-else>
        <content-placeholders :rounded="true">
          <content-placeholders-img />
        </content-placeholders>
      </div>
    </div>

<!-- Custom Plan Modal -->
<div class="modal fade" id="customPlanModal" ref="customPlanModal" tabindex="-1" role="dialog" 
aria-labelledby="customPlanModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="exampleModalLongTitle">Create Your Own Plan</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label for="plan_name">Plan Name</label>
          <input type="text" class="form-control" id="plan_name" v-model="customPlan.name">
        </div>
        <div class="form-group">
          <label for="plan_allowed_drivers">Maximum number of drivers</label>
          <input type="number" class="form-control" id="plan_allowed_drivers" v-model="customPlan.allowed_drivers">
        </div>
        <div class="form-group">
          <label for="plan_allowed_children">Maximum number of seats</label>
          <input type="number" class="form-control" id="plan_allowed_children" v-model="customPlan.allowed_children">
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn-secondary" data-dismiss="modal">Cancel</button>
        <button type="button" class="btn-primary" @click.prevent="createCustomPlan">
          <i class="fas fa-spinner fa-spin" v-if="submitingCustomPlan"></i>
          OK
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Addon Custom Plan Modal -->
<div class="modal fade" id="addonCustomPlanModal" ref="addonCustomPlanModal" tabindex="-1" role="dialog" 
aria-labelledby="addonCustomPlanModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="exampleModalLongTitle">Create Your Own Plan</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label for="plan_allowed_drivers">Number of drivers to add</label>
          <input type="number" class="form-control" id="plan_allowed_drivers" v-model="addon_drivers">
        </div>
        <div class="form-group">
          <label for="plan_allowed_children">Number of seats to add</label>
          <input type="number" class="form-control" id="plan_allowed_children" v-model="addon_children">
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn-secondary" data-dismiss="modal">Cancel</button>
        <button type="button" class="btn-primary" @click.prevent="addToCustomPlan">
          <i class="fas fa-spinner fa-spin" v-if="submittingAddonCustomPlan"></i>
          OK
        </button>
      </div>
    </div>
  </div>
</div>
  </div>
</template>

<script>
import avatar from "./Avatar.vue";

export default {
  data() {
    return {
      plans: [],
      school: [],
      errors: {},
      loading: true,
      submiting: false,
      is_payment_enabled: "",
      currency: {},
      billing_cycle: {},
      customPlan: {
        name: "",
        allowed_drivers: "",
        allowed_children: ""
      },
      submitingCustomPlan: false,
      addon_drivers: "",
      addon_children: "",
      submittingAddonCustomPlan: false,
      price_per_driver: '',
      price_per_seat: '',
    };
  },
  components: {
    avatar,
  },
  created() {
    const clientSecret = new URLSearchParams(window.location.search).get(
      "payment_intent_client_secret"
    );
    const paymentIntent = new URLSearchParams(window.location.search).get(
      "payment_intent"
    );
    this.submitPaymentMethod(paymentIntent, clientSecret);
  },
  mounted() {
    this.getAuthUser();
  },
  methods: {
    checkRenew() {
      if (this.school.plan_renews_at) {
        let today = new Date();
        let renew_date = Date.parse(this.school.plan_renews_at);
        return renew_date <= today;
      }
      return false;
    },
    submitPaymentMethod(paymentIntent, clientSecret) {
      if(paymentIntent != null && clientSecret!= null)
      {
      axios
        .post("/api/profile/updatePayment", {
          paymentIntent: paymentIntent,
          clientSecret: clientSecret,
        })
        .then((response) => {
          this.$toasted.global.error("Payment method updated!");
          this.submiting = false;
          location.href = "/plan";
        })
        .catch((error) => {
          this.submiting = false;
          if (error.response.data.errors.Payment[0]) {
            swal(
              "Payment Error",
              error.response.data.errors.Payment[0],
              "error"
            );
          }
        });
      }
    },
    getAuthUser() {
      this.loading = true;
      axios.get(`/api/profile/getAuthUser`).then((response) => {
        this.school = response.data;
        this.getPlans();
        this.loading = false;
      });
    },
    getPlans() {
      this.loading = true;
      axios.get(`/api/plans/getPlans`).then((response) => {
        console.log(response.data);
        this.plans = response.data.plans;
        this.currency = response.data.currency;
        this.billing_cycle = response.data.billing_cycle;
        this.price_per_driver = response.data.price_per_driver;
        this.price_per_seat = response.data.price_per_seat;
        this.is_payment_enabled = response.data.is_payment_enabled;
        this.loading = false;
      });
    },
    designCustomPlan() {
      $(this.$refs.customPlanModal).modal('show');
    },
    createCustomPlan() {
      if (!this.submitingCustomPlan) {
        this.submitingCustomPlan = true
        axios.post(`/api/plans/createCustom`, this.customPlan)
        .then(response => {
          this.submitingCustomPlan = false
          $(this.$refs.customPlanModal).modal('hide');
          let planId = response.data.id;
          this.customPlan = {
            name: "",
            allowed_drivers: "",
            allowed_children: ""
          };
          location.href = `/plan/${planId}/pay`;
        })
        .catch(error => {
          this.submitingCustomPlan = false
          this.$toasted.global.error('Error in creating plan!')
          this.customPlan = {
            name: "",
            allowed_drivers: "",
            allowed_children: ""
          };
        })
      }
    },
    addonCustomPlan() {
      $(this.$refs.addonCustomPlanModal).modal('show');
    },
    addToCustomPlan () {
      if (!this.submitingAddonCustomPlan) {
        this.submitingAddonCustomPlan = true
        axios.post(`/api/plans/addToCustom`, {
          addon_drivers: this.addon_drivers,
          addon_children: this.addon_children
        })
        .then(response => {
          this.submitingAddonCustomPlan = false
          $(this.$refs.addonCustomPlanModal).modal('hide');
          let planId = response.data.id;
          this.addon_drivers = "";
          this.addon_children = "";
          location.href = `/plan/${planId}/pay`;
        })
        .catch(error => {
          this.submitingAddonCustomPlan = false
          this.$toasted.global.error('Error in creating plan!')
          this.addon_drivers = "";
          this.addon_children = "";
        }
        )
      }
    },
    selectPlan(planId, renew = false) {
      //check if stripe is enabled
      if (this.is_payment_enabled) {
        //if so,
        if (
          this.school.plan.is_free != 1 &&
          this.school.plan.is_pay_as_you_go != 1
        ) {
          if (renew) {
            location.href = `/plan/${planId}/pay`;
          } else {
            swal({
              title: "Are you sure?",
              text: "The new plan will take effect immediately and you will not be able to continue the current subscription!",
              icon: "warning",
              buttons: true,
              dangerMode: true,
            }).then((willProceed) => {
              if (willProceed) {
                location.href = `/plan/${planId}/pay`;
              }
            });
          }
        } else location.href = `/plan/${planId}/pay`;
      } else {
        var selected_plan = null;
        for (var i = 0; i < this.plans.length; i++) {
          if (this.plans[i].id == planId) {
            selected_plan = this.plans[i];
            break;
          }
        }
        if (selected_plan != null) {
          if (
            selected_plan.is_free == 1
          ) {
            swal({
              title: "Are you sure?",
              text: "The new plan will take effect immediately and you will not be able to continue the current subscription!",
              icon: "warning",
              buttons: true,
              dangerMode: true,
            }).then((willProceed) => {
              if (willProceed) {
                location.href = `/plan/${planId}/pay`;
              }
            });
          } else
            swal(
              "Contact system administrator",
              "In order to change your current plan, please contant the system administrator.",
              "info"
            );
        }
      }
    },
  },
};
</script>
<style scoped>
.btn {
  padding: 0 !important;
}
.btn:focus,
.btn.focus {
  outline: 0;
  -webkit-box-shadow: 0 0 0;
  box-shadow: 0 0 0;
}
</style>