@extends('layouts.app')

<script src="https://www.paypal.com/sdk/js?client-id=<?= $client_id ?>"></script>
<script>
    // Render the PayPal button into #paypal-button-container
    paypal.Buttons({
        createOrder: function(data, actions) {
            return fetch('/api/profile/paypalCreateOrder', {
                method: 'POST',
                body: JSON.stringify({
                    'plan': <?= $plan ?>,
                })
            }).then(function(res) {
                //res.json();
                return res.json();
            }).then(function(order) {
                console.log(order);
                return order.id;
            });
        },
        onApprove: function(data, actions) {
            return fetch('/api/profile/paypalUpdateOrder', {
                method: 'POST',
                body: JSON.stringify({
                    orderId: data.orderID,
                })
            }).then(function(res) {
                // console.log(res.json());
                return res.json();
            }).then(function(orderData) {
                // Successful capture! For demo purposes:
                //  console.log('Capture result', orderData, JSON.stringify(orderData, null, 2));
                var transaction = orderData.purchase_units[0].payments.captures[0];
                alert('done');
            });
        }
    }).render('#paypal-button-container');
</script>

@section('content')


<paypal></paypal>

@endsection