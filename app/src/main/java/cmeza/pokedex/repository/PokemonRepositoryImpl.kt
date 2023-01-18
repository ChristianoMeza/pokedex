package cmeza.pokedex.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cmeza.pokedex.core.Constants
import cmeza.pokedex.core.server.OkHttpServer
import cmeza.pokedex.models.Pokemon
import cmeza.pokedex.models.PokemonResult
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class PokemonRepositoryImpl: PokemonRepository {
    val pokeList = MutableLiveData<List<PokemonResult>>()
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
                        imgUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon${id}.png"
                    )
                )
            }
        }
        return itemList
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
}