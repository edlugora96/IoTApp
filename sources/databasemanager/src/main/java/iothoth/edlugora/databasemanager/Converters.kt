package iothoth.edlugora.databasemanager

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types.newParameterizedType
import iothoth.edlugora.domain.Action
import iothoth.edlugora.domain.Value
import java.lang.reflect.ParameterizedType
import java.sql.Types


class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private var actionType: ParameterizedType = newParameterizedType(
        List::class.java,
        Action::class.java
    )

    private var valueType: ParameterizedType = newParameterizedType(
        List::class.java,
        Value::class.java
    )
    private var actionJsonAdapter: JsonAdapter<List<Action>> = moshi.adapter(actionType)
    private var valueJsonAdapter: JsonAdapter<List<Value>> = moshi.adapter(valueType)


    @TypeConverter
    fun toActionList(data: String?): List<Action> {
        if (data.isNullOrEmpty() || data == "null") {
            return listOf(
                Action(
                    name = "",
                    icon = "",
                    type = "",
                    value = "",
                    url = "",
                )
            )
        }

        return actionJsonAdapter.fromJson(data) as List<Action>
    }

    @TypeConverter
    fun fromActionList(someObjects: List<Action>?): String {
        return actionJsonAdapter.toJson(someObjects) ?: ""
    }

    @TypeConverter
    fun toValueList(data: String?): List<Value> {
        if (data.isNullOrEmpty() || data == "null") {
            return listOf(
                Value(
                    name = "",
                    icon = "",
                    type = "",
                    value = "",
                    url = "",
                )
            )
        }

        return valueJsonAdapter.fromJson(data) as List<Value>
    }

    @TypeConverter
    fun fromValueList(someObjects: List<Value>?): String {
        return valueJsonAdapter.toJson(someObjects) ?: ""
    }

}