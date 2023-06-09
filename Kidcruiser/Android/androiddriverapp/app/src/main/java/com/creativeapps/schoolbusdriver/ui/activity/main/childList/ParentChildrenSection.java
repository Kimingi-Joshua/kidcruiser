package com.creativeapps.schoolbusdriver.ui.activity.main.childList;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Child;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;


public final class ParentChildrenSection extends Section  {

    private final String title;
    public final List<Child> mChildrenList;
    private final ItemClickListener mItemClickListner;

    public ParentChildrenSection(@NonNull final String title, @NonNull final List<Child> childrenList,
                                 ItemClickListener itemClickListner) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.fragment_child_list_item)
                .headerResourceId(R.layout.fragment_child_list_header)
                .build());

        this.title = title;
        this.mItemClickListner = itemClickListner;
        this.mChildrenList = new ArrayList<>();
        this.mChildrenList.addAll(childrenList);
    }

    @Override
    public int getContentItemsTotal() {
        return mChildrenList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(final View view) {
        return new ChildItemViewHolder(view, mItemClickListner);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ChildItemViewHolder itemHolder = (ChildItemViewHolder) holder;

        final Child child = mChildrenList.get(position);

        itemHolder.mChildName.setText(child.getchildName());
        //itemHolder.imgItem.setImageResource(contact.profileImage);


        itemHolder.mAbsent = Util.getAbsentStatus(child);
        itemHolder.mLastCheckStatus = Util.getLastCheckStatus(child);

        if (itemHolder.mAbsent != null){
            if (itemHolder.mAbsent != 0) {
                itemHolder.mView.findViewById(R.id.check_in).setEnabled(false);
                itemHolder.mView.findViewById(R.id.check_out).setEnabled(false);
                itemHolder.mView.setBackgroundColor(Color.parseColor("#dddddd"));
                itemHolder.mChildAbsence.setText("Absent");
            }
        }

        if (itemHolder.mLastCheckStatus != null){
            boolean disableCheckIn = itemHolder.mLastCheckStatus == Util.CHECK_IN_FLAG;
            itemHolder.mView.findViewById(R.id.check_in).setEnabled(!disableCheckIn);
            itemHolder.mView.findViewById(R.id.check_out).setEnabled(disableCheckIn);
            if(disableCheckIn)
            {
                ImageViewCompat.setImageTintList(
                        (ImageView)itemHolder.mView.findViewById(R.id.check_in),
                        ColorStateList.valueOf(Color.parseColor("#dddddd")));
            }
            else
            {
                ImageViewCompat.setImageTintList(
                        (ImageView)itemHolder.mView.findViewById(R.id.check_out),
                        ColorStateList.valueOf(Color.parseColor("#dddddd")));
            }
        }

    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.mParentNameTextView.setText(title);
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position, int check_in_out /*3: check_in, 4: check_out*/);
    }
}
