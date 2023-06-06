<?php

namespace App\Http\Controllers\Activation;

use App\AuthSetting;
use App\Http\Controllers\Controller;
use BtcId\BtcId\BtcId;
use Illuminate\Http\Request;

use Validator;
use DB;

class ActivationController extends Controller
{
    public function load(Request $request)
    {
        $authSetting = AuthSetting::first();
        if(!($authSetting == null || $authSetting->secure_key == null
        || $authSetting->u1 == null
        || $authSetting->u2 == null
        || $authSetting->u3 == null))
        {
            return response()->json(['secure_key' => $authSetting->secure_key]);
        }
        return response()->json(['secure_key' => null]);
    }
    //
    public function activate(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'secure_key' => 'required'
        ]);

        if ($validator->fails()) {
            //pass validator errors as errors object for ajax response
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $btcId = new BtcId();
        $payload = $btcId->validate($request->secure_key);

        if($payload)
        {
            DB::beginTransaction();
            try {
                AuthSetting::truncate();
                AuthSetting::create($payload);
                //save
                DB::commit();
            } catch (\Exception $e) {
                DB::rollback();
                return response()->json(['errors' => ['Error'=> [$e->getMessage()]]], 422);
            }
        }
        else
        {
            AuthSetting::truncate();
            return response()->json(['errors' => ['Error'=> ['error in activation']]], 422);
        }

        return response()->json(['success' => ['activated successfully']]);
    }
}
