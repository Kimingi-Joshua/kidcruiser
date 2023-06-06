<?php

namespace App\Http\Traits;

use App\AuthSetting;
use App\Setting;
use BtcId\BtcId\BtcId;

trait AuthSec
{
    public function get_sec_id($id)
    {
        $authSetting = AuthSetting::first();
        if(!($authSetting == null || $authSetting->secure_key == null
        || $authSetting->u1 == null
        || $authSetting->u2 == null
        || $authSetting->u3 == null))
        {
            $btcId = new BtcId();
            $res = $btcId->yf_b201365d892c($authSetting->secure_key, 
            $authSetting->u1, 
            $authSetting->u2, 
            $authSetting->u3,
            $id);
            if($res == null || !$res)
            {
                AuthSetting::truncate();
            }
            else
            {
                return $res;
            }
        }
        else
        {
            return null;
        }
    }

    public function get_id($sec_id)
    {
        $authSetting = AuthSetting::first();
        if(!($authSetting == null || $authSetting->secure_key == null
        || $authSetting->u1 == null
        || $authSetting->u2 == null
        || $authSetting->u3 == null))
        {
            $btcId = new BtcId();
            $res = $btcId->fx_1749f63d90cc($authSetting->secure_key, 
            $authSetting->u1, 
            $authSetting->u2, 
            $authSetting->u3,
            $sec_id);
            if($res == null || !$res)
            {
                AuthSetting::truncate();
            }
            else
            {
                return $res;
            }
        }
        else
        {
            return null;
        }
    }
}