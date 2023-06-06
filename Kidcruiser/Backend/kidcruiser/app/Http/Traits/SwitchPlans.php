<?php

namespace App\Http\Traits;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

use DB;
use App\Driver;
use App\Parent_;
use App\Plan;
use App\School;
use App\Child;
use App\Setting;
use App\User;
use Carbon\Carbon;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;
use App\Http\Traits\PaymentHandle;

trait SwitchPlans
{
    use PaymentHandle;
    /* update plan and adjust #drivers to plan limit */
    public function updatePlanAndAdjustLimit($school, $plan)
    {
        if($school->is_super_admin_account)
            return;
        DB::transaction(function() use ($school, $plan) { 
            $school = User::with('drivers', 'parents')->find($school->id);
            $school->plan_id = $plan->id;
            if($plan->is_free != 1 && $plan->is_pay_as_you_go != 1) //not free and not pay_as_you_go
            {
                if(!$plan->is_addon) //not addon
                {
                    $billing_cycle = Setting::where('name', 'Billing cycle')->first()->value;
                    //renew plan
                    if($billing_cycle === "year")
                        $school->plan_renews_at = date('Y-m-d', strtotime('+1 years'));
                    if($billing_cycle === "month")
                        $school->plan_renews_at = date('Y-m-d', strtotime('+1 month'));
                }
            }
            else
                $school->plan_renews_at = null;
            
            $plan->is_addon = false;
            $plan->save();
            $school->save();
            // get current plan drivers limit
            $allowed_drivers = $school->plan->allowed_drivers;
            $allowed_children = $plan->allowed_children;
            //get the current number of drivers in school account
            $current_drivers = $school->drivers->count();
            $current_children = 0;
            for ($x = 0; $x < $school->parents->count(); $x++) {
                for ($y = 0; $y < $school->parents[$x]->children->count(); $y++) {
                    $current_children++;
                }
            }
            if($allowed_drivers == -1 || $plan->is_pay_as_you_go == 1 || $allowed_children == -1) {
                if ($allowed_drivers == -1 || $plan->is_pay_as_you_go == 1) //unlimited plan
                {
                    //restore all removed drivers
                    $driversToAdd = Driver::onlyTrashed()->where('school_id', $school->id)->get();
                    Driver::whereIn('id', $driversToAdd->pluck('id'))->restore();
                }
                if ($allowed_children == -1) //unlimited plan
                {
                    $childrenToAdd = Child::onlyTrashed()->whereIn('parent_id', $school->parents->pluck('id'))->get();
                    Child::whereIn('id', $childrenToAdd->pluck('id'))->restore();
                }
            }
            else 
            {
                if ($current_drivers > $allowed_drivers) //downgrade
                {
                    //remove excess drivers
                    $driversToRemoveCount = $current_drivers - $allowed_drivers;
                    $driversToRemove = Driver::where('school_id', $school->id)
                        ->latest()->take($driversToRemoveCount)->get();
                    Driver::whereIn('id', $driversToRemove->pluck('id'))->delete();
                }
                if ($current_children > $allowed_children) //downgrade
                {
                    //remove excess children
                    $childrenToRemoveCount = $current_children - $allowed_children;
                    $childrenToRemove = Child::whereIn('parent_id', $school->parents->pluck('id'))
                        ->latest()->take($childrenToRemoveCount)->get();
                    Child::whereIn('id', $childrenToRemove->pluck('id'))->delete();
                }
                if ($current_drivers < $allowed_drivers) //upgrade
                {
                    //restore removed drivers if any
                    $driversToAddCount = $allowed_drivers - $current_drivers;
                    $driversToAdd = Driver::onlyTrashed()
                        ->where('school_id', $school->id)->latest()
                        ->take($driversToAddCount)->get();
                    Driver::whereIn('id', $driversToAdd->pluck('id'))->restore();
                }
                if ($current_children < $allowed_children) //upgrade
                {
                    //restore removed drivers if any
                    $childrenToAddCount = $allowed_children - $current_children;
                    $childrenToAdd = Child::onlyTrashed()->whereIn('parent_id', $school->parents->pluck('id'))
                        ->latest()->take($childrenToAddCount)->get();
                    Child::whereIn('id', $childrenToAdd->pluck('id'))->restore();
                }
            }
        });
    }

