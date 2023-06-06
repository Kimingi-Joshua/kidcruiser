<template>
  <div class="container">
    <div class="card-header px-0 mt-2 bg-transparent clearfix">
      <h4 class="float-left pt-2">
        <i class="card-icon fas fa-wallet"></i> Charge Wallet for Parents
      </h4>
    </div>
    <div v-if="school" class="card-header px-0 mt-2 bg-transparent clearfix">
      <p class="float-left pt-2">{{ school.name }}</p>
    </div>
    <div class="card-body px-0">
      <div class="row justify-content-between">
        <div class="col-7 col-md-5">
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text" @click="filter">
                <i class="fas fa-search"></i>
              </span>
            </div>
            <input
              type="text"
              class="form-control"
              placeholder="Seach"
              v-model.trim="filters.search"
              @keyup.enter="filter"
            />
          </div>
        </div>
        <div class="col-auto">
          <multiselect
            v-model="filters.pagination.per_page"
            :options="[25, 50, 100, 200]"
            :searchable="false"
            :show-labels="false"
            :allow-empty="false"
            @select="changeSize"
            placeholder="Search"
          >
          </multiselect>
        </div>
      </div>
      <table class="table table-hover" v-if="!loading">
        <thead>
          <tr>
            <th>
              <input
                type="checkbox"
                v-model="selectAll"
                @change="selectAllParent"
              />
            </th>
            <th class="d-none d-sm-table-cell">
              <a href="#" class="text-dark" @click.prevent="sort('name')"
                >Name</a
              >
              <i
                class="ml-1 fas"
                :class="{
                  'fa-long-arrow-alt-down':
                    filters.orderBy.column == 'name' &&
                    filters.orderBy.direction == 'asc',
                  'fa-long-arrow-alt-up':
                    filters.orderBy.column == 'name' &&
                    filters.orderBy.direction == 'desc',
                }"
              ></i>
            </th>
            <th class="d-none d-sm-table-cell">
              <a class="text-dark">No. of children</a>
              <i class="ml-1 fas"></i>
            </th>
            <th class="d-none d-sm-table-cell">
              <a href="#" class="text-dark" @click.prevent="sort('tel_number')"
                >Telephone</a
              >
              <i
                class="ml-1 fas"
                :class="{
                  'fa-long-arrow-alt-down':
                    filters.orderBy.column == 'created_at' &&
                    filters.orderBy.direction == 'asc',
                  'fa-long-arrow-alt-up':
                    filters.orderBy.column == 'created_at' &&
                    filters.orderBy.direction == 'desc',
                }"
              ></i>
            </th>
            <th class="d-none d-sm-table-cell">
              <a class="text-dark">Location</a>
              <i class="ml-1 fas"></i>
            </th>
            <th class="d-none d-sm-table-cell">
              <a href="#" class="text-dark" @click.prevent="sort('created_at')"
                >Created</a
              >
              <i
                class="ml-1 fas"
                :class="{
                  'fa-long-arrow-alt-down':
                    filters.orderBy.column == 'created_at' &&
                    filters.orderBy.direction == 'asc',
                  'fa-long-arrow-alt-up':
                    filters.orderBy.column == 'created_at' &&
                    filters.orderBy.direction == 'desc',
                }"
              ></i>
            </th>
            <th
              v-if="school.plan.is_pay_as_you_go == 1"
              class="d-none d-sm-table-cell"
            >
              <a href="#" class="text-dark" @click.prevent="sort('driver')"
                >Wallet</a
              >
              <i
                class="ml-1 fas"
                :class="{
                  'fa-long-arrow-alt-down':
                    filters.orderBy.column == 'wallet' &&
                    filters.orderBy.direction == 'asc',
                  'fa-long-arrow-alt-up':
                    filters.orderBy.column == 'wallet' &&
                    filters.orderBy.direction == 'desc',
                }"
              ></i>
            </th>
            <th
              v-if="school.plan.is_pay_as_you_go == 1"
              class="d-none d-sm-table-cell"
            >
              <a href="#" class="text-dark" @click.prevent="sort('driver')"
                >Next due at</a
              >
              <i
                class="ml-1 fas"
                :class="{
                  'fa-long-arrow-alt-down':
                    filters.orderBy.column == 'next_renews_at' &&
                    filters.orderBy.direction == 'asc',
                  'fa-long-arrow-alt-up':
                    filters.orderBy.column == 'next_renews_at' &&
                    filters.orderBy.direction == 'desc',
                }"
              ></i>
            </th>
            <th
              v-if="school.plan.is_pay_as_you_go != 1"
              class="d-none d-sm-table-cell"
            >
              <a href="#" class="text-dark" @click.prevent="sort('created_at')"
                >Last update</a
              >
              <i
                class="ml-1 fas"
                :class="{
                  'fa-long-arrow-alt-down':
                    filters.orderBy.column == 'updated_at' &&
                    filters.orderBy.direction == 'asc',
                  'fa-long-arrow-alt-up':
                    filters.orderBy.column == 'updated_at' &&
                    filters.orderBy.direction == 'desc',
                }"
              ></i>
            </th>
            <th class="d-none d-sm-table-cell"></th>
            <th class="d-none d-sm-table-cell"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="parent in parents"
            @click="selectParent(parent.secret_key)"
          >
            <td>
              <label class="form-checkbox">
                <input
                  type="checkbox"
                  :value="parent.secret_key"
                  v-model="selectedparents"
                />
                <i class="form-icon"></i>
              </label>
            </td>
            <td>{{ parent.name }}</td>
            <td class="d-none d-sm-table-cell">{{ parent.children_count }}</td>
            <td class="d-none d-sm-table-cell">
              +({{ parent.country_code }}) {{ parent.tel_number }}
            </td>
            <td class="d-none d-sm-table-cell">
              {{
                parent.address_latitude == null ||
                parent.address_longitude == null
                  ? "Not set"
                  : parseFloat(parent.address_latitude).toFixed(2) +
                    "," +
                    parseFloat(parent.address_longitude).toFixed(2)
              }}
            </td>
            <td class="d-none d-sm-table-cell">
              <small>{{ parent.created_at | moment("LL") }}</small> -
              <small class="text-muted">{{
                parent.created_at | moment("LT")
              }}</small>
            </td>
            <td
              v-if="school.plan.is_pay_as_you_go != 1"
              class="d-none d-sm-table-cell"
            >
              <small>{{ parent.updated_at | moment("LL") }}</small> -
              <small class="text-muted">{{
                parent.updated_at | moment("LT")
              }}</small>
            </td>
            <td
              v-if="school.plan.is_pay_as_you_go == 1"
              class="d-none d-sm-table-cell"
            >
              {{ parent.wallet }}
            </td>
            <td
              v-if="school.plan.is_pay_as_you_go == 1"
              class="d-none d-sm-table-cell"
            >
              <small>{{
                parent.next_renews_at != null ? parent.next_renews_at : "-"
              }}</small>
            </td>
            <td class="d-sm-table-cell">
              <a
                href="#"
                @click="openChargeWalletModal(parent.secret_key)"
                :disabled="submittingChargeWallet"
              >
                <i class="fas fa-plus-square"></i>
              </a>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="row" v-if="!loading && filters.pagination.total > 0">
        <div class="col pt-2">
          {{ filters.pagination.from }}-{{ filters.pagination.to }} of
          {{ filters.pagination.total }}
        </div>
        <div class="col" v-if="filters.pagination.last_page > 1">
          <nav aria-label="Page navigation">
            <ul class="pagination justify-content-end">
              <li
                class="page-item"
                :class="{ disabled: filters.pagination.current_page <= 1 }"
              >
                <a
                  class="page-link"
                  href="#"
                  @click.prevent="changePage(pagination.current_page - 1)"
                  ><i class="fas fa-angle-left"></i
                ></a>
              </li>
              <li
                class="page-item"
                v-for="page in filters.pagination.last_page"
                :class="{ active: filters.pagination.current_page == page }"
              >
                <span
                  class="page-link"
                  v-if="filters.pagination.current_page == page"
                  >{{ page }}</span
                >
                <a
                  class="page-link"
                  href="#"
                  v-else
                  @click.prevent="changePage(page)"
                  >{{ page }}</a
                >
              </li>
              <li
                class="page-item"
                :class="{
                  disabled:
                    filters.pagination.current_page >=
                    filters.pagination.last_page,
                }"
              >
                <a
                  class="page-link"
                  href="#"
                  @click.prevent="
                    changePage(filters.pagination.current_page + 1)
                  "
                  ><i class="fas fa-angle-right"></i
                ></a>
              </li>
            </ul>
          </nav>
        </div>
        <!-- Button trigger modal -->
        <button type="button" class="btn btn-success mr-3" :disabled="selectedparents.length==0" data-toggle="modal" 
        data-target="#chargeWalletModal">
          Charge Wallet
        </button>
      </div>

      <div
        class="no-items-found text-center mt-5"
        v-if="!loading && !parents.length > 0"
      >
        <i class="icon-magnifier fa-3x text-muted"></i>
        <p class="mb-0 mt-3"><strong>Could not find any items</strong></p>
        <p class="text-muted">Try changing the filters or add a new one</p>
      </div>
      <content-placeholders v-if="loading">
        <content-placeholders-text />
      </content-placeholders>
    </div>

    <!-- Upload Parents Modal -->
    <div
      class="modal fade"
      id="chargeWalletModal"
      ref="chargeWalletModal"
      tabindex="-1"
      role="dialog"
      aria-labelledby="chargeWalletModalTitle"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="exampleModalLongTitle">
              Charge Wallet
            </h5>
            <button
              type="button"
              class="close"
              data-dismiss="modal"
              aria-label="Close"
            >
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div>
              <div class="my-4">
                Charge wallet for the selected parents
              </div>
              <div class="form-group ">
                <label>Amount</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="amount"
                  placeholder="enter the required amount"
                />
              </div>
            </div>
            <br />
            <br />
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-dismiss="modal"
            >
              Cancel
            </button>
            <button
              type="button"
              class="btn btn-primary"
              @click.prevent="submitChargeWallet"
            >
              <i
                class="fas fa-spinner fa-spin"
                v-if="submittingChargeWallet"
              ></i>
              OK
            </button>
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
      file: "",
      parents: [],
      school: {},
      filters: {
        pagination: {
          from: 0,
          to: 0,
          total: 0,
          per_page: 25,
          current_page: 1,
          last_page: 0,
        },
        orderBy: {
          column: "id",
          direction: "asc",
        },
        search: "",
      },
      loading: true,
      amount: "",
      submittingChargeWallet: false,
      selectedparents: [],
      selectAll: false,
    };
  },
  mounted() {
    if (localStorage.getItem("filtersTableParents")) {
      this.filters = JSON.parse(localStorage.getItem("filtersTableParents"));
    } else {
      localStorage.setItem("filtersTableParents", this.filters);
    }
    this.getParents();
  },
  methods: {
    openChargeWalletModal(secret_key) {
      this.selectedParent = null;
      for (var i = 0; i < this.parents.length; i++) {
        if (this.parents[i].secret_key == secret_key) {
          this.selectedParent = this.parents[i];
          break;
        }
      }
      if (this.selectedParent != null)
        $(this.$refs.chargeWalletModal).modal("show");
    },
    submitChargeWallet() {
      if (!this.submittingChargeWallet) {
        this.submittingChargeWallet = true;
        swal({
          title: "Are you sure?",
          text: "You are about to charge a wallet, do you want to continue?",
          icon: "warning",
          buttons: true,
          dangerMode: true,
        }).then((willChange) => {
          if (willChange) {
            axios
              .post(`/api/schools/parents/chargeWallet`, 
              {
                  selectedparents: this.selectedparents,
                  amount: parseInt(this.amount),
              })
              .then((response) => {
                $(this.$refs.chargeWalletModal).modal("hide");
                this.$toasted.global.error("Wallet charged successfully!");
                this.selectedparents = [];
                this.getParents();
              })
              .catch((error) => {
                this.$toasted.global.error("Error in charging wallet");
              })
              .then(() => {
                this.submittingChargeWallet = false;
                this.loading = false;
              });
          }
          else{
            this.submittingChargeWallet = false;
          }
        });
      }
    },
    selectAllParent() {
      if (!this.selectAll) {
        this.selectedparents = [];
      } else {
        this.parents.forEach((p) => {
          this.selectedparents.push(p.secret_key);
        });
      }
      console.log(this.selectedparents);
    },
    selectParent(id) {
      if (!this.selectAll) {
        if (this.selectedparents.includes(id)) {
          var index = this.selectedparents.indexOf(id);
          if (index > -1) {
            this.selectedparents.splice(index, 1);
          }
        } else this.selectedparents.push(id);
      }
    },
    getParents() {
      let str = window.location.pathname;
      let res = str.split("/");
      this.loading = true;
      localStorage.setItem("filtersTableParents", JSON.stringify(this.filters));
      let school_id = res[2];
      let newPostData = { ...this.filters, school: school_id };
      axios
        .post("/api/schools/parents/filter", newPostData)
        .then((response) => {
          if (response.data) {
            this.parents = response.data.parents.data;
            this.school = response.data.school;
            console.log(this.school);
            delete response.data.parents.data;
            delete response.data.school;
            this.filters.pagination = response.data.parents;
          }
          this.loading = false;
        })
        .catch((error) => {
          console.log(error);
          this.errors = error.response.data.errors;
          this.$toasted.global.error(this.errors[0]);
          this.loading = false;
        });
    },
    // Filters
    filter() {
      this.selectAll = false;
      this.selectedparents = [];
      this.filters.pagination.current_page = 1;
      this.getParents();
    },
    changeSize(perPage) {
      this.filters.pagination.current_page = 1;
      this.filters.pagination.per_page = perPage;
      this.getParents();
    },
    sort(column) {
      if (column == this.filters.orderBy.column) {
        this.filters.orderBy.direction =
          this.filters.orderBy.direction == "asc" ? "desc" : "asc";
      } else {
        this.filters.orderBy.column = column;
        this.filters.orderBy.direction = "asc";
      }

      this.getParents();
    },
    changePage(page) {
      this.filters.pagination.current_page = page;
      this.getParents();
    },
  },
};
</script>
<style>
input[type="file"] {
  position: absolute;
  top: -500px;
}

.file-upload {
  position: relative;
}

.file-upload__label {
  display: block;
  padding: 1em 2em;
  color: #fff;
  background: #38c172;
  border-radius: 0.4em;
  transition: background 0.3s;
}
.file-upload__label:hover {
  color: #fff;
  background-color: #82a1ac;
  border-color: #82a1ac;
  cursor: pointer;
}

.file-upload__input {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  font-size: 1;
  width: 0;
  height: 100%;
  opacity: 0;
}
</style>
