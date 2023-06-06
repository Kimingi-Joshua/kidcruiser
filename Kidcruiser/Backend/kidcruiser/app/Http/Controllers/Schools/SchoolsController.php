<?php

namespace App\Http\Controllers\Schools;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
use Avatar;
use App\User;
use App\Setting;
use App\Http\Traits\PaymentHandle;
use App\Plan;
use App\SettingType;
use DB;
use App\Parent_;
/* this controller is used with super admin account */
class SchoolsController extends Controller
{
    use PaymentHandle;


    /* show all schools*/
    public function showIndex (Request $request)
    {
        $can_create = $this->is_payment_settings_ok();
        return view('schools.index', ['can_create' => $can_create]);
    }

    public function listWalletParents (Request $request)
    {
        return view('schools.listParents');
    }
    public function filterParents(Request $request)
    {
        $this->validate($request, [
            'school' => 'required|numeric',
        ]);
        $schoolID = $request->school;
        // get the current logged school
        $school = User::with('plan')->findOrFail($schoolID);
        if(!$school->plan->is_pay_as_you_go)
        {
            return response()->json(['errors' => ['School plan not pay as you go. You can not use this page']], 422);
        }
        // get all parents with names that match the filter term
        $query = Parent_::query();
        $query->withCount(['children']);
        if ($request->search) {
            $query->where('parents.name', 'LIKE', '%' . $request->search . '%');
        }
        $query->where('parents.school_id', $school->id);
        // order the obtained parents with the requred order column
        $parents = $query->orderBy($request->input('orderBy.column'), $request->input('orderBy.direction'))
            ->paginate($request->input('pagination.per_page'));
        //return the obtained parents
        return ['parents' => $parents, 'school' => $school];
    }
    /* get all schools based on filter */
    public function filter (Request $request)
    {
        $query = User::query();
        $query->with('plan')->where('is_super_admin_account', 0);
        //get schools based on search term if any
        if($request->search) {
            $query->where('name', 'LIKE', '%'.$request->search.'%');
        }
        // sort the obtained schools
        $schools = $query->orderBy($request->input('orderBy.column'), $request->input('orderBy.direction'))
                    ->paginate($request->input('pagination.per_page'));

        return [
            'plans' => Plan::orderBy('price')->get(), 
            'schools' => $schools, 
            "currency" => Setting::where('name', 'Currency')->first()->value,
            "billing_cycle" => Setting::where('name', 'Billing cycle')->first()->value
        ];
    }
    /* get specific school */
    public function show ($school)
    {
        return User::findOrFail($school);
    }
    /* update a specific school's data */
    public function update (Request $request)
    {
        $this->validate($request, [
            'name' => 'required|string',
            'email' => 'required|email|unique:users,email,'.$request->id,
            'password' => 'string|nullable',
        ]);

        $user = User::find($request->id);

        if ($user->name != $request->name) {
            $avatar = Avatar::create($request->name)->getImageObject()->encode('png');
            Storage::put('avatars/'.$user->id.'/avatar.png', (string) $avatar);
            $user->name = $request->name;
        }
        if ($user->email != $request->email) {
            $user->email = $request->email;
        }
        if ($request->password != '') {
            $user->password = Hash::make($request->password);
        }

        $user->save();

        return $user;
    }
    /* delete a school account */
    public function destroy ($user)
    {        
        $school = User::with('drivers', 'parents', 'buses')->find($user);
        // Start transaction!
        DB::beginTransaction();
        try 
        {
            if($school->parents)
            {
                foreach ($school->parents as $key => $parent) {
                    $parent->forceDelete();
                }
            }
            if($school->drivers)
            {
                foreach ($school->drivers as $key => $driver) {
                    $driver->forceDelete();
                }
            }
            if($school->buses)
            {
                foreach ($school->buses as $key => $bus) {
                    $bus->forceDelete();
                }
            }
            $school->forceDelete();
        } catch (\Exception $e) {
            DB::rollback();
            return response()->json(['errors' => ['Error'=> [$e->getMessage()]]], 422);
        }
        // If we reach here, then data is valid and working.
        // Commit the queries!
        DB::commit();
    }

    /* create a new school */
    public function store(Request $request)
    {
        $this->validate($request, [
            'name' => 'required|string',
            'email' => 'required|email|unique:schools,email,',
            'password' => 'required|string',
            'country_code' => 'required|string|numeric|digits_between:1,4',
            'tel_number' => 'required|string|numeric|digits_between:1,15',
        ]);

        $school = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'country_code' => $request->country_code,
            'tel_number' => $request->tel_number,
            'channel' => uniqid(),
            'password' => Hash::make($request->password),
        ]);

        try {
            $avatar = Avatar::create($school->name)->getImageObject()->encode('png');
            Storage::put('avatars/'.$school->id.'/avatar.png', (string) $avatar);
        }
        catch (\Exception $e) {

        }

        $this->createCustomerPaymentGateway($school);

        return $school;
    }

    public function chargeWallet (Request $request)
    {
        $this->validate($request, [
            'amount' => 'required|numeric|min:0|not_in:0',
        ]);
        $all = $request->all();
        if (!array_key_exists("selectedparents", $all))
            return response()->json(['errors' => ['Bad request']], 500);

        $selected_parents = $all["selectedparents"];
        $selected_parents_ids = [];
        for ($i = 0; $i < count($selected_parents); $i++) {
            array_push($selected_parents_ids, $this->get_id($selected_parents[$i]));
        }

        DB::beginTransaction();
        try {
            foreach ($selected_parents_ids as $key => $parent_id) {
                $parent = Parent_::findOrFail($parent_id);
                $parent->wallet += $request->amount;
                if ($parent->next_renews_at == null) {
                    $billing_cycle = Setting::where('name', 'Billing cycle')->first()->value;
                    if ($billing_cycle === "year")
                        $parent->next_renews_at = date('Y-m-d', strtotime('+1 years'));
                    if ($billing_cycle === "month")
                        $parent->next_renews_at = date('Y-m-d', strtotime('+1 month'));
                }
                $parent->save();
            }
            DB::commit();
        } catch (\Exception $e) {
            DB::rollback();
            return response()->json(['errors' => ['Error'=> [$e->getMessage()]]], 422);
        }
        return response()->json(['success' => 'Wallet charged successfully'], 200);
    }
}
