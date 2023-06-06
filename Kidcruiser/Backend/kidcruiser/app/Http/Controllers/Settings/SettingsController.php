<?php

namespace App\Http\Controllers\Settings;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

use App\Setting;
use App\SettingType;
use DB;
use Validator;
class SettingsController extends Controller
{
    /* get all settings */
    public function getSettings()
    {
        return SettingType::with(['settings'])->get();
    }
    /* update the values of the settings */
    public function updateSettings (Request $request)
    {
        $setting_types = $request->all();
        DB::beginTransaction();
        try
        {
            // update the corresponding value for each setting
            foreach ($setting_types as $setting_type) {
                $sms_gateway = 'none';
                if($setting_type['name'] === "SMS")
                {
                    $enabled_value = 0;
                    foreach ($setting_type["settings"] as $setting) {
                        if($setting["name"] === 'SMS Gateway')
                        {
                            if(array_key_exists('value', $setting) && $setting["value"] !== "none")
                            {
                                $enabled_value = 1;
                                $sms_gateway = $setting["value"];
                            }
                            break;
                        }
                    }
                    $setting_type["enabled"] = $enabled_value;
                }
                if($setting_type['name'] === "Payment")
                {
                    $enabled_value = 0;
                    foreach ($setting_type["settings"] as $setting) {
                        if($setting["name"] === 'Payment Gateway')
                        {
                            if(array_key_exists('value', $setting) && $setting["value"] !== "none")
                            {
                                $enabled_value = 1;
                                $sms_gateway = $setting["value"];
                            }
                            break;
                        }
                    }
                    $setting_type["enabled"] = $enabled_value;
                }
                DB::table('setting_types')
                ->where('id', '=', $setting_type["id"])
                ->update([
                    'enabled' =>  $setting_type["enabled"],
                    'updated_at' => now(),
                ]);

                if($setting_type['name'] === "SMS")
                {
                    foreach ($setting_type["settings"] as $setting) {
                        if($setting["name"] === 'SMS Gateway')
                        {
                            if(array_key_exists('id', $setting) && array_key_exists('value', $setting))
                            {
                                DB::table('settings')
                                ->where('id', '=', $setting["id"])
                                ->update([
                                    'value' => $setting["value"],
                                    'updated_at' => now(),
                                ]);
                            }
                        }
                        else
                        {
                            // Test if setting name contains the sms_gateway 
                            if(strpos(strtolower($setting['name']), $sms_gateway) !== false){
                                
                                $validator = Validator::make($setting, [
                                    'value' => 'required']);
    
                                    if ($validator->fails()) {
                                        //pass validator errors as errors object for ajax response
                                        return response()->json(['errors' => $setting["name"] . ' is required'], 422);
                                    }
    
                                    if(array_key_exists('id', $setting) && array_key_exists('value', $setting))
                                    {
                                        DB::table('settings')
                                        ->where('id', '=', $setting["id"])
                                        ->update([
                                            'value' => $setting["value"],
                                            'updated_at' => now(),
                                        ]);
                                    }
                            }
                        }

                    }
                }
                else if($setting_type['name'] === "Payment")
                {
                    foreach ($setting_type["settings"] as $setting) {
                        if($setting["name"] === 'Payment Gateway')
                        {
                            if(array_key_exists('id', $setting) && array_key_exists('value', $setting))
                            {
                                DB::table('settings')
                                ->where('id', '=', $setting["id"])
                                ->update([
                                    'value' => $setting["value"],
                                    'updated_at' => now(),
                                ]);
                            }
                        }
                        else
                        {
                            // Test if setting name contains the sms_gateway 
                            if(strpos(strtolower($setting['name']), $sms_gateway) !== false){
                                
                                $validator = Validator::make($setting, [
                                    'value' => 'required']);
    
                                    if ($validator->fails()) {
                                        //pass validator errors as errors object for ajax response
                                        return response()->json(['errors' => $setting["name"] . ' is required'], 422);
                                    }
    
                                    if(array_key_exists('id', $setting) && array_key_exists('value', $setting))
                                    {
                                        DB::table('settings')
                                        ->where('id', '=', $setting["id"])
                                        ->update([
                                            'value' => $setting["value"],
                                            'updated_at' => now(),
                                        ]);
                                    }
                            }
                        }

                    }
                }
                else
                {
                    if($setting_type["enabled"] === null || $setting_type["enabled"] === 1)
                    {
                        foreach ($setting_type["settings"] as $setting) {
                            $validator = Validator::make($setting, [
                            'value' => 'required']);
                
                            if ($validator->fails()) {
                                //pass validator errors as errors object for ajax response
                                return response()->json(['errors' => $setting["name"] . ' is required'], 422);
                            }
    
                            if(array_key_exists('id', $setting) && array_key_exists('value', $setting))
                            {
                                DB::table('settings')
                                ->where('id', '=', $setting["id"])
                                ->update([
                                    'value' => $setting["value"],
                                    'updated_at' => now(),
                                ]);
                            }
                        }
                    }

                }
            }
            // when done commit
            DB::commit();
            return response()->json(['success' => ['settings updated successfully']]);
        }
        catch (\Exception $e)
        {
            dd($e);
            // rollback if errors
            DB::rollback();
            return response()->json(['errors' => ['settings not updated']], 500);
        }
    }

    public function getPrivacyPolicy(Request $request)
    {
        $privacy = file_get_contents(public_path('privacy.html'));

        return response()->json(['privacy' => $privacy]);
    }

    public function updatePrivacyPolicy(Request $request)
    {
        //validate the request
        $validator = Validator::make($request->all(), [
            'privacy' => 'required|string',
        ]);

        if ($validator->fails()) {
            //pass validator errors as errors object for ajax response
            return response()->json(['errors' => $validator->errors()], 422);
        }

        file_put_contents(public_path('privacy.html'), $request->privacy);

        return response()->json(['success' => ['Privacy Policy updated successfully']]);
    }

    public function updateTerms(Request $request)
    {
        //validate the request
        $validator = Validator::make($request->all(), [
            'terms' => 'required|string',
        ]);

        if ($validator->fails()) {
            //pass validator errors as errors object for ajax response
            return response()->json(['errors' => $validator->errors()], 422);
        }

        file_put_contents(public_path('terms.html'), $request->terms);

        return response()->json(['success' => ['Terms updated successfully']]);
    }

    public function getTerms(Request $request)
    {
        $terms = file_get_contents(public_path('terms.html'));

        return response()->json(['terms' => $terms]);
    }

}
