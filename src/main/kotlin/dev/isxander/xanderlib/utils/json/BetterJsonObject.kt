package dev.isxander.xanderlib.utils.json

import com.google.gson.*
import java.io.*
import java.util.stream.Collectors


@Suppress("unused")
class BetterJsonObject {
    private val pp: Gson
    private var data: JsonObject? = null

    constructor() {
        pp = GsonBuilder().setPrettyPrinting().create()
        data = JsonObject()
    }

    constructor(jsonIn: String?) {
        pp = GsonBuilder().setPrettyPrinting().create()
        if (jsonIn == null || jsonIn.isEmpty()) {
            data = JsonObject()
            return
        }
        try {
            data = JsonParser.parseString(jsonIn).asJsonObject
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        } catch (e: JsonIOException) {
            e.printStackTrace()
        }
    }

    constructor(objectIn: JsonObject?) {
        pp = GsonBuilder().setPrettyPrinting().create()
        data = objectIn ?: JsonObject()
    }

    @JvmOverloads
    fun optString(key: String?, value: String = ""): String {
        if (key == null || key.isEmpty() || !has(key)) return value
        val prim = asPrimitive(get(key))
        return if (prim != null && prim.isString) prim.asString else value
    }

    @JvmOverloads
    fun optInt(key: String?, value: Int = 0): Int {
        if (key == null || key.isEmpty() || !has(key)) {
            return value
        }
        val primitive = asPrimitive(this[key])
        try {
            if (primitive != null && primitive.isNumber) {
                return primitive.asInt
            }
        } catch (ignored: NumberFormatException) {
        }
        return value
    }

    @JvmOverloads
    fun optFloat(key: String?, value: Float = 0f): Float {
        if (key == null || key.isEmpty() || !has(key)) return value
        val primitive = asPrimitive(this[key])
        try {
            if (primitive != null && primitive.isNumber) return primitive.asFloat
        } catch (ignored: NumberFormatException) {
        }
        return value
    }

    @JvmOverloads
    fun optDouble(key: String?, value: Double = 0.0): Double {
        if (key == null || key.isEmpty() || !has(key)) {
            return value
        }
        val primitive = asPrimitive(this[key])
        try {
            if (primitive != null && primitive.isNumber) {
                return primitive.asDouble
            }
        } catch (ignored: NumberFormatException) {
        }
        return value
    }

    @JvmOverloads
    fun optBoolean(key: String?, value: Boolean = false): Boolean {
        if (key == null || key.isEmpty() || !has(key)) {
            return value
        }
        val primitive = asPrimitive(this[key])
        return if (primitive != null && primitive.isBoolean) {
            primitive.asBoolean
        } else value
    }

    fun has(key: String?): Boolean {
        return data!!.has(key)
    }

    operator fun get(key: String?): JsonElement {
        return data!![key]
    }

    fun getObj(key: String?): BetterJsonObject {
        return BetterJsonObject(data!!.getAsJsonObject(key))
    }

    fun addProperty(key: String?, value: String?): BetterJsonObject {
        if (key != null) {
            data!!.addProperty(key, value)
        }
        return this
    }

    fun addProperty(key: String?, value: Number?): BetterJsonObject {
        if (key != null) {
            data!!.addProperty(key, value)
        }
        return this
    }

    fun addProperty(key: String?, value: Boolean?): BetterJsonObject {
        if (key != null) {
            data!!.addProperty(key, value)
        }
        return this
    }

    fun add(key: String?, `object`: BetterJsonObject): BetterJsonObject {
        if (key != null) {
            data!!.add(key, `object`.data)
        }
        return this
    }

    fun writeToFile(file: File?) {
        if (file == null || file.exists() && file.isDirectory) {
            return
        }
        try {
            if (!file.exists()) {
                val parent = file.parentFile
                if (parent != null && !parent.exists()) {
                    parent.mkdirs()
                }
                file.createNewFile()
            }
            val writer = FileWriter(file)
            val bufferedWriter = BufferedWriter(writer)
            bufferedWriter.write(toPrettyString())
            bufferedWriter.close()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun asPrimitive(element: JsonElement): JsonPrimitive? {
        return (element as? JsonPrimitive)?.asJsonPrimitive
    }

    val allKeys: List<String>
        get() {
            val keys: MutableList<String> = ArrayList()
            val entrySet = data!!.entrySet()
            for (entry in entrySet){
                keys.add(entry.key)
            }
            return keys
        }

    override fun toString(): String {
        return data.toString()
    }

    fun toPrettyString(): String {
        return pp.toJson(data)
    }

    companion object {
        @Throws(IOException::class)
        fun getFromFile(file: File): BetterJsonObject {
            if (!file.parentFile.exists() || !file.exists()) throw FileNotFoundException()
            val f = BufferedReader(FileReader(file))
            val lines = f.lines().collect(Collectors.toList())
            f.close()
            if (lines.isEmpty()) return BetterJsonObject()
            val builder = java.lang.String.join("", lines)
            return if (builder.trim { it <= ' ' }.isNotEmpty()) BetterJsonObject(builder.trim { it <= ' ' }) else BetterJsonObject()
        }

        fun getFromLines(lines: List<String?>): BetterJsonObject {
            if (lines.isEmpty()) return BetterJsonObject()
            val builder = java.lang.String.join("", lines)
            return if (builder.trim { it <= ' ' }.isNotEmpty()) BetterJsonObject(builder.trim { it <= ' ' }) else BetterJsonObject()
        }
    }
}
