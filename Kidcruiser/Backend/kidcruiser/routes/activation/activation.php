<?php
Route::middleware('auth')->group(function () {
    Route::group(['namespace' => 'Activation', 'middleware' => 'superadmin'], function() {
        
        Route::view('/activation', 'activation.activation');    
        // api
        Route::group(['prefix' => 'api/activation'], function() {
            Route::get('/', 'ActivationController@load');
            Route::post('/activate', 'ActivationController@activate');
        });
    });
});
