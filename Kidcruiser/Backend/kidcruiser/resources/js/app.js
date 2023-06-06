
/**
 * First we will load all of this project's JavaScript dependencies which
 * includes Vue and other libraries. It is a great starting point when
 * building robust, powerful web applications using Vue and Laravel.
 */

require('./bootstrap');

window.Vue = require('vue');


// Dependencies --------------------------------------

import Toasted from 'vue-toasted';
import VueClip from 'vue-clip'
import Multiselect from 'vue-multiselect'
import swal from 'sweetalert';
import VueContentPlaceholders from 'vue-content-placeholders'
import Chartkick from 'vue-chartkick'
import Chart from 'chart.js'
import Datepicker from 'vuejs-datepicker';


Vue.use(Chartkick.use(Chart))

Vue.use(require('vue-moment'));
Vue.use(Toasted)
Vue.toasted.register('error', message => message, {
    position : 'bottom-center',
    duration : 3000
})
Vue.use(VueClip)
Vue.component('multiselect', Multiselect)
Vue.use(VueContentPlaceholders)

import wysiwyg from "vue-wysiwyg";
Vue.use(wysiwyg, {});


/**
 * Next, we will create a fresh Vue application instance and attach it to
 * the page. Then, you may begin adding components to this application
 * or customize the JavaScript scaffolding to fit your unique needs.
 */

 // Layout
 Vue.component('sidebar', require('./components/layout/Sidebar.vue').default);

// Dashboard
Vue.component('dashboard', require('./components/dashboard/Dashboard.vue').default);
Vue.component('sadmin_dashboard', require('./components/dashboard/SAdminDashboard.vue').default);


// Profile
Vue.component('profile', require('./components/profile/Profile.vue').default);
Vue.component('profile-password', require('./components/profile/Password.vue').default);
Vue.component('plan', require('./components/profile/Plan.vue').default);

Vue.component('stripepay', require('./components/profile/stripe/Stripepay.vue').default);
Vue.component('razorpay', require('./components/profile/razor/Razorpay.vue').default);
Vue.component('paypal', require('./components/profile/paypal/Paypalpay.vue').default);

// Schools
Vue.component('schools-index', require('./components/schools/Index.vue').default);
Vue.component('schools-create', require('./components/schools/Create.vue').default);
Vue.component('schools-list-parents', require('./components/schools/ListParents.vue').default);


// School
Vue.component('school', require('./components/school/School.vue').default);

// Settings
Vue.component('settings', require('./components/settings/Settings.vue').default);
Vue.component('privacy-policy', require('./components/settings/Privacy.vue').default);
Vue.component('terms', require('./components/settings/Terms.vue').default);

// Plans
Vue.component('plans-index', require('./components/plans/Index.vue').default);
Vue.component('plans-create', require('./components/plans/Create.vue').default);
Vue.component('plans-edit', require('./components/plans/Edit.vue').default);

// Parent
Vue.component('parents-index', require('./components/parents/Index.vue').default);
Vue.component('parents-create', require('./components/parents/Create.vue').default);
Vue.component('parents-edit', require('./components/parents/Edit.vue').default);
Vue.component('parents-map', require('./components/parents/Map.vue').default);

// Driver
Vue.component('drivers-index', require('./components/drivers/Index.vue').default);
Vue.component('drivers-create', require('./components/drivers/Create.vue').default);
Vue.component('drivers-edit', require('./components/drivers/Edit.vue').default);
Vue.component('drivers-map', require('./components/drivers/Map.vue').default);
Vue.component('drivers-history', require('./components/drivers/History.vue').default);

//Bus
Vue.component('buses-index', require('./components/buses/Index.vue').default);


//Activation
Vue.component('activation', require('./components/activation/Activation.vue').default);

// products IOS
Vue.component('products-index', require('./components/productsIOS/Index.vue').default);
Vue.component('products-create', require('./components/productsIOS/Create.vue').default);
Vue.component('products-edit', require('./components/productsIOS/Edit.vue').default);

// Custom Plans
Vue.component('custom-plans-index', require('./components/customPlans/Index.vue').default);

const app = new Vue({
    el: '#app'
});
