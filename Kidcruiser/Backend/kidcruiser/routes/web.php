<?php

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/

use BtcId\BtcId\BtcId;

Route::get('/', function () {
    return view('welcome', [
        "plans" => App\Plan::where('is_custom', 0)->get(), 
        // Personalize your system
        "system_name" => App\Setting::where('name', 'System name')->first()->value,
        "company_title" => App\Setting::where('name', 'Company title')->first()->value,
        "company_website" => App\Setting::where('name', 'Company website')->first()->value,
        "company_email" => App\Setting::where('name', 'Company email')->first()->value,
        "company_telephone" => App\Setting::where('name', 'Company telephone')->first()->value,
        "facebook_link" => App\Setting::where('name', 'Facebook link')->first()->value,
        "twitter_link" => App\Setting::where('name', 'Twitter link')->first()->value,
        "instagram_link" => App\Setting::where('name', 'Instagram link')->first()->value,
        "linkedin_link" => App\Setting::where('name', 'Linkedin link')->first()->value,
        // Currency and billing cycle
        "currency" => App\Setting::where('name', 'Currency')->first()->value,
        "billing_cycle" => App\Setting::where('name', 'Billing cycle')->first()->value
        ]);
});

Route::get('/razor-success-charge', 'PaymentReturnController@razorSuccessCharge')->name('razorSuccessCharge');

Route::get(
    'recharge/{parent}',
    'Parents\ParentController@rechargeWallet');
    
Route::post(
'finalizePay',
'Parents\ParentController@finalizePay')->name('finalize-pay');

Route::get(
    'paid',
    'Parents\ParentController@updateParentWallet')->name('payment-done');

Route::post('/api/profile/paypalCreateOrder', 'Profile\ProfileController@createOrder');
Route::post('/api/profile/paypalUpdateOrder', 'Profile\ProfileController@updateOrder');

Route::get(
    'flutterwave/payment',
    'Profile\ProfileController@handleFlutterwavePayment'
)->name('flutterwave-payment');


Route::post(
    'api/paytabs/school_payment/done',
    'Profile\ProfileController@handlePaytabsPayment'
)->name('paytabs-school-payment-done');

Route::post(
    'api/paytabs/school_payment/return',
    'Profile\ProfileController@handleSchoolPaytabsPayment')->name('paytabs-school-payment-return');

Route::post(
    'api/paytabs/parent_payment/done',
    'Parents\ParentController@handlePaytabsParentWallet')->name('paytabs-parent-payment-done');

Route::post(
    'api/paytabs/parent_payment/return',
    'Parents\ParentController@returnPaytabsParentWallet')->name('paytabs-parent-payment-return');

// mobile APIs

//parents
Route::group(['namespace' => 'Parents'], function() {
    Route::group(['prefix' => 'api/parents'], function() {
        Route::get('/getParentTelNumber','ParentController@getParentTelNumber');
	    Route::get('/getSchoolBusDriverParentTelNumber','ParentController@getSchoolBusDriverParentTelNumber');
        Route::put('/updatePosition', 'ParentController@updatePosition');
        Route::put('/updateChildAbsent', 'ParentController@updateChildAbsent');
        Route::put('/setZoneAlertDistance', 'ParentController@setZoneAlertDistance');
        Route::put('/setSetting', 'ParentController@setSetting');
        Route::post('/verifyParentTelNumber', 'ParentController@verifyParentTelNumber');
        Route::post('/getDriverLog', 'ParentController@getDriverLog');
        Route::post('/getChildLog', 'ParentController@getChildLog');
        Route::post('/validateReceipt', 'ParentController@validateReceipt');
        Route::get('/getIOSProducts', 'ParentController@getIOSProducts');
        Route::get('/deleteParent', 'ParentController@deleteParent');
    });
});
//drivers
Route::group(['namespace' => 'Drivers'], function() {
    Route::group(['prefix' => 'api/drivers'], function() {
        Route::get('/getSchoolBusDriverTelNumber','DriverController@getSchoolBusDriverTelNumber');
        Route::put('/updatePosition', 'DriverController@updatePosition');
        Route::put('/updatePositionWithSpeed', 'DriverController@updatePositionWithSpeed');
        Route::post('/authDriverTelNumber', 'DriverController@authDriverTelNumber');
        Route::post('/verifyDriverTelNumber', 'DriverController@verifyDriverTelNumber');

        Route::post('/checkInOut', 'DriverController@checkInOut');
        Route::get('/deleteDriver', 'DriverController@deleteDriver');
    });
});

Auth::routes();

Route::middleware('auth')->get('/home', function () {
    if (Auth::user()->is_super_admin_account)
        return redirect()->route('sadmin_home');
    else
        return redirect()->route('home');
});

//Route::get('/dashboard', 'DashboardController@index')->name('home');

require __DIR__ . '/profile/profile.php';
require __DIR__ . '/schools/schools.php';
require __DIR__ . '/settings/settings.php';
require __DIR__ . '/plans/plans.php';
require __DIR__ . '/dashboard/dashboard.php';
require __DIR__ . '/school/school.php';
require __DIR__ . '/parents/parents.php';
require __DIR__ . '/drivers/drivers.php';
require __DIR__ . '/drivers/buses.php';
require __DIR__ . '/activation/activation.php';
require __DIR__ . '/productsIOS/productsIOS.php';



