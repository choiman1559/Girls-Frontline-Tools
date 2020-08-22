package com.fqxd.gftools.features.alarm.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GFAlarmObjectClass {
    private int Hour;
    private int Minute;
    private Sector sector;
    private int SquadNumber;
    private long timeToTrigger;
    private String Package;
    private long ID;

    public void setHour(int Hour) { this.Hour = Hour; }
    public void setMinute(int Minute) { this.Minute = Minute; }
    public void setSector(Sector sector) { this.sector = sector; }
    public void setSquadNumber(int SquadNumber) { this.SquadNumber = SquadNumber; }
    public void setTimeToTrigger(long timeToTrigger) { this.timeToTrigger = timeToTrigger; }
    public void setPackage(String Package) { this.Package = Package; }
    private void setID(long ID) { this.ID = ID; }

    public int getHour() { return Hour; }
    public int getMinute() { return Minute; }
    public Sector getSector() { return sector; }
    public int getSquadNumber() { return SquadNumber; }
    public long getTimeToTrigger() { return timeToTrigger; }
    public String getPackage() { return Package; }
    public long getID() { return ID; }

    public JSONObject parse() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("Hour",Hour);
        json.put("Minute",Minute);
        json.put("Sector",sector.getJsonObj());
        json.put("SquadNumber",SquadNumber);
        json.put("timeToTrigger",timeToTrigger);
        json.put("Package",Package);
        json.put("ID",timeToTrigger * SquadNumber);
        return json;
    }

    public void setHourAndMinuteFromSector() {
        List<Integer> list = sector.toInteger(sector.GetLSDTable());
        this.Hour = list.get(0);
        this.Minute = list.get(1);
    }

    public void setTimeToTriggerFromHourAndMinute() {
        Calendar date = Calendar.getInstance();
        date.setTime(new Date(System.currentTimeMillis()));
        date.add(Calendar.HOUR_OF_DAY,Hour);
        date.add(Calendar.MINUTE,Minute);
        timeToTrigger = date.getTimeInMillis();
    }

    public void setTimeToTriggerAndHourAndMinuteFromSector() {
        setHourAndMinuteFromSector();
        setTimeToTriggerFromHourAndMinute();
    }

    public static GFAlarmObjectClass getGFAlarmObjectClassFromJson(JSONObject json) throws JSONException {
        GFAlarmObjectClass obj = new GFAlarmObjectClass();
        obj.setHour(json.getInt("Hour"));
        obj.setMinute(json.getInt("Minute"));
        obj.setSector(Sector.getSectorFromJson(json.getJSONObject("Sector")));
        obj.setSquadNumber(json.getInt("SquadNumber"));
        obj.setTimeToTrigger(json.getLong("timeToTrigger"));
        obj.setPackage(json.getString("Package"));
        obj.setID(json.getLong("ID"));
        return obj;
    }
}
