package sg.edu.np.mad.greencycle.Analytics;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {HourlyData.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HourlyDataDao hourlyDataDao();

    // Migration from version 1 to 2: Add tankId column
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE HourlyData ADD COLUMN tankId TEXT");
        }
    };

    // Migration from version 2 to 3: Add default values to columns
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE new_HourlyData (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp TEXT, tankId TEXT, ec REAL NOT NULL DEFAULT 0, moisture REAL NOT NULL DEFAULT 0, nitrogen REAL NOT NULL DEFAULT 0, potassium REAL NOT NULL DEFAULT 0, phosphorous REAL NOT NULL DEFAULT 0, temperature REAL NOT NULL DEFAULT 0, humidity REAL NOT NULL DEFAULT 0, ph REAL NOT NULL DEFAULT 0)");
            database.execSQL("INSERT INTO new_HourlyData (id, timestamp, tankId, ec, moisture, nitrogen, potassium, phosphorous, temperature, humidity, ph) SELECT id, timestamp, tankId, ec, moisture, nitrogen, potassium, phosphorous, temperature, humidity, ph FROM HourlyData");
            database.execSQL("DROP TABLE HourlyData");
            database.execSQL("ALTER TABLE new_HourlyData RENAME TO HourlyData");
        }
    };
}
