<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;

use Exception;

use Laravel\Cashier\Cashier;

use Illuminate\Routing\Controller;
use Symfony\Component\HttpFoundation\Response;

use Illuminate\Support\Facades\Log;

use \App\Plan;
use \App\User;
use \App\Setting;

use \App\Http\Traits\SwitchPlans;
use App\Parent_;
use App\SettingType;
use App\Transaction;
use Razorpay\Api\Api;
use \App\Http\Traits\AuthSec;
use App\Http\Traits\PaymentHandle;

class PaymentReturnController extends Controller
{
    use SwitchPlans;
    use AuthSec;
    use PaymentHandle;
    
    public function razorSuccessCharge(Request $request)
    {
        $params = $request->all();
        Log::info("received " . json_encode($params['razorpay_payment_id']));

        $paymentId = $params['razorpay_payment_id'];

        $paymentIdSaved = Transaction::where('ref', $paymentId)->first();
        if ($paymentIdSaved) {
            Abort(404);
        }


        $payment_gateway_enable = $this->is_payment_enabled();

        if ($payment_gateway_enable == "razorpay") {
            $razorpay_key = Setting::where('name', 'Razorpay Key Id')->first()->value;
            $razorpay_secret = Setting::where('name', 'Razorpay Key Secret')->first()->value;
            $razorpay_api = new Api($razorpay_key, $razorpay_secret);
            $payment = $razorpay_api->payment->fetch($paymentId);

            if (isset($payment->notes) && isset($payment->notes->school)) {
                $schoolID = $payment->notes->school;
            } else {
                $schoolID = null;
            }
            if (isset($payment->notes) && isset($payment->notes->plan)) {
                $planID = $payment->notes->plan;
            } else {
                $planID = null;
            }
            if ($planID) {
                $plan = Plan::findOrFail($planID);
                if ($schoolID) {
                    $school = User::findOrFail($schoolID);
                    if ($school && $plan) {
                        $this->updatePlanAndAdjustLimit($school, $plan);
                        Transaction::create(['ref' => $paymentId]);
                    }
                }
            }
        }
        return redirect('/');
    }
}
