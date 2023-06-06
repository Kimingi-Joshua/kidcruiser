<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class School extends Model
{
    protected $appends = ['isPayAsYouGo'];

    public function getIsPayAsYouGoAttribute()
    {
        return $this->plan->is_pay_as_you_go;
    }
    public function plan()
    {
        return $this->belongsTo('App\Plan')->withTrashed();
    }
}
