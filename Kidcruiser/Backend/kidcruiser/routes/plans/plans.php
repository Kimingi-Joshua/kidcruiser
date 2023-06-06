<?php
Route::middleware('auth')->group(function () {
    Route::group(['namespace' => 'Plans', 'middleware' => 'schooladmin'], function() {
        // api
        Route::group(['prefix' => 'api/plans'], function() {
            Route::get('/getPlans', 'PlansController@all');
            Route::post('/createCustom', 'PlansController@createCustom');
            Route::post('/addToCustom', 'PlansController@addToCustom');
        });
    });

    Route::group(['namespace' => 'Plans', 'middleware' => 'superadmin'], function() {
        // views
        Route::group(['prefix' => 'plans'], function() {
            Route::get('/', 'PlansController@showIndex');
            Route::view('/create', 'plans.create');
            Route::view('/{plan}/edit', 'plans.edit');
        });

        Route::group(['prefix' => 'custom-plans'], function() {
            Route::view('/', 'customPlans.index');
        });

        // api
        Route::group(['prefix' => 'api/plans'], function() {
            
            Route::post('/filter', 'PlansController@filter');

            Route::get('/{plan}', 'PlansController@show');

            Route::post('/store', 'PlansController@store');
            Route::put('/update/{plan}', 'PlansController@update');

            Route::delete('/{plan}', 'PlansController@destroy');
        });

        Route::group(['prefix' => 'api/custom-plans'], function() {
            Route::post('/custom-filter', 'PlansController@customFilter');
            Route::post('/set-price', 'PlansController@setCustomPrice');
        });
    });


});
