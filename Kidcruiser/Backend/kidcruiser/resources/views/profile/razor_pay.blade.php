@extends('layouts.app')

<script src="https://checkout.razorpay.com/v1/checkout.js"></script>
@section('content')

    <razorpay razorpay_key_id={{$razorpay_key_id}} 
    order_id={{$order_id}} 
    email={{$email}}></razorpay>
@endsection