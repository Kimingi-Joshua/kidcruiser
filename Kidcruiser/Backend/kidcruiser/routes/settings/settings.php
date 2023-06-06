<?php
Route::middleware('auth')->group(function () {
    Route::group(['namespace' => 'Settings', 'middleware' => 'superadmin'], function() {
        
        Route::view('/settings', 'settings.settings');
        Route::view('/terms', 'settings.terms');
        Route::view('/privacy-policy', 'settings.privacyPolicy');
        
        // api
        Route::group(['prefix' => 'api/settings'], function() {
            Route::get('/getSettings', 'SettingsController@getSettings');
            Route::put('/updateSettings', 'SettingsController@updateSettings');

            Route::get('/getPrivacyPolicy', 'SettingsController@getPrivacyPolicy');
            Route::put('/updatePrivacyPolicy', 'SettingsController@updatePrivacyPolicy');

            Route::get('/getTerms', 'SettingsController@getTerms');
            Route::put('/updateTerms', 'SettingsController@updateTerms');
        });
    });
});
