package com.example.projectpant.model;

import java.util.ArrayList;
import java.util.List;

// Singleton list for all ads
public class PantAdList {

    private static List<PantAd> PantAdList;

    private PantAdList(){}

    // Retrieves the list
    public static List<PantAd> getinstance(){
        if (PantAdList == null)
            PantAdList = new ArrayList<>();
        return PantAdList;
    }

    // Gets specific ad in list based on id field
    public static PantAd getAdById(String Id){
        for (PantAd ad : PantAdList){
            if (ad.getId().equals(Id))
                return ad;
        }
        return null;
    }
}
