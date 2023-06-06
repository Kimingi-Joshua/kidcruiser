@extends('layouts.public')



<div class="container mt-6">

    <!-- Success message -->
    @isset($success)
    <div class="alert alert-success">
        {{$success}}
    </div>
    @endif
    <div class="card-header px-0 mt-2 bg-transparent clearfix">
        <h4 class="float-left pt-2">
            <i class="card-icon fas fa-wallet"></i> Recharge wallet
        </h4>
    </div>
    <form method="post" action="{{ route('finalize-pay', ['secret_key' => $parent->secret_key]) }}">
    @csrf
        <div class="card-body px-0">
            <div class="row" v-if="!loading">
                <div class="form-group col-sm-12">
                    <strong>Name </strong> {{$parent->name}}
                </div>
                <div class="form-group col-sm-12">
                    <strong>Tel number </strong> +({{$parent->country_code}}) {{$parent->tel_number}}
                </div>
                <div class="form-group col-sm-12">
                    <label>Recharge amount</label>
                    <input type="number" class="form-control" placeholder="Recharge amount" name="amount">
                    <!-- Error -->
                    @if ($errors->has('amount'))
                    <div class="error">
                        {{ $errors->first('amount') }}
                    </div>
                    @endif
                </div>
            </div>
        </div>

        <div class="card-footer px-3 py-2">
            <div v-if="checkRenew()" class="card-footer p-4 d-flex justify-content-center align-items-center">
                <button type="submit" class="btn btn-primary">
                    Recharge
                </button>
            </div>
        </div>
    </form>
</div>