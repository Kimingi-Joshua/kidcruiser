<?php
Route::middleware('auth')->group(function () {
    Route::group(['namespace' => 'ProductsIOS', 'middleware' => 'schooladmin'], function() {
        // api
        Route::group(['prefix' => 'api/ios-products'], function() {
            Route::get('/getProducts', 'ProductsIOSController@all');
        });
    });

    Route::group(['namespace' => 'ProductsIOS', 'middleware' => 'superadmin'], function() {
        // views
        Route::group(['prefix' => 'ios-products'], function() {
            Route::get('/', 'ProductsIOSController@showIndex');
            Route::view('/create', 'productsIOS.create');
            Route::view('/{product}/edit', 'productsIOS.edit');
        });

        // api
        Route::group(['prefix' => 'api/ios-products'], function() {
            
            Route::post('/filter', 'ProductsIOSController@filter');

            Route::get('/{product}', 'ProductsIOSController@show');

            Route::post('/store', 'ProductsIOSController@store');
            Route::put('/update/{product}', 'ProductsIOSController@update');

            Route::delete('/{product}', 'ProductsIOSController@destroy');
        });
    });


});
