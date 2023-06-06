<?php

namespace App\Http\Controllers\Plans;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
use Validator;
use App\Plan;
use DB;
use App\Driver;
use App\Setting;
use App\SettingType;

use App\Http\Traits\PaymentHandle;
use App\Http\Traits\SwitchPlans;
use App\CustomPlanSetting;
class PlansController extends Controller
{
    use SwitchPlans;
    use PaymentHandle;

    /* show all plans*/
    public function showIndex (Request $request)
    {
        $can_create = true;
        
        return view('plans.index', ['can_create' => $can_create]);
    }

    public function customFilter(Request $request)
    {
        $this->deleteCustomPlans();
        //get plans based on search term if any
        $query = Plan::query();
        if($request->search) {
            $query->where('name', 'LIKE', '%'.$request->search.'%');
        }
        $query->with('schools')->where('is_custom', 1);
        // sort the obtained plans
        if($request->input('orderBy.direction')) {
            $plans = $query->orderBy($request->input('orderBy.column'), $request->input('orderBy.direction'))
                    ->paginate($request->input('pagination.per_page'));
        }
        else
        {
            $plans = $query->orderBy('id')->get();
        }
        return ['plans' => $plans,
        'price_per_driver' => CustomPlanSetting::first()->price_per_driver,
        'price_per_seat' => CustomPlanSetting::first()->price_per_seat,
        "currency" => Setting::where('name', 'Currency')->first()->value,
        "billing_cycle" => Setting::where('name', 'Billing cycle')->first()->value];
    }

    //delete custom plans that are not assigned to schools and created from one day ago
    public function deleteCustomPlans()
    {
        $plans = Plan::where('is_custom', 1)->get();
        foreach($plans as $plan) {
            if($plan->schools->count() == 0 && $plan->created_at < now()->subDays(1)) {
                $plan->forceDelete();
            }
        }
    }

    /* get all plans based on filter */
    public function filter (Request $request)
    {
        //get plans based on search term if any
        $query = Plan::query();
        if($request->search) {
            $query->where('name', 'LIKE', '%'.$request->search.'%');
        }
        $query->where('is_custom', 0);
        // sort the obtained plans
        if($request->input('orderBy.direction')) {
            $plans = $query->orderBy($request->input('orderBy.column'), $request->input('orderBy.direction'))
                    ->paginate($request->input('pagination.per_page'));
        }
        else
        {
            $plans = $query->get();
        }
        return ['plans' => $plans,
        "currency" => Setting::where('name', 'Currency')->first()->value,
        "billing_cycle" => Setting::where('name', 'Billing cycle')->first()->value];
    }
    /* get specific plan */
    public function show ($plan)
    {
        return ['plan' => Plan::findOrFail($plan),
        "currency" => Setting::where('name', 'Currency')->first()->value,
        "billing_cycle" => Setting::where('name', 'Billing cycle')->first()->value];
    }
    /* get all plans */
    public function all ()
    {
        return [
            'plans' => Plan::where('is_custom', 0)->orderBy('price')->get(), 
            'is_payment_enabled' => $this->is_payment_enabled(),
            'price_per_driver' => CustomPlanSetting::first()->price_per_driver,
            'price_per_seat' => CustomPlanSetting::first()->price_per_seat,
            "currency" => Setting::where('name', 'Currency')->first()->value,
            "billing_cycle" => Setting::where('name', 'Billing cycle')->first()->value
        ];
    }
    /* delete a specific plan */
    public function destroy($plan)
    {
        $plan = Plan::with('schools')->find($plan);
        if($plan->is_free!=1 && $plan->is_pay_as_you_go!=1 ) //not free plan and not pay_as_you_go
        {
            if(sizeof($plan->schools)!=0)
            {
                return response()->json(['errors' => ['Plan'=> ["Can not delete plan because there are schools assigned to this plan!!"]]], 422);
            }
            //delete it from database
            $plan->forceDelete();
        }  
    }
    /* create a custom new plan */
    public function createCustom (Request $request)
    {
        // make nice names for validation
        $niceNames = [
            'allowed_drivers' => 'maximum number of drivers',
        ]; 
        //validate the request
        $this->validate($request, [
            'name' => 'required|string|max:255',
            'allowed_drivers' => 'required|numeric|min:-1',
            'allowed_children' => 'required|numeric|min:-1',
        ], [], $niceNames);

        //calculate the price
        $price = $this->calculatePrice($request->allowed_drivers, $request->allowed_children);
        // create local plan on database
        $local_plan = Plan::create([
            'name' => $request->name,
            'price' => $price,
            'allowed_drivers' => $request->allowed_drivers,
            'allowed_children' => $request->allowed_children,
            'is_custom' => 1,
        ]);
        return $local_plan;
    }

