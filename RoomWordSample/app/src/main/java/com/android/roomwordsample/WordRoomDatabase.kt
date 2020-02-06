package com.android.roomwordsample

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Word::class), version = 1, exportSchema = false)
public abstract class WordRoomDatabase: RoomDatabase(){

    private class WordDatabaseCallback(
        private val scope: CoroutineScope): RoomDatabase.Callback(){
        override fun onOpen(db: SupportSQLiteDatabase){
            super.onOpen(db)
            INSTANCE?.let { database->
                scope.launch {
                    populateDatabase(database.wordDao())
                }
            }
        }
        suspend fun populateDatabase(wordDao: WordDao){
            wordDao.deleteAll()
            var word = Word("Abu Ghalib's List")
            wordDao.insert(word)
        }
    }

    abstract fun wordDao(): WordDao

    companion object{
        @Volatile
        private var INSTANCE: WordRoomDatabase?= null

        fun getDatabase(context: Context, scope: CoroutineScope): WordRoomDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext, WordRoomDatabase::class.java,
                    "word_database"
                ).addCallback(WordDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }

    }


}