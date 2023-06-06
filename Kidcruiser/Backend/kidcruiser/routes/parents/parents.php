<?php
Route::middleware('auth')->group(function () {
    Route::group(['namespace' => 'Parents', 'middleware' => 'schooladmin'], function() {
        // views
        Route::group(['prefix' => 'parents'], function() {
            Route::view('/', 'parents.index');
            Route::view('/driver={driver}', 'parents.index');
            Route::view('/create', 'parents.create');
            Route::view('/{parent}/edit', 'parents.edit');
            Route::get('/{parent}/map', 'ParentController@showmap');
        });

        // api
        Route::group(['prefix' => 'api/parents'], function() {
            Route::get('/all', 'ParentController@all');

            Route::post('/assign', 'ParentController@assignDrivers');

            Route::post('/upload', 'ParentController@upload');


            Route::get('/getParent/{parent}', 'ParentController@getParent');

            Route::post('/filter', 'ParentController@filter');

            Route::get('/{parent}', 'ParentController@show');

            Route::post('/store', 'ParentController@store');

            Route::get('/child/{parent}', 'ParentController@getChildren');

            Route::put('/update/{parent}', 'ParentController@update');

            Route::delete('/deleteMany', 'ParentController@deleteMany');

            Route::delete('/{parent}', 'ParentController@destroy');
        });
    });
});

