package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BlogPostEntity::class,
        PostRevisionEntity::class,
        GalleryItemEntity::class,
        ChatConversationEntity::class,
        ChatMessageEntity::class,
        SiteConfigEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ArchiveDatabase : RoomDatabase() {
    abstract fun archiveDao(): ArchiveDao

    companion object {
        @Volatile
        private var INSTANCE: ArchiveDatabase? = null

        fun getDatabase(context: Context): ArchiveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArchiveDatabase::class.java,
                    "chitrons_archive.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
