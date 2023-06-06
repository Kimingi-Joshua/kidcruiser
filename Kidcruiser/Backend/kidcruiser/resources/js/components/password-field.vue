<template>
<div>
  <div class="input-group">
    <input
      v-if="showPassword"
      class="form-control"
      :class="{ 'is-invalid': error }"
      type="text"
      v-model="myPassword"
      v-on:input="updateValue($event.target.value)"
    />
    <input
      v-else
      class="form-control"
      :class="{ 'is-invalid': error }"
      type="password"
      v-model="myPassword"
      v-on:input="updateValue($event.target.value)"
    />
    <div class="input-group-append">
      <button class="button" type="button" @click="toggleShow">
        <span class="icon is-small is-right">
          <i
            class="fas"
            :class="{
              'fa-eye-slash': showPassword,
              'fa-eye': !showPassword,
            }"
          ></i>
        </span>
      </button>
    </div>
  </div>
  <div class="invalid-feedback" v-if="error">{{error[0]}}</div>
</div>
</template>
<script>
export default {
  props: ['password', 'error'],
  data () {
    return {
      showPassword: false,
      myPassword: this.password
    }
  },
  methods: {
    updateValue: function (value) {
      this.$emit('input', value);
    },
    toggleShow() {
      this.showPassword = !this.showPassword;
    },
  }
}
</script>
<style>
.invalid-feedback {
  display: block !important;
  width: 100%;
  margin-top: 0.25rem;
  font-size: 80%;
  color: #e3342f;
}
</style>