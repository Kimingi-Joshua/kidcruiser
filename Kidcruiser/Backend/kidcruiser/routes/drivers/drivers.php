<?php
Route::middleware('auth')->group(function () {
    Route::group(['namespace' => 'Drivers', 'middleware' => 'schooladmin'], function() {
        // views
        Route::group(['prefix' => 'drivers'], function() {
            Route::view('/', 'drivers.index');
            Route::view('/create', 'drivers.create');
            Route::view('/{driver}/edit', 'drivers.edit');
            Route::get('/{driver}/map', 'DriverController@showmap');
            Route::get('/map', 'DriverController@showAllMap');
            Route::view('/{driver}/history', 'drivers.history');
        });

        // api
        Route::group(['prefix' => 'api/drivers'], function() {
            Route::get('/all', 'DriverController@all');
            
            Route::get('/getDriver/{driver}', 'DriverController@getDriver');
            Route::get('/getLog/{driver}', 'DriverController@getLog');

            Route::post('/filter', 'DriverController@filter');
            Route::post('/sendMessage', 'DriverController@sendMessage');

            Route::get('/{driver}', 'DriverController@show');

            Route::post('/store', 'DriverController@store')->middleware('can:create,App\Driver');
            
            Route::put('/update/{driver}', 'DriverController@update');

            Route::delete('/{driver}', 'DriverController@destroy');
        });
    });
});



