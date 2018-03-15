package com.sunelectronics.sunbluetoothapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.interfaces.IDelete;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;
import com.sunelectronics.sunbluetoothapp.ui.LPRecyclerViewAdapter;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LP_CONTENT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LP_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LP_TABLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PROFILE_ID;

public class LPDataBaseHelper extends SQLiteOpenHelper implements IDelete {

    private static final String TAG = "LPDataBaseHelper";
    private Context mContext;
    private List<LocalProgram> mLocalProgramList;
    private RecyclerView mLpRecyclerView;

    public LPDataBaseHelper(Context context) {
        super(context, Constants.LP_DB_NAME, null, Constants.LP_DB_VER);
        mContext = context;
    }

        /*The onCreate method is not called until the database is first created by a call to getWritable or getReadable database*/

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate called, database created!!");
       /*SQL command:

    CREATE TABLE lp_table (_id INTEGER PRIMARY KEY, lp_name TEXT, lp_content TEXT);

     */
        String CREATE_LOCAL_PROGRAM_TABLE = "CREATE TABLE " + LP_TABLE + "(" +
                PROFILE_ID + " INTEGER PRIMARY KEY," + LP_NAME +
                " TEXT, " + LP_CONTENT + " TEXT);";

        db.execSQL(CREATE_LOCAL_PROGRAM_TABLE);
        // TODO: 8/20/2017 add a sample local program to database when database is first created

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d(TAG, "onUpgrade: called, database upgraded from ver " + oldVersion + " to ver " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS" + LP_TABLE);
        onCreate(db);
    }

    //CRUD operations; Create, Read, Update, Delete methods;

    public void addLocalProgramToDB(LocalProgram lp) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LP_NAME, lp.getName());
        contentValues.put(LP_CONTENT, lp.getContent());
        long row = db.insert(LP_TABLE, null, contentValues);

        if (row > 0) {
            Log.d(TAG, "addLocalProgramToDB: called, local program saved to db: " + "name: " + lp.getName() + " content: " + lp.getContent());
            Toast.makeText(mContext, "Local Program added to database", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Could not add local program to database", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * returns a list of local programs stored in local_programs.db database
     *
     * @return List
     */
    public List<LocalProgram> getLocalPrograms() {

        mLocalProgramList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(LP_TABLE, new String[]{PROFILE_ID, LP_NAME, LP_CONTENT}, null, null, null, null,
                LP_NAME + " COLLATE NOCASE;");

        if (cursor.moveToFirst()) {

            do {

                LocalProgram lp = new LocalProgram();
                lp.setId(cursor.getInt(cursor.getColumnIndex(PROFILE_ID)));
                lp.setName(cursor.getString(cursor.getColumnIndex(LP_NAME)));
                lp.setContent(cursor.getString(cursor.getColumnIndex(LP_CONTENT)));
                mLocalProgramList.add(lp);

            } while (cursor.moveToNext());
            cursor.close();

        } else {

            Log.d(TAG, "getLocalPrograms: no items in db");

            Toast.makeText(mContext, "No local programs in database!!!", Toast.LENGTH_SHORT).show();
        }

        return mLocalProgramList;
    }

    public void upDateExistingLP(LocalProgram localProgram) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LP_NAME, localProgram.getName());
        contentValues.put(LP_CONTENT, localProgram.getContent());
        int rowsAffected = db.update(LP_TABLE, contentValues, PROFILE_ID + "=?", new String[]{String.valueOf(localProgram.getId())});
        if (rowsAffected > 0) {
            Toast.makeText(mContext, "Local Program Updated!", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(mContext, "Unable to update Local Program", Toast.LENGTH_LONG).show();
        }
    }

    public void setRecyclerView(RecyclerView lpRecyclerView) {
        mLpRecyclerView = lpRecyclerView;
    }

    @Override
    public void deleteProfile(int id) {
        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(LP_TABLE, PROFILE_ID + "=?", new String[]{String.valueOf(id)});

        if (rowsDeleted > 0) {

            Toast.makeText(mContext, "Local program deleted", Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(mContext, "Could not delete local program", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void deleteAllProfiles() {
        if (mLocalProgramList.size() > 0) {

            SQLiteDatabase db = getWritableDatabase();
            int rowsDeleted = db.delete(LP_TABLE, null, null);
            if (rowsDeleted > 0) {

                String localProgramText = (rowsDeleted == 1) ? " Local Program deleted" : " Local Programs deleted";
                mLocalProgramList = getLocalPrograms();//refresh list

                ((LPRecyclerViewAdapter) mLpRecyclerView.getAdapter()).setLocalProgramList(mLocalProgramList);
                mLpRecyclerView.getAdapter().notifyDataSetChanged();

                Toast.makeText(mContext, rowsDeleted + localProgramText, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(mContext, "No Local Programs deleted", Toast.LENGTH_LONG).show();
            }
        }

    }
}
