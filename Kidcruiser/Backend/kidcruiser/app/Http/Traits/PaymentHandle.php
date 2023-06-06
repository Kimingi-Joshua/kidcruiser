<?php

namespace App\Http\Traits;

use App\Parent_;
use App\Plan;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Auth;

use Srmklive\PayPal\Services\PayPal as PayPalClient;

use App\Setting;
use App\Transaction;
use Illuminate\Support\Facades\Log;
use \App\Http\Traits\AuthSec;
use Razorpay\Api\Api;
use Razorpay\Api\Errors\SignatureVerificationError;
use App\Libraries\Flutterwave\library\rave;
use App\User;

use Paytabscom\Laravel_paytabs\Facades\paypage;

trait PaymentHandle
{
    use AuthSec;
    public function createCustomerPaymentGateway($school)
    {
        $payment_gateway_enable = $this->is_payment_enabled();
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "stripe") {
                $stripe_key = Setting::where('name', 'Stripe Secret key')->first()->value;
                $stripe = new \Stripe\StripeClient($stripe_key);

                try {
                    $stripe_customer = $stripe->customers->create([
                        'name' => $school->name,
                        'email' => $school->email,
                    ]);

                    $school->stripe_id = $stripe_customer->id;
                    $school->save();
                } catch (\Throwable $th) {
                }
            } else if ($payment_gateway_enable == "razorpay") {
                $razorpay_key = Setting::where('name', 'Razorpay Key Id')->first()->value;
                $razorpay_secret = Setting::where('name', 'Razorpay Key Secret')->first()->value;
                $razorpay_api = new Api($razorpay_key, $razorpay_secret);

                try {
                    $customer = $razorpay_api->customer->create(array(
                        'name' => $school->name,
                        'email' => $school->email,
                    ));
                    $school->razorpay_id = $customer->id;
                    $school->save();
                } catch (\Throwable $th) {
                }
            } else if ($payment_gateway_enable == "flutterwave") {

            }
        }
    }
    public function is_payment_enabled()
    {
        $payment_gateway_enable = Setting::where('name', 'Payment Gateway')->first()->value;
        if ($payment_gateway_enable == null || $payment_gateway_enable == 'none') {
            return null;
        } else {
            return $payment_gateway_enable;
        }
    }
    /* check if the settings for stripe is ok */
    public function is_payment_settings_ok()
    {
        $payment_gateway_enable = $this->is_payment_enabled();
        if ($payment_gateway_enable) {
            // get null settings
            $null_settings = Setting::where('value', null)->where('name', 'like', ucfirst($payment_gateway_enable) . '%')->get();
            $payment_ok = (count($null_settings) == 0);
            return $payment_ok;
        } else {
            return true;
        }
    }


    public function getPaymentBalance()
    {
        $payment_gateway_enable = $this->is_payment_enabled();
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "stripe") {
                $stripe_key = Setting::where('name', 'Stripe Secret key')->first()->value;
                $stripe = new \Stripe\StripeClient($stripe_key);
                $balance = $stripe->balance->retrieve();
                return $balance;
            } else if ($payment_gateway_enable == "razorpay") {
                $razorpay_key = Setting::where('name', 'Razorpay Key Id')->first()->value;
                $razorpay_secret = Setting::where('name', 'Razorpay Key Secret')->first()->value;
                $razorpay_api = new Api($razorpay_key, $razorpay_secret);
                //TODO
                return [ 
                    'available' => [0], 
                    "pending" => [0],
                ];
            }
            else{
                return [ 
                    'available' => [0], 
                    "pending" => [0],
                ];
            }
        }
    }


    public function updateSchoolPlanPayment(Request $request, $school)
    {
        $payment_gateway_enable = $this->is_payment_enabled();
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "stripe") {
                // get the request data and make sure that it contains the required information
                $requestData = $request->all();
                if (!(array_key_exists('paymentIntent', $requestData) && array_key_exists('clientSecret', $requestData))) {
                    return response()->json(['errors' => ['Payment' => ['Payment can not be created']]], 422);
                }
                // get the paymentIntent and plan id sent from front end
                $paymentIntent = $requestData['paymentIntent'];
                $paymentIntentSaved = Transaction::where('ref', $paymentIntent)->first();
                if ($paymentIntentSaved) {
                    Abort(404);
                }
                try {
                    $stripe_key = Setting::where('name', 'Stripe Secret key')->first()->value;
                    \Stripe\Stripe::setApiKey($stripe_key);
                    $paymentIntentStripe = \Stripe\PaymentIntent::retrieve(
                        $paymentIntent,
                        []
                    );
                    $plan = $paymentIntentStripe['metadata']['plan'];
                    //get plan details
                    $plan = Plan::findOrFail($plan);
                    // get the current school

                    Log::info("Renew the subscription of " . $school->name);

                    // do not return to free plan if this is addon plan
                    if (!$plan->is_addon) {
                        // return to free plan
                        $free_plan = Plan::where('is_free', 1)->first();
                        $this->updatePlanAndAdjustLimit($school, $free_plan);
                    }
                    // subscribe school to plan
                    $this->updatePlanAndAdjustLimit($school, $plan);
                    Transaction::create(['ref' => $paymentIntent]);
                } catch (\Stripe\Error\Base $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                } catch (\Exception $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                }
            } else if ($payment_gateway_enable == "razorpay") {
                $razorpay_key = Setting::where('name', 'Razorpay Key Id')->first()->value;
                $razorpay_secret = Setting::where('name', 'Razorpay Key Secret')->first()->value;
                $razorpay_api = new Api($razorpay_key, $razorpay_secret);

                // get the request data and make sure that it contains the required information
                $requestData = $request->all();
                if (!(array_key_exists('payment_id', $requestData) && array_key_exists('plan', $requestData) && array_key_exists('order_id', $requestData))) {
                    return response()->json(['errors' => ['Payment' => ['Payment can not be created']]], 422);
                }
                // get the token and plan id sent from front end
                $payment_id = $requestData['payment_id'];
                $plan = $requestData['plan'];
                $order_id = $requestData['order_id'];

                $paymentIdSaved = Transaction::where('ref', $payment_id)->first();

                if ($paymentIdSaved) {
                    Abort(404);
                }

                //get plan details
                $plan = Plan::findOrFail($plan);
                try {

                    $sig = hash_hmac('sha256', $order_id . "|" . $payment_id, $razorpay_secret);

                    $attributes = array(
                        'razorpay_signature' => $sig,
                        'razorpay_payment_id' => $payment_id,
                        'razorpay_order_id' => $order_id
                    );
                    $razorpay_api->utility->verifyPaymentSignature($attributes);

                    if (!$plan->is_addon) {
                        //return to free plan
                        $free_plan = Plan::where('is_free', 1)->first();
                        $this->updatePlanAndAdjustLimit($school, $free_plan);
                    }

                    //update school's plan
                    $this->updatePlanAndAdjustLimit($school, $plan);

                    Transaction::create(['ref' => $payment_id]);
                } catch (SignatureVerificationError $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                } catch (\Exception $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                }
            } else if ($payment_gateway_enable == "flutterwave") {
                $flutterwave_secret = Setting::where('name','Flutterwave Secret Key')->first()->value;
                Log::info(" handlePayment received");
                $tx_ref = $request->input('tx_ref');
                $transaction_id = $request->input('transaction_id');
                Log::info($tx_ref . " received");
        
                if($tx_ref)
                {
                    $previousTrans = Transaction::where('ref', $tx_ref)->first();
                    if($previousTrans)
                    {
                        Abort(404);
                    }
                }        
                list($school_email, $schoolId, $planId, $amount) = 
                $this->verifyFlutterwaveTransaction($transaction_id, $flutterwave_secret);
        
                
                $plan = Plan::findOrFail($planId);
                $school = User::where('email', $school_email)->where('id', $schoolId)->first(); 
        
                if($school && $plan)
                {
                    $this->updatePlanAndAdjustLimit($school, $plan);
                    Transaction::create(['ref' => $tx_ref]);
                    Log::info("Subscription of ". $school->name ." is renewed successfully");
                    return redirect('plan');
                }
                else
                {
                    return abort(404);
                }  
            } else if ($payment_gateway_enable == "paytabs") {
                $params = $request->all();
                Log::info("params = " . json_encode($params));

                $orderId = $params['cart_id'];

                $pieces = explode("_", $orderId);
                $schoolId = $pieces[0];
                $planId = $pieces[1];
                $plan = Plan::findOrFail($planId);
                $school = User::where('id', $schoolId)->first(); 
        
                $currency = Setting::where('name', 'Currency')->first()->value;
                $tran_ref = $params['tran_ref'];
                if($tran_ref)
                {
                    $previousTrans = Transaction::where('ref', $tran_ref)->first();
                    if($previousTrans)
                    {
                        Abort(404);
                    }
                } 

                $server_key = Setting::where('name', 'Paytabs API Server Key')->first()->value;
                $profile_id = Setting::where('name', 'Paytabs Profile Id')->first()->value;
                $region = Setting::where('name', 'Paytabs Region')->first()->value;
                
                $pay = paypage::setKeys($region, $currency, $profile_id, $server_key);
                $transaction = $pay->queryTransaction($tran_ref);
                Log::info("transaction = " . json_encode($transaction));
                if($transaction->success == true && $transaction->cart_amount == ($plan->is_addon ? $plan->addon_price : $plan->price))
                {
                    if($school && $plan)
                    {
                        $this->updatePlanAndAdjustLimit($school, $plan);
                        Transaction::create(['ref' => $tran_ref]);
                        Log::info("Subscription of ". $school->name ." is renewed successfully");
                    }
                    else
                    {
                        return abort(404);
                    }    
                }
            }
        }
    }

    public function prepareSchoolPayment($school, $plan, $payment_gateway_enable)
    {
        $currency = Setting::where('name', 'Currency')->first()->value;
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "stripe") {
                try {
                    if (!$school->stripe_id) {
                        $this->createCustomerPaymentGateway($school);
                    }
                    //otherwise, go to payment
                    $stripe_pub_key = Setting::where('name', 'Stripe Publishable key')->first()->value;
                    $stripe_key = Setting::where('name', 'Stripe Secret key')->first()->value;
                    \Stripe\Stripe::setApiKey($stripe_key);

                    $paymentIntent = \Stripe\PaymentIntent::create([
                        'customer' => $school->stripe_id,
                        'setup_future_usage' => 'off_session',
                        'amount' => $plan->is_addon ? $plan->addon_price : $plan->price * 100,
                        'currency' => strtolower($currency),
                        'automatic_payment_methods' => [
                            'enabled' => 'true',
                        ],
                        'metadata' => [
                            'plan' => $plan->id
                        ],
                    ]);
                    return view('profile.stripe_pay', [
                        'stripe_publishable_key' => $stripe_pub_key,
                        'client_secret' => $paymentIntent->client_secret
                    ]);
                } catch (\Stripe\Error\Base $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                } catch (\Exception $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                }
            } else if ($payment_gateway_enable == "razorpay") {
                $razorpay_key = Setting::where('name', 'Razorpay Key Id')->first()->value;
                $razorpay_secret = Setting::where('name', 'Razorpay Key Secret')->first()->value;
                $razorpay_api = new Api($razorpay_key, $razorpay_secret);

                try {
                    if (!$school->razorpay_id) {
                        $this->createCustomerPaymentGateway($school);
                    }

                    $order = $razorpay_api->order->create([
                        'amount' => $plan->is_addon ? $plan->addon_price : $plan->price * 100, // amount in the smallest currency unit
                        'currency' => $currency, //
                    ]);
                    $order_id = $order['id'];
                    return view(
                        'profile.razor_pay',
                        [
                            'razorpay_key_id' => $razorpay_key,
                            'order_id' => $order_id,
                            'email' => $school->email
                        ]
                    );
                } catch (\Exception $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                }
            } else if ($payment_gateway_enable == "paypal") {
                $paypal_client_id = Setting::where('name', 'Paypal Client ID')->first()->value;
                return view(
                    'profile.paypal_pay',
                    [
                        'client_id' => $paypal_client_id,
                        "plan" => $plan->id
                    ]
                );
            } else if ($payment_gateway_enable == "flutterwave") {
                $payment = new Rave("");

                $payment
                ->setCustomPlanId($plan->id)
                ->setCustomSchoolId($school->id)
                ->setAmount($plan->is_addon ? $plan->addon_price : $plan->price) // amount in the smallest currency unit)
                ->setDescription("Please pay for ". $plan->name. " plan")
                ->setCurrency($currency)
                ->setEmail($school->email)
                ->setFirstname($school->name)
                ->setRedirectUrl(route('flutterwave-payment')) //status=successful&tx_ref=RV_5f8c8fa8984db&transaction_id=1628516
                ->initialize();
            } else if ($payment_gateway_enable == "paytabs") {

                $server_key = Setting::where('name', 'Paytabs API Server Key')->first()->value;
                $profile_id = Setting::where('name', 'Paytabs Profile Id')->first()->value;
                $region = Setting::where('name', 'Paytabs Region')->first()->value;
                $orderId = $school->id . '_'. $plan->id . '_' . date("Y-m-d H:i:s");

                $pay = paypage::setKeys($region, $currency, $profile_id, $server_key)
                ->sendPaymentCode('all')
                ->sendTransaction('sale')
                ->sendCart($orderId,$plan->is_addon ? $plan->addon_price : $plan->price, $plan->is_addon ? "Please pay for addon to ". $plan->name. " plan" : "Please pay for ". $plan->name. " plan")
                ->sendCustomerDetails($school->name, $school->email, '', '', '', '', '', '','')
                ->sendHideShipping(true)
                ->sendURLs(route('paytabs-school-payment-return'), route('paytabs-school-payment-done'))
                ->sendLanguage('en')->create_pay_page();

                return redirect($pay->getTargetUrl());
            }
        }
    }

    public function getPaypalClient()
    {
        $currency = Setting::where('name', 'Currency')->first()->value;
        $paypal_client_id = Setting::where('name', 'Paypal Client ID')->first()->value;
        $paypal_secret = Setting::where('name', 'Paypal Secret')->first()->value;
        $paypal_env = Setting::where('name', 'Paypal Environment')->first()->value;

        $config = [
            'mode'    => $paypal_env,
            'sandbox' => [
                'client_id'         => $paypal_client_id,
                'client_secret'     => $paypal_secret,
                'app_id'            => 'APP-80W284485P519543T',
            ],
            'live' => [
                'client_id'         => $paypal_client_id,
                'client_secret'     => $paypal_secret,
                'app_id'            => env('PAYPAL_LIVE_APP_ID', ''),
            ],

            'payment_action' => env('PAYPAL_PAYMENT_ACTION', 'Sale'), // Can only be 'Sale', 'Authorization' or 'Order'
            'currency'       => $currency,
            'notify_url'     => env('PAYPAL_NOTIFY_URL', ''), // Change this accordingly for your application.
            'locale'         => env('PAYPAL_LOCALE', 'en_US'), // force gateway language  i.e. it_IT, es_ES, en_US ... (for express checkout only)
            'validate_ssl'   => env('PAYPAL_VALIDATE_SSL', false), // Validate SSL when creating api client.
        ];
        $paypalClient = new PayPalClient($config);

        $token = $paypalClient->getAccessToken();
        $paypalClient->setAccessToken($token);

        return $paypalClient;

    }

    public function paypalCreateOrder($request, $school)
    {
        $data = json_decode($request->getContent(), true);
        $currency = Setting::where('name', 'Currency')->first()->value;
        $payment_gateway_enable = $this->is_payment_enabled();
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "paypal") {
                $plan = $data['plan'];
                //get plan details
                $plan = Plan::findOrFail($plan);
                $paypalClient = $this->getPaypalClient();
                try {
                    $order = $paypalClient->createOrder([
                        "intent" => "CAPTURE",
                        "purchase_units" => [
                            [
                                "reference_id" => $plan->id,
                                "custom_id" => $school->id,
                                "amount" => [
                                    "currency_code" => $currency,
                                    "value" => $plan->price,
                                ],
                                "description" => 'The renewal fees for the plan of the school'
                            ]
                        ],
                        'application_context' => [
                            'shipping_preference' => 'NO_SHIPPING'
                        ]
                    ]);
                    return response()->json($order);
                } catch (\Exception $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                }
            }
        }
    }

    public function paypalUpdateOrder($request, $school)
    {
        $data = json_decode($request->getContent(), true);
        $orderId = $data['orderId'];
        $payment_gateway_enable = $this->is_payment_enabled();
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "paypal") {
                $paypalClient = $this->getPaypalClient();
                try {
                    $result = $paypalClient->capturePaymentOrder($orderId);
                    dd($orderId, $result);
                    if($result['status'] === "COMPLETED"){
                        dd($result);
                    }
                } catch (\Exception $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                }
            }
        }
        

    }
    
    public function chargeOffline($school, $payment_gateway_enable)
    {
        $currency = Setting::where('name', 'Currency')->first()->value;
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "stripe") {
                $stripe_key = Setting::where('name', 'Stripe Secret key')->first()->value;
                $stripe = new \Stripe\StripeClient($stripe_key);

                $payMethods = $stripe->paymentMethods->all([
                    'customer' => $school->stripe_id,
                    'type' => 'card'
                ]);
                for ($i = 0; $i < count($payMethods['data']); $i++) {
                    $payMethodId = $payMethods['data'][$i]['id'];
                    Log::info("payMethods = " . json_encode($payMethodId));

                    \Stripe\Stripe::setApiKey($stripe_key);
                    try {
                        $paymentIntent = \Stripe\PaymentIntent::create([
                            'customer' => $school->stripe_id,
                            'amount' => $school->plan->price * 100,
                            'currency' => strtolower($currency),
                            'metadata' => [
                                'plan' => $school->plan->id
                            ],
                            'payment_method' => $payMethodId,
                            'off_session' => true,
                            'confirm' => true,
                        ]);
                        $success = true;
                    } catch (\Stripe\Exception\CardException $e) {
                        // Error code will be authentication_required if authentication is needed
                        echo 'Error code is:' . $e->getError()->code;
                        $payment_intent_id = $e->getError()->payment_intent->id;
                        $payment_intent = \Stripe\PaymentIntent::retrieve($payment_intent_id);
                        $success = false;
                    }
                    if ($success)
                        return true;
                }
            } else if ($payment_gateway_enable == "razorpay") {
                $razorpay_key = Setting::where('name', 'Razorpay Key Id')->first()->value;
                $razorpay_secret = Setting::where('name', 'Razorpay Key Secret')->first()->value;
                $razorpay_api = new Api($razorpay_key, $razorpay_secret);
                $currency = Setting::where('name', 'Currency')->first()->value;

                $razorpay_api->paymentLink->create(
                    array(
                        'amount' => $school->plan->price * 100,
                        'currency' => $currency,
                        'description' => 'Renew ' . $school->plan->name,
                        'customer' => array(
                            'name' => $school->name,
                            'email' => $school->email,
                        ),
                        'notify' => array('sms' => true, 'email' => true),
                        'reminder_enable' => true,
                        'notes' => array('plan' => $school->plan->id, 'school' => $school->id),
                        'callback_url' => route('razorSuccessCharge'),
                        'callback_method' => 'get'
                    ),
                    'application/json'
                );
                return false;
            } else {
                return false;
            }
        }
        return false;
    }


    public function prepareParentPayment($amount, $secret_key, $parent)
    {
        $currency = Setting::where('name', 'Currency')->first()->value;
        $payment_gateway_enable = $this->is_payment_enabled();
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "stripe") {
                if ($parent->school->plan->is_pay_as_you_go == 1) {
                    $stripe_key = Setting::where('name', 'Stripe Secret key')->first()->value;
                    \Stripe\Stripe::setApiKey($stripe_key);
                    try {
                        //otherwise, go to payment
                        $stripe_pub_key = Setting::where('name', 'Stripe Publishable key')->first()->value;
                        $paymentIntent = \Stripe\PaymentIntent::create([
                            'amount' => $amount * 100,
                            'currency' => strtolower($currency),
                            'metadata' => [
                                'plan' => $parent->school->plan->id,
                                'parent' => $secret_key
                            ],
                        ]);
                        return view('parents.stripe_pay', [
                            'stripe_publishable_key' => $stripe_pub_key,
                            'client_secret' => $paymentIntent->client_secret,
                            'amount' => $amount . ' ' . $currency,
                            'return_url' => route('payment-done')
                        ]);
                    } catch (\Stripe\Error\Base $e) {
                        return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                    } catch (\Exception $e) {
                        return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                    }
                } else {
                    Abort(404);
                }
            } else if ($payment_gateway_enable == "razorpay") {
                $razorpay_key = Setting::where('name', 'Razorpay Key Id')->first()->value;
                $razorpay_secret = Setting::where('name', 'Razorpay Key Secret')->first()->value;
                $razorpay_api = new Api($razorpay_key, $razorpay_secret);
                $currency = Setting::where('name', 'Currency')->first()->value;
                try {
                    $orgAmount = $amount;
                    $amount = $amount * 100;
                    //go to payment
                    $order = $razorpay_api->order->create([
                        'amount' => $amount, // amount in the smallest currency unit
                        'currency' => $currency, //
                        'notes' => array('parent' => $secret_key, 'plan' => $parent->school->plan->id,)
                    ]);
                    $order_id = $order['id'];
                    return view(
                        'parents.razor_pay',
                        [
                            'razorpay_key_id' => $razorpay_key,
                            'orgAmount' => $orgAmount,
                            'currency' => $currency, //
                            'amount' => $amount,
                            'order_id' => $order_id,
                            'name' => $parent->name,
                            "callback_url" => route('payment-done'),
                            'contact' => $parent->country_code . $parent->tel_number
                        ]
                    );
                } catch (\Exception $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                }
            } else if ($payment_gateway_enable == "flutterwave") {
                try {
                    $payment = new Rave("");
                    $payment
                    ->setCustomPlanId($parent->school->plan->id)
                    ->setCustomSchoolId($parent->id)
                    ->setAmount($amount) // amount in the smallest currency unit)
                    ->setDescription("Please recharge your wallet with ". $amount)
                    ->setCurrency($currency)
                    ->setEmail($parent->tel_number . '@schools.com')
                    ->setFirstname($parent->name)
                    ->setRedirectUrl(route('payment-done')) //status=successful&tx_ref=RV_5f8c8fa8984db&transaction_id=1628516
                    ->initialize();
                } catch (\Exception $e) {
                    return response()->json(['errors' => ['Payment' => [$e->getMessage()]]], 422);
                }
            } else if ($payment_gateway_enable == "paytabs") {
                $server_key = Setting::where('name', 'Paytabs API Server Key')->first()->value;
                $profile_id = Setting::where('name', 'Paytabs Profile Id')->first()->value;
                $region = Setting::where('name', 'Paytabs Region')->first()->value;

                $orderId = $parent->id . '_'. $parent->school->plan->id . '_' . date("Y-m-d H:i:s");

                $pay = paypage::setKeys($region, $currency, $profile_id, $server_key)
                ->sendPaymentCode('all')
                ->sendTransaction('sale')
                ->sendCart($orderId, $amount, "Please pay ". $amount. " to recharge your wallet")
                ->sendCustomerDetails($parent->name, '', $parent->tel_number, '', '', '', '', '','')
                ->sendHideShipping(true)
                ->sendURLs(route('paytabs-parent-payment-return'), route('paytabs-parent-payment-done'))
                ->sendLanguage('en')->create_pay_page();

                return redirect($pay->getTargetUrl());
            }
        }
    }


    public function finalizeParentPayment(Request $request)
    {
        $payment_gateway_enable = $this->is_payment_enabled();
        if ($payment_gateway_enable) {
            if ($payment_gateway_enable == "stripe") {
                // get the request data and make sure that it contains the required information
                $requestData = $request->all();
                if (!(array_key_exists('payment_intent', $requestData) && array_key_exists('payment_intent_client_secret', $requestData))) {
                    return response()->json(['errors' => ['Payment' => ['Payment can not be created']]], 422);
                }
                // get the paymentIntent and plan id sent from front end
                $paymentIntent = $requestData['payment_intent'];

                $paymentIntentSaved = Transaction::where('ref', $paymentIntent)->first();
                if ($paymentIntentSaved) {
                    Abort(404);
                }

                $stripe_key = Setting::where('name', 'Stripe Secret key')->first()->value;
                \Stripe\Stripe::setApiKey($stripe_key);
                $paymentIntentStripe = \Stripe\PaymentIntent::retrieve(
                    $paymentIntent,
                    []
                );
                $plan = $paymentIntentStripe['metadata']['plan'];
                $parent = $paymentIntentStripe['metadata']['parent'];
                //get plan details
                $plan = Plan::findOrFail($plan);
                $id = $this->get_id($parent);
                $parent = Parent_::with('school.plan')->findOrFail($id);

                $amount = $paymentIntentStripe['amount'] / 100;

                $parent->wallet += $amount;
                if ($parent->next_renews_at == null) {
                    $billing_cycle = Setting::where('name', 'Billing cycle')->first()->value;
                    if ($billing_cycle === "year")
                        $parent->next_renews_at = date('Y-m-d', strtotime('+1 years'));
                    if ($billing_cycle === "month")
                        $parent->next_renews_at = date('Y-m-d', strtotime('+1 month'));
                }
                $parent->save();
                Transaction::create(['ref' => $paymentIntent]);

                return view('parents.recharge')->with(
                    [
                        'success' => 'Wallet charged successfully',
                        'parent' => $parent
                    ]
                );

                Abort(404);
            } else if ($payment_gateway_enable == "razorpay") {
                $razorpay_key = Setting::where('name', 'Razorpay Key Id')->first()->value;
                $razorpay_secret = Setting::where('name', 'Razorpay Key Secret')->first()->value;
                $razorpay_api = new Api($razorpay_key, $razorpay_secret);

                $requestData = $request->all();
                if (!(array_key_exists('payment_id', $requestData))) {
                    return response()->json(['errors' => ['Payment' => ['Payment can not be created']]], 422);
                }
                // get the paymentIntent and plan id sent from front end
                $paymentId = $requestData['payment_id'];

                $paymentIntentSaved = Transaction::where('ref', $paymentId)->first();
                if ($paymentIntentSaved) {
                    Abort(404);
                }

                $payment = $razorpay_api->payment->fetch($paymentId);
                $parentID = $payment->notes->parent;
                if ($parentID) {
                    $id = $this->get_id($parentID);
                    $parent = Parent_::with('school.plan')->findOrFail($id);
                    $amount = $payment['amount'] / 100;
                    $parent->wallet += $amount;
                    if ($parent->next_renews_at == null) {
                        $billing_cycle = Setting::where('name', 'Billing cycle')->first()->value;
                        if ($billing_cycle === "year")
                            $parent->next_renews_at = date('Y-m-d', strtotime('+1 years'));
                        if ($billing_cycle === "month")
                            $parent->next_renews_at = date('Y-m-d', strtotime('+1 month'));
                    }
                    $parent->save();

                    Transaction::create(['ref' => $paymentId]);

                    return view('parents.recharge')->with(
                        [
                            'success' => 'Wallet charged successfully',
                            'parent' => $parent
                        ]
                    );
                }
            } else if ($payment_gateway_enable == "flutterwave") {
                $flutterwave_secret = Setting::where('name','Flutterwave Secret Key')->first()->value;
                Log::info(" handlePayment received");
                $tx_ref = $request->input('tx_ref');
                $transaction_id = $request->input('transaction_id');
                Log::info($tx_ref . " received");
        
                if($tx_ref)
                {
                    $previousTrans = Transaction::where('ref', $tx_ref)->first();
                    if($previousTrans)
                    {
                        Abort(404);
                    }
                }        
                list($school_email, $parentId, $planId, $amount) = 
                $this->verifyFlutterwaveTransaction($transaction_id, $flutterwave_secret);
                
                //charge parent
                $parent = Parent_::findOrFail($parentId);
                $parent->wallet += $amount;
                if($parent->next_renews_at == null)
                {
                    $billing_cycle = Setting::where('name', 'Billing cycle')->first()->value;
                    if ($billing_cycle === "year")
                        $parent->next_renews_at = date('Y-m-d', strtotime('+1 years'));
                    if ($billing_cycle === "month")
                        $parent->next_renews_at = date('Y-m-d', strtotime('+1 month'));
                }
                $parent->save();

                Transaction::create(['ref' => $tx_ref]);

                return view('parents.recharge')->with(
                    [
                        'success' => 'Wallet charged successfully',
                        'parent' => $parent
                    ]
                );
            } else if ($payment_gateway_enable == "paytabs") {
                $params = $request->all();
                Log::info("params = " . json_encode($params));

                $orderId = $params['cart_id'];

                $pieces = explode("_", $orderId);
                $parentId = $pieces[0];
                $planId = $pieces[1];
        
                $currency = Setting::where('name', 'Currency')->first()->value;
                $tran_ref = $params['tran_ref'];

                if($tran_ref)
                {
                    $previousTrans = Transaction::where('ref', $tran_ref)->first();
                    if($previousTrans)
                    {
                        Abort(404);
                    }
                } 
                
                $server_key = Setting::where('name', 'Paytabs API Server Key')->first()->value;
                $profile_id = Setting::where('name', 'Paytabs Profile Id')->first()->value;
                $region = Setting::where('name', 'Paytabs Region')->first()->value;
                
                $pay = paypage::setKeys($region, $currency, $profile_id, $server_key);
                $transaction = $pay->queryTransaction($tran_ref);
                Log::info("transaction = " . json_encode($transaction));
                if($transaction->success == true)
                {
                    //charge parent
                    $parent = Parent_::findOrFail($parentId);
                    $parent->wallet += $transaction->cart_amount;

                    if($parent->next_renews_at == null)
                    {
                        $billing_cycle = Setting::where('name', 'Billing cycle')->first()->value;
                        if ($billing_cycle === "year")
                            $parent->next_renews_at = date('Y-m-d', strtotime('+1 years'));
                        if ($billing_cycle === "month")
                            $parent->next_renews_at = date('Y-m-d', strtotime('+1 month'));
                    }
                    $parent->save();
    
                    Transaction::create(['ref' => $tran_ref]);  
                }
            }
        }
    }

    public function verifyFlutterwaveTransaction($transaction_id, $flutterwave_secret)
    {
        $curl = curl_init();

        curl_setopt_array($curl, array(
        CURLOPT_URL => "https://api.flutterwave.com/v3/transactions/".$transaction_id."/verify",
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_ENCODING => "",
        CURLOPT_MAXREDIRS => 10,
        CURLOPT_TIMEOUT => 0,
        CURLOPT_FOLLOWLOCATION => true,
        CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
        CURLOPT_CUSTOMREQUEST => "GET",
        CURLOPT_HTTPHEADER => array(
            "Content-Type: application/json",
            "Authorization: Bearer ".$flutterwave_secret
        ),
        ));

        $response = curl_exec($curl);

        curl_close($curl);

        $payload = json_decode($response, true);

        if(isset($payload["status"]) && $payload["status"] == "success")
        {
            if(isset($payload["data"]) && isset($payload["data"]["customer"]) 
            && isset($payload["data"]["customer"]["email"]) 
            && isset($payload["data"]["meta"])
            && isset($payload["data"]["id"]))
            {
                $school_email = $payload["data"]["customer"]["email"];
                $meta = $payload["data"]["meta"];
                $schoolId = $meta['school'];
                $planId = $meta['plan'];
                $amount = $payload["data"]["amount"];
                return array($school_email, $schoolId, $planId, $amount);
            }
        }
        else
        {
            return abort(404);
        }
    }
}
