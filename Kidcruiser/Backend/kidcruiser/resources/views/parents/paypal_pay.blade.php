@extends('layouts.public')

@section('content')
<div class="container">
    <div class="card-header px-0 mt-2 bg-transparent clearfix">
        <h4 class="float-left pt-2">
            <i class="card-icon fas fa-wallet"></i> Recharge wallet
        </h4>
    </div>
    <div class="card-body px-0">
        <div class="card-footer py-2 my-4">
            <div class="card-footer d-flex justify-content-center align-items-center">
                <button id="submit" class="btn btn-primary">
                    Pay <?= $orgAmount ?> <?= $currency ?>
                </button>
            </div>
        </div>
    </div>
</div>
@endsection

@section('script')
<script type="text/javascript">
    var options = {
        "key": '<?= $razorpay_key_id ?>', // Enter the Key ID generated from the Dashboard
        "amount": '<?= $amount ?>', // Amount is in currency subunits. Default currency is INR. Hence, 50000 refers to 50000 paise or INR 500.
        "order_id": '<?= $order_id ?>',
        "handler": function(response) {
            //send to server to verify and complete
            submitRazorpayPaymentId(response.razorpay_payment_id)
        },
        "prefill": {
            "name": '<?= $name ?>',
            "contact": '<?= $contact ?>'
        }
    };

    var razorpay = new Razorpay(options);
    razorpay.on('payment.failed', function(response) {
        alert(response.error.code);
        alert(response.error.description);
    });
    const pay = document.getElementById('submit');
    pay.addEventListener('click', async (event) => {
        razorpay.open();
        event.preventDefault();
    });

    function submitRazorpayPaymentId(payment_id){
        window.location.href = '<?= $callback_url ?>' + "?payment_id=" + payment_id;
    }
</script>
@endsection