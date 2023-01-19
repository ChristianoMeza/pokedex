package cmeza.pokedex.core.storage


import android.content.Context
import cmeza.pokedex.core.Constants
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets

class StorageManager (val context: Context){

    fun getStorageDir(): String{
        return "${context.externalMediaDirs.first()}/${Constants().STORAGE}"
    }

    fun createDir(): Boolean{
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, Constants().STORAGE).apply { mkdirs() } }
        return mediaDir!!.isDirectory()
    }

    fun saveDataStorage(data: STORAGE_DATA, json: String, id: Int?){
        createDir()
        var strFile = ""
        when(data){
            STORAGE_DATA.POKEMON_LIST->{
                strFile = JSON_POKEMON_LIST
            }
            STORAGE_DATA.POKEMON_DETAIL->{
                strFile = "${id}${JSON_PEKEMON_DETAIL}"
            }
        }
        File(getStorageDir(), strFile).printWriter().use { out ->
            out.println(json)
        }
    }

    fun getDataStorage(data: STORAGE_DATA, id: Int?): String?{
        var strFile = ""
        when(data){
            STORAGE_DATA.POKEMON_LIST->{
                strFile = JSON_POKEMON_LIST
            }
            STORAGE_DATA.POKEMON_DETAIL->{
                strFile = "${id}${JSON_PEKEMON_DETAIL}"
            }
        }
        val file = File("${getStorageDir()}/${strFile}")
        if (file.exists()){
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8)
        }
        return null
    }

    companion object{
        const val JSON_POKEMON_LIST = "pokemon.json"
        const val JSON_PEKEMON_DETAIL = "-pokemon-det.json"
    }
}

enum class STORAGE_DATA {
    POKEMON_LIST,
    POKEMON_DETAIL
}