package com.sheikhnaim2026.superpodcast.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * AppDatabase
 *
 * Main Room database instance for SuperPodcast.
 * Uses the Singleton pattern with double-checked locking to ensure only one database instance exists.
 */
@Database(entities = [SubscribedPodcast::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to subscription database operations.
     */
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of [AppDatabase], creating it on first access.
         *
         * @param context Application context used to build the database.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "superpodcast_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
