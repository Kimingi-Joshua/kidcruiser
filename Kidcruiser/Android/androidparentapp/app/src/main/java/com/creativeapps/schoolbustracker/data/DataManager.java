package com.creativeapps.schoolbustracker.data;

import com.creativeapps.schoolbustracker.data.network.services.ParentApiService;


public class DataManager {

    private static DataManager sInstance;

    /*get the data manager instance*/
    public static synchronized DataManager getInstance() {
        if (sInstance == null) {
            sInstance = new DataManager();
        }
        return sInstance;
    }

    public ParentApiService getParentApiService() {
        return ParentApiService.getInstance();
    }

}
