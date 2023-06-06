package com.creativeapps.schoolbusdriver.ui.activity.main.childList.bottomsheet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Child;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivityModel;
import com.creativeapps.schoolbusdriver.ui.activity.main.childList.ChildListFragment;
import com.creativeapps.schoolbusdriver.ui.activity.main.childList.ParentChildrenSection;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class BottomSheetCheckDialog extends BottomSheetDialogFragment implements ParentChildrenSection.ItemClickListener {

    private final Parent parent;
    private MainActivityModel mViewModel;
    private Driver mDriver;

    private SectionedRecyclerViewAdapter mSectionedAdapter;
    private RecyclerView mChildListRecyclerView;

    public BottomSheetCheckDialog(Parent p)
    {
        this.parent = p;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = ((MainActivity) getActivity()).createViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_child_list, container, false);

        mSectionedAdapter = new SectionedRecyclerViewAdapter();

        mChildListRecyclerView = view.findViewById(R.id.child_list_recycler_view);
        mChildListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mChildListRecyclerView.setAdapter(mSectionedAdapter);

        View mSearchView = view.findViewById(R.id.searchChildren);
        mSearchView.setVisibility(View.GONE);

        View checkProgressBar = view.findViewById(R.id.CheckProgressBar);
        checkProgressBar.setVisibility(View.GONE);


        mDriver = Util.getSavedObjectFromPreference(getContext(),
                "mPreference", "Driver", com.creativeapps.schoolbusdriver.data.network.models.Driver.class);

        setChildListUI(mDriver.getParents());
        return view;
    }

    private void setChildListUI(List<Parent> parents) {

        mSectionedAdapter.removeAllSections();

        final Map<String, List<Child>> childrenMap = new LinkedHashMap<>();

        final List<Child> children = this.parent.getChildren();

        if (children.size() > 0) {
            childrenMap.put(this.parent.getName(), children);
        }

        for (final Map.Entry<String, List<Child>> entry : childrenMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                mSectionedAdapter.addSection(new ParentChildrenSection(entry.getKey(), entry.getValue(), this));
            }
        }

        mChildListRecyclerView.setAdapter(mSectionedAdapter);
        mSectionedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position, final int check_in_out) {
        int childPos = mSectionedAdapter.getPositionInSection(position);
        ParentChildrenSection section = (ParentChildrenSection) mSectionedAdapter.getSectionForPosition(position);
        final Child child = section.mChildrenList.get(childPos);

        String title = "", message="";
        if(check_in_out == Util.CHECK_IN_FLAG)
        {
            title = "Check In";
            message = "You are about to check in " + child.getchildName() + ", are you sure?";
        }
        else if(check_in_out == Util.CHECK_OUT_FLAG)
        {
            title = "Check Out";
            message = "You are about to check out " + child.getchildName() + ", are you sure?";
        }
        //display an alert dialog to warn the driver that he is about to check in or out a child.
        new AlertDialog.Builder(this.getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).showHideProgressBar(true);
                        mViewModel.checkInOutChildServer(mDriver.getSecretKey(),
                                child.getId(), check_in_out, getString(R.string.checked_in_string),
                                getString(R.string.checked_out_string));
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.getCheckinStatus().removeObservers(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.getCheckinStatus().observe(this, new BottomSheetCheckDialog.CheckingObserver());
    }

    private class CheckingObserver implements Observer<Integer> {
        @Override
        public void onChanged(Integer CheckingStatus) {
            if (CheckingStatus == null)
                return;

            if (CheckingStatus != 0) {

                new AlertDialog.Builder(BottomSheetCheckDialog.this.getContext())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Success")
                        .setMessage("Child checked " + (CheckingStatus==Util.CHECK_IN_FLAG? "in":"out") + " successfully.")
                        .setNegativeButton("Ok", null)
                        .show();
            }
            else{
                //display an alert dialog to warn the user that there is an error
                new AlertDialog.Builder(BottomSheetCheckDialog.this.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Error")
                        .setMessage(getString(R.string.unexpected_error))
                        .setNegativeButton("Ok", null)
                        .show();

            }
            dismiss();
            mViewModel.setCheckinStatus(null);
        }
    }
}