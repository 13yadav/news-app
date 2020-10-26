package com.strange.coder.news.data

import androidx.room.TypeConverter
import com.strange.coder.news.data.model.Source

class Converters {

    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }

}