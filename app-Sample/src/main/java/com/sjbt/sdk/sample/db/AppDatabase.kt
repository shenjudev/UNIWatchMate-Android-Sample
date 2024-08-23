package com.sjbt.sdk.sample.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.sjbt.sdk.sample.entity.DeviceBindEntity
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import com.sjbt.sdk.sample.entity.OxygenItemEntity
import com.sjbt.sdk.sample.entity.SportGoalEntity
import com.sjbt.sdk.sample.entity.StepItemEntity
import com.sjbt.sdk.sample.entity.UnitInfoEntity
import com.sjbt.sdk.sample.entity.UserEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor

@Database(
    version = 5,
    entities = [SportGoalEntity::class, UserEntity::class, DeviceBindEntity::class, UnitInfoEntity::class, StepItemEntity::class, HeartRateItemEntity::class, OxygenItemEntity::class],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingDao(): SettingDao
    abstract fun userDao(): UserDao
    abstract fun syncDataDao(): SyncDataDao

    companion object {
        private const val DB_NAME = "db_sample"
        fun build(context: Context, ioDispatcher: CoroutineDispatcher): AppDatabase {
            val database = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .setQueryExecutor(ioDispatcher.asExecutor())
                //Because this is a sample, version migration is not necessary. So use destructive recreate to avoid crash.
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5)
                .build()
            return database
        }

        //添加字段、表等具体的版本迁移策略(Add specific version migration policies such as fields and tables)
        val MIGRATION_2_3 = Migration(2, 3) {
            it.execSQL("ALTER TABLE DeviceBindEntity ADD COLUMN deviceMode INTEGER NOT NULL DEFAULT 0");
        }
        val MIGRATION_3_4 = Migration(3, 4) {
            it.execSQL("ALTER TABLE DeviceBindEntity ADD COLUMN deviceConnectState INTEGER NOT NULL DEFAULT 0");
        }
        val MIGRATION_4_5 = Migration(4, 5) {
            it.execSQL(
                "CREATE TABLE IF NOT EXISTS `StepItemEntity` (`step` INTEGER NOT NULL, `time` TEXT NOT NULL , `calories` REAL NOT NULL,  `distance` REAL NOT NULL,`userId` INTEGER NOT NULL, PRIMARY KEY(`userId`, `time`)  )"
            )
            it.execSQL(
                "CREATE TABLE IF NOT EXISTS `HeartRateItemEntity` ( `heartRate` INTEGER NOT NULL,`userId` INTEGER NOT NULL, `time` TEXT NOT NULL, PRIMARY KEY(`userId`, `time`))"
            )
            it.execSQL(
                "CREATE TABLE IF NOT EXISTS  `OxygenItemEntity` (`userId` INTEGER NOT NULL, `time` TEXT NOT NULL, `oxygen` INTEGER NOT NULL, PRIMARY KEY(`userId`, `time`))"
            )
        }
    }




}