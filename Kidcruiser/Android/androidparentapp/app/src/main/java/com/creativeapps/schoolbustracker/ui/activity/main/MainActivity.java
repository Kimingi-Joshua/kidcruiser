package com.creativeapps.schoolbustracker.ui.activity.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeapps.schoolbustracker.R;
import com.creativeapps.schoolbustracker.data.Util;
import com.creativeapps.schoolbustracker.data.network.models.Parent;
import com.creativeapps.schoolbustracker.data.network.models.Payload;
import com.creativeapps.schoolbustracker.ui.activity.login.LoginActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;

import net.mrbin99.laravelechoandroid.Echo;
import net.mrbin99.laravelechoandroid.EchoCallback;
import net.mrbin99.laravelechoandroid.EchoOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    final String TAG = "MainActivity";

    //view model of the main activity
    private MainActivityModel mViewModel;
    //parent object that holds parent information
    private Parent mParent;
    //Laravel echo (web socket) for listening on the driver location changes
    private Echo echo;
    //overlay that prevent the user from interacting with any gui element on the screen while the
    // spinner is shown
    private Dialog mOverlayDialog;
    private ProgressBar mProgressBar;
    //navigation and drawer layout
    private AppBarConfiguration mAppBarConfiguration;
    public NavController navController;
    public NavigationView navigationView;
    private DrawerLayout mDrawer;

    private AdView mAdView;

    private static Context mContext;
    private Menu mOptionsMenu;

    public static Integer MAP_TYPE = 0; //0 for GoogleMaps and 1 for MabBox
    private Bundle mBundle;
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.map_bar_buttons, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh) {
            this.showHideProgressBar(true);
            mViewModel.getParentServer(mParent.getCountry_code(), mParent.getTel_number(), mParent.getSecretKey());
            mViewModel.getParent().observe(this, new ParentObserver());
        }
        return super.onOptionsItemSelected(item);
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        MAP_TYPE = Util.getSavedObjectFromPreference(getApplicationContext(),
                "mPreference", "MAP_TYPE", Integer.class);
        if(MAP_TYPE == null)
        {
            MAP_TYPE = 0;
        }
        //inflate the layout
        setContentView(R.layout.activity_main);
        //setup the navigation with drawer
        setupNavigation(savedInstanceState);
        //instantiate the view model
        mViewModel = createViewModel();

        mContext = getApplicationContext();

        mProgressBar = findViewById(R.id.MapProgressBar);
        mOverlayDialog = new Dialog(this, android.R.style.Theme_Panel);

        mAdView = findViewById(R.id.adView);

        mAdView.setVisibility(View.GONE);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //get the last saved parent information from the SharedPreference
        mParent = Util.getSavedObjectFromPreference(getApplicationContext(),
                "mPreference", "Parent", Parent.class);

        //check if there are already saved data for the parent
        if(mParent ==null)
            //otherwise, go to login again
            Util.redirectToActivity(this, LoginActivity.class);

        //check if the parent is not assigned to a driver yet
        if(mParent.getDriver()==null)
            Util.displayExitMessage(getString(R.string.no_driver_alert), getString(R.string.parent_not_assigned_to_bus_driver_alert), this,false);
        else
        {
            if(mParent.getDriver().getVerified()!=1)
                Util.displayExitMessage(getString(R.string.no_tracking_alert), getString(R.string.driver_not_use_app_yet), this,false);
        }
        navigationView.getMenu().findItem(R.id.nav_wallet).setVisible(mParent.getSchool().getIsPayAsYouGo() == 1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Setting Up One Time Navigation
    private void setupNavigation(Bundle bundle) {

        mDrawer = findViewById(R.id.drawer_layout);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_mabbox_home, R.id.nav_googlemaps_home,
                R.id.nav_callDriver, R.id.nav_childLog, R.id.nav_history,
                R.id.nav_settings,R.id.nav_about,
                R.id.nav_logout)
                .setDrawerLayout(mDrawer)
                .build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //setup the navigation controller
        navigationView = findViewById(R.id.navigationView);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        setHomeMap();
    }

    private void setHomeMap() {
        if(MAP_TYPE == 1)
        {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_mabbox_home, true)
                    .build();
            navController.navigate(R.id.nav_mabbox_home, mBundle, navOptions);
        }
        else
        {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_googlemaps_home, true)
                    .build();
            navController.navigate(R.id.nav_googlemaps_home, mBundle, navOptions);
        }
    }

    @Override
    public void onBackPressed() {
        //close drawer when back button pressed
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //close drawer when any item selected
        mDrawer.closeDrawers();
        //if the item already selected, do nothing
        if(menuItem.isChecked())
            return false;

        //check which menu item is selcted
        switch(menuItem.getItemId()) {
            //show map
            case R.id.nav_home:
                menuItem.setChecked(true);
                if(MAP_TYPE == 1) {
                    navController.navigate(R.id.nav_mabbox_home);
                }
                else
                {
                    navController.navigate(R.id.nav_googlemaps_home);
                }
                break;
            //call the driver
            case R.id.nav_callDriver:
                CallDriver();
                //do not check the menu item and do not add it to the navigation stack
                return false;
            //show preference screen (settings)
            case R.id.nav_settings:
                menuItem.setChecked(true);
                navController.navigate(R.id.nav_settings);
                break;
            //show preference screen (settings)
            case R.id.nav_history:
                menuItem.setChecked(true);
                navController.navigate(R.id.nav_history);
                break;
            //show child check screen
            case R.id.nav_childLog:
                menuItem.setChecked(true);
                navController.navigate(R.id.nav_childLog);
                break;
            //share the app
            case R.id.nav_shareApp:
                shareApp();
                //do not check the menu item and do not add it to the navigation stack
                return false;
            //show about
            case R.id.nav_about:
                menuItem.setChecked(true);
                navController.navigate(R.id.nav_about);
                break;
            case R.id.nav_wallet:
                showWalletPopup();
                return false;
            //logout
            case R.id.nav_logout:
                //display an alert dialog to warn the user that he is about to make phone call. If the user
                // chooses yes, a call will be placed
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                //do not check the menu item and do not add it to the navigation stack
                return false;
        }
        return true;
    }

    private void showWalletPopup() {
        View customLayout = LayoutInflater.from(MainActivity.this).inflate(R.layout.wallet_popup, null);
        //Set wallet balance from parent
        TextView walletBalance = customLayout.findViewById(R.id.balance);
        walletBalance.setText("Balance: " + String.valueOf(mParent.getWallet()));
        //Set next renews at from parent
        if(mParent.getNextRenewsAt() != null) {
            TextView nextRenewAt = customLayout.findViewById(R.id.renewes_on);
            nextRenewAt.setText("Renews at: " + mParent.getNextRenewsAt());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setView(customLayout)
                .setPositiveButton("Pay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Util.WEB_SERVER_URL+"/recharge/"+mParent.getSecretKey()));
                        startActivity(browserIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(500, 200); //Controlling width and height.
        dialog.show();
    }

    private void shareApp() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getString(R.string.share_body);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
    private void CallDriver() {
        if(mParent.getDriver()==null)
        {
            Util.displayExitMessage(getString(R.string.alert), getString(R.string.not_assigned_to_driver_yet), this, false);
        }
        else
        {
            //check the permission for making phone calls
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED) {
                //if not, request a phone call permission
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, Util.PERMISSIONS_REQUEST_CALL);
            } else {
                //display an alert dialog to warn the user that he is about to make phone call. If the user
                // chooses yes, a call will be placed
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.call_driver))
                        .setMessage(getString(R.string.are_you_sure_call_driver))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //define the call intent
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                //set the telephone number
                                callIntent.setData(Uri.parse("tel:" + mParent.getDriver().getTel_number()));
                                MainActivity.this.startActivity(callIntent);

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        //The result of the permission request is handled here
        //check the type of the request
        switch (requestCode) {
            //make a call permission request
            case Util.PERMISSIONS_REQUEST_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CallDriver();
                } else {
                    //if not granted, display a message
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    /*function used to show/hide spinner and the overlay dialog*/
    public void showHideProgressBar(Boolean show)
    {
        if(show)
        {
            mOverlayDialog.show();
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            mOverlayDialog.dismiss();
            mProgressBar.setVisibility(View.GONE);
        }
    }


    /*return a view model for the activity*/
    public MainActivityModel createViewModel() {
        return ViewModelProviders.of(this).get(MainActivityModel.class);
    }

    private class ShowAdsObserver implements Observer<Integer> {

        @Override
        public void onChanged(Integer showAds) {
            if (showAds == null)
                return;

            if (showAds==1) {
                mAdView.setVisibility(View.VISIBLE);
            }
            else
                mAdView.setVisibility(View.GONE);
        }
    }
    private class UseMabBoxObserver implements Observer<Integer> {

        @Override
        public void onChanged(Integer useMabBox) {
            if (useMabBox == null)
                return;

            MAP_TYPE = useMabBox;
            Util.saveObjectToSharedPreference(getApplicationContext(),
                    "mPreference", "MAP_TYPE", MAP_TYPE);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //start listening for location change of the parent
        startDriverLocationUpdates();

        mViewModel.getShowAds().observe(this, new ShowAdsObserver());
        mViewModel.getUseMabBox().observe(this, new UseMabBoxObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //stop listening for location change of the parent
        stopLocationUpdates();
    }

    /*start listening for location change of the driver. Note that, this function is executed in
    the main activity so that the app track the driver location regardless the fragment displayed*/
    private void startDriverLocationUpdates()
    {
        if(mParent==null || mParent.getDriver()==null)
            return;
        //get the channel name of the driver associated with the parent
        final String channel_name = mParent.getDriver().getChannel();
        // Setup options
        EchoOptions options = new EchoOptions();

        // Setup host of your Laravel Echo Server
        options.host = Util.WEB_SOCKET_SERVER_URL;

        // Create the client
        echo = new Echo(options);

        echo.connect(new EchoCallback() {
            @Override
            public void call(Object... args) {
                // Success connect
                Log.d("Echo", "call: Success connect");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //update the UI to show "online" status
                        mViewModel.setConnectivityStatus(true);
                    }
                });
                listenToChannel(channel_name);
            }
        }, new EchoCallback() {
            @Override
            public void call(Object... args) {
                // Error connect
                Log.d("Echo", "call: Error connect");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //update the UI to show "offline" status
                        mViewModel.setConnectivityStatus(false);
                    }
                });
            }
        });
    }

    private void listenToChannel(final String channel_name)
    {
        echo.channel(channel_name)
                .listen("LocationChangeEvent", new EchoCallback() {
                    @Override
                    public void call(Object... args) {
                        // Event thrown.
                        try {
                            String new_loc = ((JSONObject) args[1]).get("data").toString();
                            Gson gson = new Gson();
                            Payload ll = gson.fromJson(new_loc, Payload.class);
                            ll.polylineValues = new ArrayList<Double>();
                            JSONArray jArray = new JSONArray(ll.polyline);
                            if (jArray != null) {
                                for (int i=0;i<jArray.length();i++){
                                    ll.polylineValues.add(jArray.getDouble(i));
                                }
                            }
                            //ll.polylineValues = ll.polyline;
                            mViewModel.setDriverRealTimeData(ll);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /*stop listening for location change*/
    public void stopLocationUpdates()
    {
        if(echo!=null)
            echo.disconnect();
    }

    public void logout() {
        Util.saveObjectToSharedPreference(getApplicationContext(),
                "mPreference", "Parent", null);
        finishAffinity();
        Util.redirectToActivity(this, LoginActivity.class);
    }
    //Observer for refresh parent
    private class ParentObserver implements Observer<Parent> {

        @Override
        public void onChanged(@Nullable Parent parent) {
            showHideProgressBar(false);
            if (parent == null) {

                //get the last saved parent information from the SharedPreference
                mParent = Util.getSavedObjectFromPreference(getContext(),
                        "mPreference", "Parent", Parent.class);
            } else {
                mParent = parent;
                Log.d(TAG, "ParentObserver => onChanged: " + parent.getName());
                Util.saveObjectToSharedPreference(getContext(),
                        "mPreference", "Parent", parent);
            }
            navigationView.getMenu().findItem(R.id.nav_wallet).setVisible(mParent.getSchool().getIsPayAsYouGo() == 1);
            //if the parent is not verified, go to the login activity
            if (mParent.getVerified() != 1) {
                //logout
                logout();
            }
        }

    }
}