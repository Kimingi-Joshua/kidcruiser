package com.creativeapps.schoolbustracker.ui.activity.login;

import android.content.Context;
import android.os.Bundle;

import com.creativeapps.schoolbustracker.R;
import com.creativeapps.schoolbustracker.data.Util;
import com.creativeapps.schoolbustracker.data.network.models.Parent;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.google.firebase.FirebaseApp;

public class LoginActivity extends AppCompatActivity {

    //view model for the activity
    private LoginModel mViewModel;

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate the layout
        setContentView(R.layout.activity_login);
        mContext = getApplicationContext();

        FirebaseApp.initializeApp(this);

        //create an instance of the view model
        mViewModel = createViewModel();
    }

    public static Context getContext() {
        return mContext;
    }

    /*create and return an instance of the view model*/
    public LoginModel createViewModel() {
        return ViewModelProviders.of(this).get(LoginModel.class);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //start observing the parent live data from the view model
        mViewModel.getParent().observe(this, new LoginActivity.parentObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //stop observing live data when the fragment is paused
        mViewModel.getParent().removeObservers(this);
    }

    /*Observer for the parent live data that contains the data of the parent. This data is obtained
    after the process (verify authentication code of the drive) is finished*/
    public class parentObserver implements Observer<Parent> {
        @Override
        public void onChanged(@Nullable Parent parent) {
            if (parent == null) return;
            //if the process (verify authentication code of the parent) is returned the data of
            // the parent, i.e., the parent authenticated correctly
            if (parent!=null) {
                //save the data of the parent to the SharedPreference
                Util.saveObjectToSharedPreference(getApplicationContext(), "mPreference",
                        "Parent", parent);
                //finish the login activity
                finishAffinity();
                //redirect the main screen
                Util.redirectToActivity(LoginActivity.this, MainActivity.class);
            }
        }
    }
}
