<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class AuthSetting extends Model
{
    //
    protected $guarded = ['id', 'created_at', 'updated_at', 'deleted_at'];
}
