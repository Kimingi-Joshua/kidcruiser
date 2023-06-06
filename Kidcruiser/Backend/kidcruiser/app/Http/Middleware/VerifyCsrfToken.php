<?php

namespace App\Http\Middleware;

use Illuminate\Foundation\Http\Middleware\VerifyCsrfToken as Middleware;

class VerifyCsrfToken extends Middleware
{
    /**
     * Indicates whether the XSRF-TOKEN cookie should be set on the response.
     *
     * @var bool
     */
    protected $addHttpCookie = true;

    /**
     * The URIs that should be excluded from CSRF verification.
     *
     * @var array
     */
    protected $except = [
        //
        'api/parents/setZoneAlertDistance/',
        'api/parents/setSetting/',
        'api/parents/updatePosition/',
        'api/parents/updateChildAbsent/',
        'api/parents/getParentTelNumber/',
        'api/parents/verifyParentTelNumber/',
        
        'api/parents/getDriverLog/',
        'api/parents/getChildLog/',
	    'api/parents/validateReceipt/',
        'api/parents/getIOSProducts/',
        'api/drivers/updatePosition/',
        'api/drivers/updatePositionWithSpeed/',
        'api/drivers/getDriverTelNumber/',
        'api/drivers/authDriverTelNumber/',
        'api/drivers/verifyDriverTelNumber/',

        'api/drivers/checkInOut/',

        'api/profile/paypalCreateOrder',
        'api/profile/paypalUpdateOrder',

        'api/paytabs/school_payment/done',
        'api/paytabs/parent_payment/done',
        'api/paytabs/school_payment/return',
        'api/paytabs/parent_payment/return'
    ];
}
