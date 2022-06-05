package com.connid.sta.connector;

/**
 * Created by gpalos on 18. 8. 2016.
 */
public class staFilter {
    public String byName;
    public String byUid;
    //public String byEmailAddress;

    @Override
    public String toString() {
        return "staFilter{" +
                "byName='" + byName + '\'' +
                ", byUid='" + byUid + '\'' +
                //", byEmailAddress='" + byEmailAddress + '\'' +
                '}';
    }
}
