package com.sunelectronics.sunbluetoothapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.interfaces.IDelete;
import com.sunelectronics.sunbluetoothapp.models.Tc01Profile;
import com.sunelectronics.sunbluetoothapp.ui.ProfileRecyclerViewAdapter;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PROFILE_ID;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_DB_VER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_PROF_DB_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_PROF_TABLE;

public class Tc01ProfDataBaseHelper extends SQLiteOpenHelper implements IDelete {
    private static final String TAG = "Tc01ProfDataBaseHelper";
    private Context mContext;
    private RecyclerView mProfileRecyclerView;

    public Tc01ProfDataBaseHelper(Context context) {
        super(context, TC01_PROF_DB_NAME, null, TC01_DB_VER);
        mContext = context;
    }

    /*SQL command:
        to create a table called PROFILE with columns: _id, NAME, A0,B0......A9,B9 this is command:

        CREATE TABLE PROFILE (_id INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, A0 TEXT, B0 TEXT, A1 TEXT, B1 TEXT,
        A2 TEXT, B2 TEXT, A3 TEXT, B3 TEXT, A4 TEXT, B4 TEXT, A5 TEXT, B5 TEXT, A6 TEXT, B6 TEXT, A7 TEXT,
        B7 TEXT, A8 TEXT, B8 TEXT, A9 TEXT, B9 TEXT, CYCLES TEXT);
         */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_SQL_COMMAND = "CREATE TABLE " + TC01_PROF_TABLE + " (_id INTEGER PRIMARY KEY" +
                " AUTOINCREMENT, NAME TEXT, A0 TEXT, B0 TEXT, A1 TEXT, B1 TEXT, A2 TEXT, B2 TEXT, A3 TEXT, " +
                "B3 TEXT, A4 TEXT, B4 TEXT, A5 TEXT, B5 TEXT, A6 TEXT, B6 TEXT, A7 TEXT, B7 TEXT, A8 TEXT, " +
                "B8 TEXT, A9 TEXT, B9 TEXT, CYCLES TEXT)";
        db.execSQL(CREATE_TABLE_SQL_COMMAND);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: called, database upgraded from ver " + oldVersion + " to ver " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS" + Constants.TC01_PROF_TABLE);
        onCreate(db);
    }

    public void addProfileToDataBase(Tc01Profile profile) {

        SQLiteDatabase db = getWritableDatabase();
        Map<String, String> scanTempTimeMap;
        scanTempTimeMap = profile.getScanTempTimes();
        char scanTempChar = 'A';
        char scanTimeChar = 'B';

        ContentValues profileValues = new ContentValues();
        profileValues.put("NAME", profile.getName());

        for (int i = 0; i < 10; i++) {

            profileValues.put(String.valueOf(scanTempChar) + i, scanTempTimeMap.get(String.valueOf(scanTempChar) + i));
            profileValues.put(String.valueOf(scanTimeChar) + i, scanTempTimeMap.get(String.valueOf(scanTimeChar) + i));
        }
        profileValues.put("CYCLES", profile.getCycles());
        try {
            db.insert(TC01_PROF_TABLE, null, profileValues);
            Toast.makeText(mContext, "Profile successfully added to database!", Toast.LENGTH_SHORT).show();

        } catch (SQLiteException e) {

            Toast.makeText(mContext, "SQlite Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteProfile(int id) {

        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(Constants.TC01_PROF_TABLE, PROFILE_ID + "=?", new String[]{String.valueOf(id)});
        if (rowsDeleted > 0) {
            Toast.makeText(mContext, "Profile deleted", Toast.LENGTH_SHORT).show();
            ProfileRecyclerViewAdapter adapter = (ProfileRecyclerViewAdapter) mProfileRecyclerView.getAdapter();
            adapter.setProfileList(getProfiles());
            adapter.notifyDataSetChanged();

        } else {
            Toast.makeText(mContext, "Could not delete profile", Toast.LENGTH_SHORT).show();

        }
    }

    public void deleteAllProfiles() {

        List<Tc01Profile> profileList;
        profileList = getProfiles();

        if (profileList.size() > 0) {

            SQLiteDatabase db = getWritableDatabase();
            int rowsDeleted = db.delete(Constants.TC01_PROF_TABLE, null, null);
            if (rowsDeleted > 0) {

                String profileDeleted = (rowsDeleted == 1) ? " Profile deleted" : " Profiles deleted";
                profileList.clear();
                profileList = getProfiles();
                ((ProfileRecyclerViewAdapter) mProfileRecyclerView.getAdapter()).setProfileList(profileList);
                mProfileRecyclerView.getAdapter().notifyDataSetChanged();
                Toast.makeText(mContext, rowsDeleted + profileDeleted, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(mContext, "No Profiles were deleted", Toast.LENGTH_LONG).show();
            }
        }
    }

    public List<Tc01Profile> getProfiles() {

        List<Tc01Profile> profileList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TC01_PROF_TABLE, new String[]{"_id", "NAME", "A0", "B0", "A1", "B1",
                        "A2", "B2", "A3", "B3", "A4", "B4", "A5", "B5", "A6", "B6", "A7", "B7", "A8", "B8", "A9", "B9", "CYCLES"},
                null, null, null, null, "NAME COLLATE NOCASE;");
        if (cursor.moveToFirst()) {

            do {
                Tc01Profile profile = new Tc01Profile(cursor.getString(cursor.getColumnIndex("NAME")));
                profile.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                profile.setCycles(cursor.getString(cursor.getColumnIndex("CYCLES")));


                for (int i = 0; i < 10; i++) {

                    profile.addSegment(i, cursor.getString(cursor.getColumnIndex("A" + i)),
                            cursor.getString(cursor.getColumnIndex("B" + i)));
                }
                profileList.add(profile);
                Log.d(TAG, "getProfiles: added profile " + profile);
            } while (cursor.moveToNext());
            cursor.close();

        } else {
            Log.d(TAG, "getProfiles: no items in db");
            Toast.makeText(mContext, "No Profiles in database!!!", Toast.LENGTH_SHORT).show();
        }
        return profileList;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        mProfileRecyclerView = recyclerView;
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TC01_PROF_TABLE, new String[]{"_id"},
                null, null, null, null, null);

        int count = cursor.getCount();
        cursor.close();
        return count;


    }
}
