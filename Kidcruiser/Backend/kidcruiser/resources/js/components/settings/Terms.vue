<template>
<div class="container">
    <div class="row justify-content-md-center">
        <div class="col-md-9 col-xl-7">
            <div class="card-header px-0 mt-2 bg-transparent clearfix">
                <h4 class="float-left pt-2"><i class="card-icon fas fa-allergies"></i> Terms & Conditions</h4>
                <div class="card-header-actions mr-1">
                    <a class="btn btn-success" href="/terms.html" target="_blank">
                        <i class="fas fa-eye"></i>
                        <span class="ml-1">Peview</span>
                    </a>
                    <a class="btn btn-primary" href="#" :disabled="submiting" @click.prevent="updateTerms">
                        <i class="fas fa-spinner fa-spin" v-if="submiting"></i>
                        <i class="fas fa-check" v-else></i>
                        <span class="ml-1">Save</span>
                    </a>
                </div>
            </div>

            <div class="card-body px-0" v-show="!loading">
                <wysiwyg v-model="terms" />
            </div>
            <div class="row justify-content-md-center" v-show="loading">
                <div class="col-md-12">
                    <content-placeholders>
                        <content-placeholders-heading :img="true" />
                        <content-placeholders-text />
                    </content-placeholders>
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
            terms: {},
            errors: {},
            loading: true,
            submiting: false,
        }
    },
    components: {
        
    },
    mounted() {
        this.getTerms();
    },
    methods: {
        getTerms() {
            this.loading = true
            this.terms = {};
            axios.get(`/api/settings/getTerms`)
                .then(response => {
                    if(response.data)
                    {
                        this.terms = response.data.terms;
                        delete response.data.data
                    }
                    this.loading = false;

                }).catch(error => {
                    if(error.response)
                        this.errors = error.response.data.errors
                    this.loading = false
                });
        },
        updateTerms() {
            if (!this.submiting) {
                this.submiting = true
                axios.put(`/api/settings/updateTerms`, {'terms': this.terms})
                    .then(response => {
                        this.errors = {}
                        this.submiting = false
                        this.$toasted.global.error('Terms updated!');
                    })
                    .catch(error => {
                        if(error.response)
                        {
                            this.errors = error.response.data.errors
                            swal("Error", error.response.data.errors, "error")
                        }
                        this.submiting = false
                    })
            }
        }
    },
}
</script>
