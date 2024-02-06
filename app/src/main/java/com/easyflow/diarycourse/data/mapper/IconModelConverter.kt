package com.easyflow.diarycourse.data.mapper

import androidx.room.TypeConverter
import com.easyflow.diarycourse.domain.models.IconModel
import com.google.gson.Gson

class IconModelConverter {

    @TypeConverter
    fun fromIconModel(iconModel: IconModel): String {
        return Gson().toJson(iconModel)
    }

    @TypeConverter
    fun toIconModel(jsonString: String): IconModel {
        return Gson().fromJson(jsonString, IconModel::class.java)
    }
}