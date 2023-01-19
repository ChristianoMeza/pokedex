package cmeza.pokedex.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cmeza.pokedex.core.Constants
import cmeza.pokedex.core.server.OkHttpServer
import cmeza.pokedex.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.lang.reflect.Type
import java.net.URL


class PokemonRepositoryImpl: PokemonRepository {

    val pokeList      = MutableLiveData<List<PokemonResult>>()
    val pokeFilter    = MutableLiveData<String>()
    val pokemonDetail = MutableLiveData<PokemonDetail?>()
    val pokeInfo      = MutableLiveData<PokemonResult>()

    private fun map(from: Pokemon): ArrayList<PokemonResult> {
        val itemList = ArrayList<PokemonResult>()
        if (from.results.isNotEmpty()){
            from.results.forEach { poke ->
                val clear = poke.url.substring(0, poke.url.length - 1);
                val id    = clear.substring(clear.lastIndexOf("/"))
                itemList.add(
                    PokemonResult(
                        name   = poke.name,
                        url    = poke.url,
                        imgUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon${id}.png",
                        evolutionUrl = "https://pokeapi.co/api/v2/evolution-chain/${id}/",
                        detail = null
                    )
                )
            }
        }
        return itemList
    }

    override fun filtrarPokemon(str: String){
        pokeFilter.postValue(str)
    }

    override fun obtenerPokemon(): MutableLiveData<String> {
        return pokeFilter
    }

    override fun solicitarPokelist() {
        val request = Request.Builder()
            .url(Constants().API_POKEDEX_ALL)
            .build()
        OkHttpServer().unsafeOkHttpClient().build()
            .newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }
                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (response.isSuccessful){
                            val responseData: Pokemon = Gson().fromJson(response.body!!.string(), Pokemon::class.java)
                            if (responseData.results.size > 0){
                                pokeList.postValue(map(responseData))
                            }
                        }
                    }
                }
            })
    }
    override fun obtenerPokelist(): LiveData<List<PokemonResult>> {
        return pokeList
    }

    override fun resetDatosPokemon() {
        pokemonDetail.postValue(null)
    }


    override fun obtenerInfoPokemon(): MutableLiveData<PokemonResult> {
        return pokeInfo
    }

    override fun solicitarInfoPokemon(pr: PokemonResult) {

        val requestDetail = Request.Builder()
            .url(pr.url)
            .build()
        val requestDetailCall: Call = OkHttpServer().unsafeOkHttpClient().build().newCall(requestDetail)

        requestDetailCall.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val detail: PokemonDetail = Gson().fromJson(response.body!!.string(), PokemonDetail::class.java)
                    pr.detail = detail
                    val evolution: Evolucion? = getEvolution(pr.evolutionUrl)
                    if (evolution != null){
                        pr.detail!!.evolutions = evolution
                        val locations: List<Ubicacion>? = getLocation(detail.location_area_encounters)
                        if (locations != null){
                            pr.detail!!.locations = locations
                            pokeInfo.postValue(pr)
                        }
                    }
                }
            }
        })
    }

    private fun getEvolution(sUrl: String): Evolucion? {
        var result: Evolucion? = null
        try {
            val url = URL(sUrl)
            val request = Request.Builder().url(url).build()
            val response = OkHttpServer().unsafeOkHttpClient().build().newCall(request).execute()
            if (response.isSuccessful){
                val evolutionData: Evolucion = Gson().fromJson(response.body!!.string(), Evolucion::class.java)
                if (evolutionData.chain.evolves_to.size > 0){
                    result = evolutionData
                }
            }
        }
        catch(err:Error) {
            Log.d(TAG,"${err.localizedMessage}")
        }
        return result
    }

    private fun getLocation(sUrl: String): List<Ubicacion>? {
        var result: List<Ubicacion>? = null
        try {
            val url = URL(sUrl)
            val request = Request.Builder().url(url).build()
            val response = OkHttpServer().unsafeOkHttpClient().build().newCall(request).execute()
            if (response.isSuccessful){
                val collectionType: Type = object : TypeToken<List<Ubicacion?>?>() {}.type
                val list: List<Ubicacion> = Gson().fromJson(response.body!!.string(), collectionType)
                result = list
            }
        }
        catch(err:Error) {
            Log.d(TAG,"${err.localizedMessage}")
        }
        return result
    }

    companion object {
        val TAG = "PokeRepo"
    }
}