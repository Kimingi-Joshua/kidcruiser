<?php

namespace App\Policies;

use App\User;
use App\Driver;
use App\Plan;
use Carbon\Carbon;
use Illuminate\Auth\Access\HandlesAuthorization;

class DriverPolicy
{
    use HandlesAuthorization;

    /**
     * Determine whether the school account can view the driver.
     *
     * @param  \App\User  $school
     * @param  \App\Driver  $driver
     * @return mixed
     */
    public function view(User $school, Driver $driver)
    {
        //
        return $school->id === $driver->school_id;
    }

    /**
     * Determine whether the school account can create drivers.
     *
     * @param  \App\User  $school
     * @return mixed
     */
    public function create(User $school)
    {
        // check if the current school account can add a new driver while still in plan limit
        // get current plan limit
        $allowed_drivers = $school->plan->allowed_drivers;
        //get the current number of drivers in school account
        $current_drivers = $school->drivers->count();
        if($school->plan->is_pay_as_you_go ==1)
        {
            return true;
        }
        else if ($school->plan->is_free ==1)
        {
            if($allowed_drivers >= ($current_drivers + 1))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            $today = Carbon::now();
            $plan_renews_at = Carbon::parse($school->plan_renews_at);
            if($today->gt($plan_renews_at)){
                return false;
            }
            else
            {
                if($allowed_drivers == -1)
                {
                    return true;
                }
                else 
                {
                    return $allowed_drivers >= ($current_drivers + 1);
                }
            }
        }
    }

    /**
     * Determine whether the school account can delete the driver.
     *
     * @param  \App\User  $school
     * @param  \App\Driver  $driver
     * @return mixed
     */
    public function delete(User $school, Driver $driver)
    {
        //
        return $school->id === $driver->school_id;
    }
}
