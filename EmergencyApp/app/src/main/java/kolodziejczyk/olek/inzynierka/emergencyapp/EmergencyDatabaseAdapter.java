package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by sasza_000 on 2016-08-09.
 */
public class EmergencyDatabaseAdapter {

    private static final String DATABASE_NAME="emergency.db";
    private static final int DATABASE_VERSION=1;

    public static final String EMERGENCY_TABLE="emergency";
    public static final String COLUMN_ID="_id";
    public static final String COLUMN_TITLE="title";
    public static final String COLUMN_NUMBER="number";
    public static final String COLUMN_MESSAGE="message";

    private String[] allColumn={COLUMN_ID,COLUMN_TITLE,COLUMN_NUMBER,COLUMN_MESSAGE};

    public static final String DATABASE_CREATE=
            "create table "+EMERGENCY_TABLE+" ( "+COLUMN_ID+" integer primary key autoincrement, "+
                    COLUMN_TITLE + " text not null, "+
                    COLUMN_NUMBER+" text not null, "+
                    COLUMN_MESSAGE+" text not null);";

    private SQLiteDatabase sqlDB;
    private Context context;

    private EmergencyDbHelper emergencyDbHelper;

    public EmergencyDatabaseAdapter(Context ctx){
        context=ctx;
    }
    public EmergencyDatabaseAdapter open() throws android.database.SQLException{
        emergencyDbHelper=new EmergencyDbHelper(context);
        sqlDB=emergencyDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        emergencyDbHelper.close();
    }


    private static class EmergencyDbHelper extends SQLiteOpenHelper{

        public EmergencyDbHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database){
            database.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            Log.w(EmergencyDbHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            database.execSQL("DROP TABLE IF EXISTS " + EMERGENCY_TABLE);
            onCreate(database);
        }


    }
}
