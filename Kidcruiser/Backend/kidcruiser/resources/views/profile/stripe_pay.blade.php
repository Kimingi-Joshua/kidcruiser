@extends('layouts.app')

<script src="https://js.stripe.com/v3/"></script>
@section('content')
    <stripepay stripe_publishable_key={{$stripe_publishable_key}} 
    client_secret={{$client_secret}}></stripepay>
@endsection