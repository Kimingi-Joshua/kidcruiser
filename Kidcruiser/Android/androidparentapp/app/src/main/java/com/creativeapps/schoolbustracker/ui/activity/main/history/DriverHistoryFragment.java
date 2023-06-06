package com.creativeapps.schoolbustracker.ui.activity.main.history;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creativeapps.schoolbustracker.R;
import com.creativeapps.schoolbustracker.data.Util;
import com.creativeapps.schoolbustracker.data.network.models.EventLog;
import com.creativeapps.schoolbustracker.data.network.models.Parent;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivity;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivityModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



public class DriverHistoryFragment extends Fragment {
    //adapter used with recyclerView used to display name and telephone numbers of parents
    private EventsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List<EventLog> mDriverLogList;
    private boolean mIsLoading = false;
    private Parent mParent;
    private MainActivityModel mViewModel;
    private Integer mPage = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate the layout
        View view = inflater.inflate(R.layout.fragment_driver_log, container, false);
        //get the last saved parent information from the SharedPreference
        mParent = Util.getSavedObjectFromPreference(this.getActivity().getApplicationContext(),
                "mPreference", "Parent", Parent.class);

        //instantiate the view model object
        mViewModel = ((MainActivity) getActivity()).createViewModel();
        
        return view;
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here

        //define the recyclerView that used to display the names and phone numbers of parents
        mRecyclerView = view.findViewById(R.id.recycler_view);

        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());

        //define a parent list
        mDriverLogList = new ArrayList<>();

        //get the adapter
        mAdapter = new EventsAdapter(mDriverLogList);

        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        initScrollListener();

        //call the api to get driver log
        mViewModel.getDriverLog(mPage,mParent.getSecretKey());

        //observe changes for the response of setting the parent's home location
        mViewModel.getGetDriverLogIsRunning().observe(this, new DriverHistoryFragment.getDriverLogIsRunning());

    }

    private void initScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!mIsLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == mDriverLogList.size() - 1) {
                        //bottom of list!
                        //((MainActivity) getActivity()).showHideProgressBar(true);
                        mViewModel.getDriverLog(++mPage,mParent.getSecretKey());
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


            }
        });
    }


    /*observe changes for the response of setting the home location of the parent*/
    private class getDriverLogIsRunning implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean getDriverLogIsRunning) {
            if (getDriverLogIsRunning == null) return;

            if (getDriverLogIsRunning) {
                //if setting the location is running, display the progress spinner
                ((MainActivity)getActivity()).showHideProgressBar(true);
            }
            else
            {
                //otherwise, stop the spinner
                ((MainActivity)getActivity()).showHideProgressBar(false);
                //check if the server returns OK status
                if(mViewModel.getDriverLogs().getValue()!=null)
                {
                    List<EventLog> driverLog = mViewModel.getDriverLogs().getValue().getEventLog();

                    //mDriverLogList.clear();
                    mDriverLogList.addAll(driverLog);

                    mAdapter.updateData(mDriverLogList);
                }
                mViewModel.setGetDriverLogIsRunning(null);
                mIsLoading = false;
            }
        }
    }
}
