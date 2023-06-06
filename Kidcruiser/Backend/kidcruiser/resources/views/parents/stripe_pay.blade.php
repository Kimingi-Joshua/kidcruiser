@extends('layouts.public')

@section('content')
<div class="container">
    <div class="card-header px-0 mt-2 bg-transparent clearfix">
        <h4 class="float-left pt-2">
            <i class="card-icon fas fa-wallet"></i> Recharge wallet for <?= $amount ?>
        </h4>
    </div>
    <div class="card-body px-0">
        <form id="payment-form">
            <div id="payment-element">
                <!-- Elements will create form elements here -->
            </div>
            <div class="card-footer py-2 my-4">
                <div class="card-footer d-flex justify-content-center align-items-center">
                    <button id="submit" type="submit" class="btn btn-primary">
                    <span id="submit_show" style="display: none;" class="spinner-border spinner-border-sm"></span>
                        Recharge
                    </button>
                </div>
            </div>
            <div id="error-message">
                <!-- Display error message to your customers here -->
            </div>
        </form>
    </div>
</div>
@endsection

@section('script')
<script type="text/javascript">
    const stripe = Stripe(
        '<?= $stripe_publishable_key ?>'
    );

    const options = {
        clientSecret: '<?= $client_secret ?>',
    };

    // Set up Stripe.js and Elements to use in checkout form, passing the client secret obtained in step 2
    const elements = stripe.elements(options);

    // Create and mount the Payment Element
    const paymentElement = elements.create('payment');
    paymentElement.mount('#payment-element');

    const form = document.getElementById('payment-form');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        let butn = document.getElementById('submit');
        let submit_show = document.getElementById('submit_show');
        
        if (submit_show.style.display === "none") {
            submit_show.style.display = "";
        }
        butn.disabled = true;

        const {
            error
        } = await stripe.confirmPayment({
            //`Elements` instance that was used to create the Payment Element
            elements,
            confirmParams: {
                return_url: '<?= $return_url ?>',
            },
        });

        if (error) {
            // This point will only be reached if there is an immediate error when
            // confirming the payment. Show error to your customer (for example, payment
            // details incomplete)
            const messageContainer = document.querySelector('#error-message');
            messageContainer.textContent = error.message;

            butn.removeAttribute('disabled');
            submit_show.style.display = "none";
        } else {
            // Your customer will be redirected to your `return_url`. For some payment
            // methods like iDEAL, your customer will be redirected to an intermediate
            // site first to authorize the payment, then redirected to the `return_url`.
        }
    });
</script>
@endsection