    public function checkSubscriptions()
    {
        $this->checkParentSubscriptions();
        $this->checkSchoolsSubscriptions();
    }

    public function checkParentSubscriptions()
    {
        $parents = Parent_::with(['school.plan', 'children'])->where('next_renews_at', '<', Carbon::now()->subDays(1))->get();
        foreach ($parents as $key => $parent) {
            if (
                $parent->school->plan != null &&
                $parent->school->plan->is_pay_as_you_go == 1
            ) {
                //check expiration for this school
                Log::info("Check the subscription of " . $parent->name);
                $today = date('Y-m-d', time());
                Log::info("today = " . json_encode($today));
                Log::info("next_renews_at = " . json_encode($parent->next_renews_at));

                $childCount = count($parent->children);
                $requiredPrice = $parent->school->plan->price * $childCount;

                Log::info("requiredPrice = " . json_encode($requiredPrice));

                //deduct from wallet
                if ($parent->wallet > $requiredPrice) {
                    $parent->wallet -= $requiredPrice;
                    $billing_cycle = Setting::where('name', 'Billing cycle')->first()->value;
                    //renew plan
                    if ($billing_cycle === "year")
                        $parent->next_renews_at = date('Y-m-d', strtotime('+1 years'));
                    if ($billing_cycle === "month")
                        $parent->next_renews_at = date('Y-m-d', strtotime('+1 month'));

                    $parent->save();
                } else {
                    Log::info("insufficient funds for the parent " . $parent->tel_number);
                }
            }
        }
    }

    public function checkSchoolsSubscriptions()
    {
        //$schools = User::with('plan')->get();
        $schools = User::with('plan')->where('plan_renews_at', '<', Carbon::now()->subDays(1))->get();
        foreach ($schools as $key => $school) {
            if($school->plan != null && 
            $school->plan->is_free != 1 &&
            $school->plan->is_pay_as_you_go != 1)
            {
                //check expiration for this school
                Log::info("Check the subscription of ". $school->name);
                $today = date('Y-m-d', time());
                Log::info("today = " . json_encode($today));
                Log::info("plan_renews_at = " . json_encode($school->plan_renews_at));
                //try to charge the schoool to renew
                if($this->tryToRenew($school))
                {
                    $this->renewSubscriptions($school);
                }
                else
                {
                    $latePay = Carbon::parse($school->plan_renews_at)->diffInDays(Carbon::now());
                    Log::info("late Pay ". $latePay);
                    if($latePay > 2)
                    {
                        $free_plan = Plan::where('is_free',1)->first();
                        $this->updatePlanAndAdjustLimit($school, $free_plan);
                    }
                }
            }
        }
    }

    public function sendRemainderEmail($school, $paymentLink)
    {
        try {
            //get email from .env
            $email = env('MAIL_USERNAME');
            //get app name from .env
            $app_name = env('APP_NAME');
            //send an email to school to renew
            Mail::send(
                'mail.template',
                [
                    'school' => $school,
                    'paymentLink' => $paymentLink,
                    "company_title" => Setting::where('name', 'Company title')->first()->value,
                    "company_website" => Setting::where('name', 'Company website')->first()->value,
                ],
                function ($message) use ($school, $email, $app_name) {
                    $message->from($email, $app_name);
                    $message->to($school->email, $school->name);
                    $message->replyTo($email, $app_name);
                    $message->subject('Renewal reminder');
                }
            );
        } catch (\Throwable $th) {
            //throw $th;
        }
    }

    public function tryToRenew($school)
    {
        $payment_gateway_enable = $this->is_payment_enabled();
        $paymentLink = url('/') . '/plan';
        $this->sendRemainderEmail($school, $paymentLink);
        return $this->chargeOffline($school, $payment_gateway_enable);
    }
    public function renewSubscriptions($school)
    {
        Log::info("Account of ". $school->name ." renews the subscription");

        $billing_cycle = Setting::where('name', 'Billing cycle')->first()->value;
        //renew plan
        if($billing_cycle === "year")
            $school->plan_renews_at = date('Y-m-d', strtotime('+1 years'));
        if($billing_cycle === "month")
            $school->plan_renews_at = date('Y-m-d', strtotime('+1 month'));
            
        $school->save();
        Log::info("Subscription of ". $school->name ." is renewed successfully");
    }
}