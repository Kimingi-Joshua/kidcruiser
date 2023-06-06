<?php

namespace App\Http\Controllers\Profile;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
use App\Http\Helpers\Upload;
use Avatar;
use App\User;
use Validator;
use DB;
use App\Driver;
use App\Parent_;
use App\Plan;
use App\Setting;
use App\SettingType;

use App\Http\Traits\SwitchPlans;
use App\Transaction;
use Illuminate\Support\Facades\Log;
use App\Http\Traits\PaymentHandle;

class ProfileController extends Controller
{
    use SwitchPlans;
    use PaymentHandle;

    /* get the current logged school */
    public function getAuthUser ()
    {
        $school = User::with('plan')->find(Auth::id());
        return $school;
    }
    
    /* update the attributes of the current logged school */
    public function updateAuthUser (Request $request)
    {
        $this->validate($request, [
            'name' => 'required|string',
            'email' => 'required|email|unique:schools,email,'.Auth::id(),
            'country_code' => 'required|string|numeric|digits_between:1,4',
            'tel_number' => 'required|string|numeric|digits_between:1,15',
        ]);
        // get the current school
        $school = Auth::user();
        // update its attribute
        $school->name = $request->name;
        $school->email = $request->email;
        $school->country_code = $request->country_code;
        $school->tel_number = $request->tel_number;
        $school->save();
        // create an avatar image for the school account based on the school's name
        $avatar = Avatar::create($school->name)->getImageObject()->encode('png');
        Storage::put('avatars/'.$school->id.'/avatar.png', (string) $avatar);
        return $school;
    }
    /* update the password of the current logged school */
    public function updatePasswordAuthUser(Request $request)
    {
        
        $this->validate($request, [
            'current' => 'required|string',
            'password' => 'required|string|confirmed',
            'password_confirmation' => 'required|string'
        ]);
        // get the logged school
        $school = Auth::user();
        // make sure that the current password is correct
        if (!Hash::check($request->current, $school->password)) {
            return response()->json(['errors' => ['current'=> ['Current password does not match']]], 422);
        }
        // update the password and save
        $school->password = Hash::make($request->password);
        $school->save();
        return $school;
    }
    /* update the avatar image of the current school account */
    public function uploadAvatarAuthUser(Request $request)
    {
        $upload = new Upload();
        $avatar = $upload->upload($request->file, 'avatars/'.Auth::id())->resize(200, 200)->getData();

        $school = Auth::user();
        $school->avatar = $avatar['basename'];
        $school->save();

        return $school;
    }
    /* remove the avatar image of the current school account */
    public function removeAvatarAuthUser()
    {
        $school = Auth::user();
        $school->avatar = 'avatar.png';
        $school->save();
        return $school;
    }


    /* get specific plan */
    public function showPlan ($plan)
    {
        return [
            'plan' => Plan::findOrFail($plan), 
            "currency" => Setting::where('name', 'Currency')->first()->value,
            "billing_cycle" => Setting::where('name', 'Billing cycle')->first()->value
        ];
    }

    /* update the payment card of a school account */
    public function updatePayment (Request $request)
    {
        $school = Auth::user();
        $this->updateSchoolPlanPayment($request, $school);
    }
    
    public function handleFlutterwavePayment (Request $request)
    {
        $school = Auth::user();
        return $this->updateSchoolPlanPayment($request, $school);
    }

    public function handlePaytabsPayment (Request $request)
    {
        $school = Auth::user();
        return $this->updateSchoolPlanPayment($request, null);
    }

    public function handleSchoolPaytabsPayment (Request $request)
    {
        return view('profile.order_placed');
    }

    public function createOrder (Request $request)
    {
        $school = Auth::user();
        return $this->paypalCreateOrder($request, $school);
    }

    public function updateOrder (Request $request)
    {
        $school = Auth::user();
        return $this->paypalUpdateOrder($request, $school);
    }

    /* update the current plan of a school account */
    public function updateSchoolPlan(Request $request)
    {
        //get the current school
        $school = Auth::user();
        if($school->is_super_admin_account)
        {
            //super admin want to change a school account's plan
            $school_account = User::findOrFail($request->id);
            if(!$school_account)
                return response()->json(['errors' => ['School'=> ['school can not be selected']]], 422);
            //get the current plan
            $plan = Plan::findOrFail($request->plan["id"]);
            if(!$plan)
                return response()->json(['errors' => ['Plan'=> ['plan can not be selected']]], 422);
                        
            $this->updatePlanAndAdjustLimit($school_account, $plan);
        }
        else
        {
            //get the plan
            $plan = Plan::findOrFail($request->plan);
            
            if(!$plan)
                return response()->json(['errors' => ['Plan'=> ['plan can not be selected']]], 422);
            //get the current school
            $school = Auth::user();

            if ($plan->is_free==1 || $plan->is_pay_as_you_go==1) //if free plan is selected
            {
                //update school's plan
                $this->updatePlanAndAdjustLimit($school, $plan);
                return view('profile.plan');
            }
            $payment_gateway_enable = $this->is_payment_enabled();
            if ($payment_gateway_enable) {
                return $this->prepareSchoolPayment($school, $plan, $payment_gateway_enable);
            }
            else
            {
                if ($plan->is_free==1) //if free plan is selected
                {
                    //update school's plan
                    $this->updatePlanAndAdjustLimit($school, $plan);
                    return view('profile.plan');
                }
                else
                {
                    return view('profile.plan');
                }
            }
        }
    }
    

}