    public function addToCustom (Request $request)
    {
        // make nice names for validation
        $niceNames = [
            'addon_drivers' => 'number of drivers',
            'addon_children' => 'number of children',
        ]; 
        //validate the request
        $this->validate($request, [
            'addon_drivers' => 'required|numeric|min:-1',
            'addon_children' => 'required|numeric|min:-1',
        ], [], $niceNames);
        //check if current school is on custom plan
        $school = Auth::user();
        //get the current plan
        $plan = $school->plan;
        if(!$plan->is_custom)
        {
            return response()->json(['errors' => ['School'=> ["School is not on custom plan!!"]]], 422);
        }
        $planPrice = $plan->price;
        //calculate the price of the addon
        $addonPrice = $this->calculatePrice($request->addon_drivers, $request->addon_children);
        $totalDrivers = $plan->allowed_drivers + $request->addon_drivers;
        $totalChildren = $plan->allowed_children + $request->addon_children;
        // create local plan on database
        $local_plan = Plan::create([
            'name' => $plan->name,
            'price' => $planPrice + $addonPrice,
            'allowed_drivers' => $totalDrivers,
            'allowed_children' => $totalChildren,
            'is_custom' => 1,
            'is_addon' => 1,
            'addon_price' => $addonPrice,
        ]);
        return $local_plan;
    }

    public function setCustomPrice(Request $request)
    {
        //make nice names for validation
        $niceNames = [
            'price_per_driver' => 'price per driver',
            'price_per_seat' => 'price per seat',
        ];
        //validate the request
        $this->validate($request, [
            'price_per_driver' => 'required|numeric|min:0',
            'price_per_seat' => 'required|numeric|min:0',
        ], [], $niceNames);

        //update the price
        CustomPlanSetting::first()->update([
            'price_per_driver' => $request->price_per_driver,
            'price_per_seat' => $request->price_per_seat,
        ]);
        return response()->json(['success' => true]);
    }

    public function calculatePrice($drivers, $children)
    {
        $price = 0;
        $price += CustomPlanSetting::first()->price_per_driver * $drivers;
        $price += CustomPlanSetting::first()->price_per_seat * $children;
        return $price;
    }

    /* create a new plan */
    public function store (Request $request)
    {
        // make nice names for validation
        $niceNames = [
            'allowed_drivers' => 'maximum number of drivers',
        ]; 
        //validate the request
        $this->validate($request, [
            'name' => 'required|string|unique:plans,name,'.$request->id,
            'price' => 'required|numeric|min:0|not_in:0',
            'allowed_drivers' => 'required|numeric|min:-1',
            'allowed_children' => 'required|numeric|min:-1',
            'is_pay_as_you_go' => 'nullable|in:0'
        ], [], $niceNames);

        // create local plan on database
        $local_plan = Plan::create([
            'name' => $request->name,
            'price' => $request->price,
            'allowed_drivers' => $request->allowed_drivers,
            'allowed_children' => $request->allowed_children,
        ]);
        return $local_plan;
    }
    /* update a specific plan's data. Note that the method is allowed only for Free plan*/
    public function update (Request $request)
    {
        $niceNames = [
            'allowed_drivers' => 'maximum number of drivers',
        ]; 
        $this->validate($request, [
            'name' => 'required|string',
            'allowed_drivers' => 'required|numeric|min:-1',
            'allowed_children' => 'required|numeric|min:-1',
            'price' => 'required|numeric|min:0',
        ], [], $niceNames);

        //update plan
        // get the local plan to be updated
        $plan = Plan::with('schools')->find($request->id);
        if($plan)
        {
            if($plan->is_free==1)
            {
                //update its attributes and save
                $plan->allowed_drivers = $request->allowed_drivers;
                $plan->allowed_children = $request->allowed_children;
                foreach ($plan->schools as $key => $school_account) {
                    $this->updatePlanAndAdjustLimit($school_account, $plan);
                }
                $plan->save();
                return $plan; 
            }
            if($request->price == 0)
            {
                return response()->json(['errors' => ['Plan'=> ['price can not be 0']]], 422);
            }
            if($plan->is_pay_as_you_go==1)
            {
                $plan->price = $request->price;
                $plan->save();
                return $plan; 
            }
            
            $plan->name = $request->name;
            $plan->allowed_drivers = $request->allowed_drivers;
            $plan->allowed_children = $request->allowed_children;
            $plan->price = $request->price;
            $plan->save();
            foreach ($plan->schools as $key => $school_account) {
                $this->updatePlanAndAdjustLimit($school_account, $plan);
            }
            return $plan; 
        }
        else {
            return response()->json(['errors' => ['Plan'=> ['plan can not be updated']]], 422);
        }
    }

}
