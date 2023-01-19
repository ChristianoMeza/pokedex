package cmeza.pokedex.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cmeza.pokedex.core.Constants
import cmeza.pokedex.core.server.ServerFailure
import cmeza.pokedex.core.server.OkHttpServer
import cmeza.pokedex.core.storage.STORAGE_DATA
import cmeza.pokedex.core.storage.StorageManager
import cmeza.pokedex.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.lang.reflect.Type
import java.net.URL


class PokemonRepositoryImpl: PokemonRepository {

    val pokeList        = MutableLiveData<List<PokemonResult>>()
    val pokeFilter      = MutableLiveData<String>()
    val pokemonDetail   = MutableLiveData<PokemonDetail?>()
    val pokeInfo        = MutableLiveData<PokemonResult>()
    val pokeListError   = MutableLiveData<ServerFailure?>()
    val pokeDetailError = MutableLiveData<ServerFailure?>()

    private fun map(from: Pokemon): ArrayList<PokemonResult> {
        val itemList = ArrayList<PokemonResult>()
        if (from.results.isNotEmpty()){
            from.results.forEach { poke ->
                val clear = poke.url.substring(0, poke.url.length - 1);
                val id    = Integer.parseInt(clear.substring(clear.lastIndexOf("/")).replace("/",""))
                itemList.add(
                    PokemonResult(
                        id     = id,
                        name   = poke.name,
                        url    = poke.url,
                        imgUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${id}.png",
                        evolutionUrl = "https://pokeapi.co/api/v2/evolution-chain/${id}/",
                        detail = null
                    )
                )
            }
        }
        return itemList
    }

    override fun obtenerPokemon(): MutableLiveData<String> {
        return pokeFilter
    }

    override fun solicitarPokelist(ctx: Context) {
        val request = Request.Builder()
            .url(Constants().API_POKEDEX_ALL)
            .build()
        OkHttpServer().unsafeOkHttpClient().build()
            .newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val storageProfile = StorageManager(ctx).getDataStorage(STORAGE_DATA.POKEMON_LIST, null)
                    if (storageProfile != null){
                        val localData: Pokemon = Gson().fromJson(storageProfile, Pokemon::class.java)
                        val map = map(localData)
                        pokeList.postValue(map)
                    }else{
                        pokeListError.postValue(ServerFailure(message = "No se pudo obtener datos, revisa tu conexión a internet y luego intenta nuevamente."))
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (response.isSuccessful){
                            val responseData: Pokemon = Gson().fromJson(response.body!!.string(), Pokemon::class.java)
                            if (responseData.results.size > 0){
                                val map = map(responseData)
                                pokeList.postValue(map)

                                //Guarda el array para luego ser usuado son conexión
                                StorageManager(ctx).saveDataStorage(
                                    STORAGE_DATA.POKEMON_LIST,
                                    JSONObject(Gson().toJson(responseData, Pokemon::class.java)).toString(),
                                    null
                                )
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

    override fun solicitarInfoPokemon(pr: PokemonResult, ctx: Context) {

        val requestDetail = Request.Builder()
            .url(pr.url)
            .build()
        val requestDetailCall: Call = OkHttpServer().unsafeOkHttpClient().build().newCall(requestDetail)

        requestDetailCall.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val storageProfile = StorageManager(ctx).getDataStorage(STORAGE_DATA.POKEMON_DETAIL, pr.id)
                if (storageProfile != null){
                    val detail: PokemonResult = Gson().fromJson(storageProfile, PokemonResult::class.java)
                    pr.detail = detail.detail
                    pokeInfo.postValue(pr)
                }else{
                    pokeDetailError.postValue(ServerFailure(message = "No se pudo obtener datos, (Sin datos en cache para este pokemon) revisa tu conexión a internet y luego intenta nuevamente."))
                }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val detail: PokemonDetail =
                            Gson().fromJson(response.body!!.string(), PokemonDetail::class.java)
                        pr.detail = detail

                        val evolution: Evolucion? = getEvolution(pr.evolutionUrl)
                        if (evolution != null) {
                            pr.detail!!.evolutions = evolution
                        }

                        val locations: List<Ubicacion>? =
                            getLocation(detail.location_area_encounters)
                        if (locations != null) {
                            pr.detail!!.locations = locations
                        }

                        pokeInfo.postValue(pr)

                        //Guardar el detalle del pokemon
                        guardarDetallePokemon(ctx, pr)
                    }
                }
            }
        })
    }

    private fun guardarDetallePokemon(ctx: Context, pr: PokemonResult){
        StorageManager(ctx).saveDataStorage(
            STORAGE_DATA.POKEMON_DETAIL,
            JSONObject(Gson().toJson(pr, PokemonResult::class.java)).toString(),
            pr.id
        )
    }

    override fun obtenerPokeListError(): MutableLiveData<ServerFailure?> {
        return pokeListError
    }

    override fun obtenerPokeDetailError(): MutableLiveData<ServerFailure?> {
        return pokeDetailError
    }

    override fun resetPokeListError() {
        pokeListError.postValue(null)
    }

    override fun resetPokeDetailError() {
        pokeDetailError.postValue(null)
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
        val FAKE_RESPONSE = "{\"count\":1279,\"next\":\"https://pokeapi.co/api/v2/pokemon?offset=151&limit=151\",\"previous\":null,\"results\":[{\"name\":\"bulbasaur\",\"url\":\"https://pokeapi.co/api/v2/pokemon/1/\"},{\"name\":\"ivysaur\",\"url\":\"https://pokeapi.co/api/v2/pokemon/2/\"},{\"name\":\"venusaur\",\"url\":\"https://pokeapi.co/api/v2/pokemon/3/\"},{\"name\":\"charmander\",\"url\":\"https://pokeapi.co/api/v2/pokemon/4/\"},{\"name\":\"charmeleon\",\"url\":\"https://pokeapi.co/api/v2/pokemon/5/\"},{\"name\":\"charizard\",\"url\":\"https://pokeapi.co/api/v2/pokemon/6/\"},{\"name\":\"squirtle\",\"url\":\"https://pokeapi.co/api/v2/pokemon/7/\"},{\"name\":\"wartortle\",\"url\":\"https://pokeapi.co/api/v2/pokemon/8/\"},{\"name\":\"blastoise\",\"url\":\"https://pokeapi.co/api/v2/pokemon/9/\"},{\"name\":\"caterpie\",\"url\":\"https://pokeapi.co/api/v2/pokemon/10/\"},{\"name\":\"metapod\",\"url\":\"https://pokeapi.co/api/v2/pokemon/11/\"},{\"name\":\"butterfree\",\"url\":\"https://pokeapi.co/api/v2/pokemon/12/\"},{\"name\":\"weedle\",\"url\":\"https://pokeapi.co/api/v2/pokemon/13/\"},{\"name\":\"kakuna\",\"url\":\"https://pokeapi.co/api/v2/pokemon/14/\"},{\"name\":\"beedrill\",\"url\":\"https://pokeapi.co/api/v2/pokemon/15/\"},{\"name\":\"pidgey\",\"url\":\"https://pokeapi.co/api/v2/pokemon/16/\"},{\"name\":\"pidgeotto\",\"url\":\"https://pokeapi.co/api/v2/pokemon/17/\"},{\"name\":\"pidgeot\",\"url\":\"https://pokeapi.co/api/v2/pokemon/18/\"},{\"name\":\"rattata\",\"url\":\"https://pokeapi.co/api/v2/pokemon/19/\"},{\"name\":\"raticate\",\"url\":\"https://pokeapi.co/api/v2/pokemon/20/\"},{\"name\":\"spearow\",\"url\":\"https://pokeapi.co/api/v2/pokemon/21/\"},{\"name\":\"fearow\",\"url\":\"https://pokeapi.co/api/v2/pokemon/22/\"},{\"name\":\"ekans\",\"url\":\"https://pokeapi.co/api/v2/pokemon/23/\"},{\"name\":\"arbok\",\"url\":\"https://pokeapi.co/api/v2/pokemon/24/\"},{\"name\":\"pikachu\",\"url\":\"https://pokeapi.co/api/v2/pokemon/25/\"},{\"name\":\"raichu\",\"url\":\"https://pokeapi.co/api/v2/pokemon/26/\"},{\"name\":\"sandshrew\",\"url\":\"https://pokeapi.co/api/v2/pokemon/27/\"},{\"name\":\"sandslash\",\"url\":\"https://pokeapi.co/api/v2/pokemon/28/\"},{\"name\":\"nidoran-f\",\"url\":\"https://pokeapi.co/api/v2/pokemon/29/\"},{\"name\":\"nidorina\",\"url\":\"https://pokeapi.co/api/v2/pokemon/30/\"},{\"name\":\"nidoqueen\",\"url\":\"https://pokeapi.co/api/v2/pokemon/31/\"},{\"name\":\"nidoran-m\",\"url\":\"https://pokeapi.co/api/v2/pokemon/32/\"},{\"name\":\"nidorino\",\"url\":\"https://pokeapi.co/api/v2/pokemon/33/\"},{\"name\":\"nidoking\",\"url\":\"https://pokeapi.co/api/v2/pokemon/34/\"},{\"name\":\"clefairy\",\"url\":\"https://pokeapi.co/api/v2/pokemon/35/\"},{\"name\":\"clefable\",\"url\":\"https://pokeapi.co/api/v2/pokemon/36/\"},{\"name\":\"vulpix\",\"url\":\"https://pokeapi.co/api/v2/pokemon/37/\"},{\"name\":\"ninetales\",\"url\":\"https://pokeapi.co/api/v2/pokemon/38/\"},{\"name\":\"jigglypuff\",\"url\":\"https://pokeapi.co/api/v2/pokemon/39/\"},{\"name\":\"wigglytuff\",\"url\":\"https://pokeapi.co/api/v2/pokemon/40/\"},{\"name\":\"zubat\",\"url\":\"https://pokeapi.co/api/v2/pokemon/41/\"},{\"name\":\"golbat\",\"url\":\"https://pokeapi.co/api/v2/pokemon/42/\"},{\"name\":\"oddish\",\"url\":\"https://pokeapi.co/api/v2/pokemon/43/\"},{\"name\":\"gloom\",\"url\":\"https://pokeapi.co/api/v2/pokemon/44/\"},{\"name\":\"vileplume\",\"url\":\"https://pokeapi.co/api/v2/pokemon/45/\"},{\"name\":\"paras\",\"url\":\"https://pokeapi.co/api/v2/pokemon/46/\"},{\"name\":\"parasect\",\"url\":\"https://pokeapi.co/api/v2/pokemon/47/\"},{\"name\":\"venonat\",\"url\":\"https://pokeapi.co/api/v2/pokemon/48/\"},{\"name\":\"venomoth\",\"url\":\"https://pokeapi.co/api/v2/pokemon/49/\"},{\"name\":\"diglett\",\"url\":\"https://pokeapi.co/api/v2/pokemon/50/\"},{\"name\":\"dugtrio\",\"url\":\"https://pokeapi.co/api/v2/pokemon/51/\"},{\"name\":\"meowth\",\"url\":\"https://pokeapi.co/api/v2/pokemon/52/\"},{\"name\":\"persian\",\"url\":\"https://pokeapi.co/api/v2/pokemon/53/\"},{\"name\":\"psyduck\",\"url\":\"https://pokeapi.co/api/v2/pokemon/54/\"},{\"name\":\"golduck\",\"url\":\"https://pokeapi.co/api/v2/pokemon/55/\"},{\"name\":\"mankey\",\"url\":\"https://pokeapi.co/api/v2/pokemon/56/\"},{\"name\":\"primeape\",\"url\":\"https://pokeapi.co/api/v2/pokemon/57/\"},{\"name\":\"growlithe\",\"url\":\"https://pokeapi.co/api/v2/pokemon/58/\"},{\"name\":\"arcanine\",\"url\":\"https://pokeapi.co/api/v2/pokemon/59/\"},{\"name\":\"poliwag\",\"url\":\"https://pokeapi.co/api/v2/pokemon/60/\"},{\"name\":\"poliwhirl\",\"url\":\"https://pokeapi.co/api/v2/pokemon/61/\"},{\"name\":\"poliwrath\",\"url\":\"https://pokeapi.co/api/v2/pokemon/62/\"},{\"name\":\"abra\",\"url\":\"https://pokeapi.co/api/v2/pokemon/63/\"},{\"name\":\"kadabra\",\"url\":\"https://pokeapi.co/api/v2/pokemon/64/\"},{\"name\":\"alakazam\",\"url\":\"https://pokeapi.co/api/v2/pokemon/65/\"},{\"name\":\"machop\",\"url\":\"https://pokeapi.co/api/v2/pokemon/66/\"},{\"name\":\"machoke\",\"url\":\"https://pokeapi.co/api/v2/pokemon/67/\"},{\"name\":\"machamp\",\"url\":\"https://pokeapi.co/api/v2/pokemon/68/\"},{\"name\":\"bellsprout\",\"url\":\"https://pokeapi.co/api/v2/pokemon/69/\"},{\"name\":\"weepinbell\",\"url\":\"https://pokeapi.co/api/v2/pokemon/70/\"},{\"name\":\"victreebel\",\"url\":\"https://pokeapi.co/api/v2/pokemon/71/\"},{\"name\":\"tentacool\",\"url\":\"https://pokeapi.co/api/v2/pokemon/72/\"},{\"name\":\"tentacruel\",\"url\":\"https://pokeapi.co/api/v2/pokemon/73/\"},{\"name\":\"geodude\",\"url\":\"https://pokeapi.co/api/v2/pokemon/74/\"},{\"name\":\"graveler\",\"url\":\"https://pokeapi.co/api/v2/pokemon/75/\"},{\"name\":\"golem\",\"url\":\"https://pokeapi.co/api/v2/pokemon/76/\"},{\"name\":\"ponyta\",\"url\":\"https://pokeapi.co/api/v2/pokemon/77/\"},{\"name\":\"rapidash\",\"url\":\"https://pokeapi.co/api/v2/pokemon/78/\"},{\"name\":\"slowpoke\",\"url\":\"https://pokeapi.co/api/v2/pokemon/79/\"},{\"name\":\"slowbro\",\"url\":\"https://pokeapi.co/api/v2/pokemon/80/\"},{\"name\":\"magnemite\",\"url\":\"https://pokeapi.co/api/v2/pokemon/81/\"},{\"name\":\"magneton\",\"url\":\"https://pokeapi.co/api/v2/pokemon/82/\"},{\"name\":\"farfetchd\",\"url\":\"https://pokeapi.co/api/v2/pokemon/83/\"},{\"name\":\"doduo\",\"url\":\"https://pokeapi.co/api/v2/pokemon/84/\"},{\"name\":\"dodrio\",\"url\":\"https://pokeapi.co/api/v2/pokemon/85/\"},{\"name\":\"seel\",\"url\":\"https://pokeapi.co/api/v2/pokemon/86/\"},{\"name\":\"dewgong\",\"url\":\"https://pokeapi.co/api/v2/pokemon/87/\"},{\"name\":\"grimer\",\"url\":\"https://pokeapi.co/api/v2/pokemon/88/\"},{\"name\":\"muk\",\"url\":\"https://pokeapi.co/api/v2/pokemon/89/\"},{\"name\":\"shellder\",\"url\":\"https://pokeapi.co/api/v2/pokemon/90/\"},{\"name\":\"cloyster\",\"url\":\"https://pokeapi.co/api/v2/pokemon/91/\"},{\"name\":\"gastly\",\"url\":\"https://pokeapi.co/api/v2/pokemon/92/\"},{\"name\":\"haunter\",\"url\":\"https://pokeapi.co/api/v2/pokemon/93/\"},{\"name\":\"gengar\",\"url\":\"https://pokeapi.co/api/v2/pokemon/94/\"},{\"name\":\"onix\",\"url\":\"https://pokeapi.co/api/v2/pokemon/95/\"},{\"name\":\"drowzee\",\"url\":\"https://pokeapi.co/api/v2/pokemon/96/\"},{\"name\":\"hypno\",\"url\":\"https://pokeapi.co/api/v2/pokemon/97/\"},{\"name\":\"krabby\",\"url\":\"https://pokeapi.co/api/v2/pokemon/98/\"},{\"name\":\"kingler\",\"url\":\"https://pokeapi.co/api/v2/pokemon/99/\"},{\"name\":\"voltorb\",\"url\":\"https://pokeapi.co/api/v2/pokemon/100/\"},{\"name\":\"electrode\",\"url\":\"https://pokeapi.co/api/v2/pokemon/101/\"},{\"name\":\"exeggcute\",\"url\":\"https://pokeapi.co/api/v2/pokemon/102/\"},{\"name\":\"exeggutor\",\"url\":\"https://pokeapi.co/api/v2/pokemon/103/\"},{\"name\":\"cubone\",\"url\":\"https://pokeapi.co/api/v2/pokemon/104/\"},{\"name\":\"marowak\",\"url\":\"https://pokeapi.co/api/v2/pokemon/105/\"},{\"name\":\"hitmonlee\",\"url\":\"https://pokeapi.co/api/v2/pokemon/106/\"},{\"name\":\"hitmonchan\",\"url\":\"https://pokeapi.co/api/v2/pokemon/107/\"},{\"name\":\"lickitung\",\"url\":\"https://pokeapi.co/api/v2/pokemon/108/\"},{\"name\":\"koffing\",\"url\":\"https://pokeapi.co/api/v2/pokemon/109/\"},{\"name\":\"weezing\",\"url\":\"https://pokeapi.co/api/v2/pokemon/110/\"},{\"name\":\"rhyhorn\",\"url\":\"https://pokeapi.co/api/v2/pokemon/111/\"},{\"name\":\"rhydon\",\"url\":\"https://pokeapi.co/api/v2/pokemon/112/\"},{\"name\":\"chansey\",\"url\":\"https://pokeapi.co/api/v2/pokemon/113/\"},{\"name\":\"tangela\",\"url\":\"https://pokeapi.co/api/v2/pokemon/114/\"},{\"name\":\"kangaskhan\",\"url\":\"https://pokeapi.co/api/v2/pokemon/115/\"},{\"name\":\"horsea\",\"url\":\"https://pokeapi.co/api/v2/pokemon/116/\"},{\"name\":\"seadra\",\"url\":\"https://pokeapi.co/api/v2/pokemon/117/\"},{\"name\":\"goldeen\",\"url\":\"https://pokeapi.co/api/v2/pokemon/118/\"},{\"name\":\"seaking\",\"url\":\"https://pokeapi.co/api/v2/pokemon/119/\"},{\"name\":\"staryu\",\"url\":\"https://pokeapi.co/api/v2/pokemon/120/\"},{\"name\":\"starmie\",\"url\":\"https://pokeapi.co/api/v2/pokemon/121/\"},{\"name\":\"mr-mime\",\"url\":\"https://pokeapi.co/api/v2/pokemon/122/\"},{\"name\":\"scyther\",\"url\":\"https://pokeapi.co/api/v2/pokemon/123/\"},{\"name\":\"jynx\",\"url\":\"https://pokeapi.co/api/v2/pokemon/124/\"},{\"name\":\"electabuzz\",\"url\":\"https://pokeapi.co/api/v2/pokemon/125/\"},{\"name\":\"magmar\",\"url\":\"https://pokeapi.co/api/v2/pokemon/126/\"},{\"name\":\"pinsir\",\"url\":\"https://pokeapi.co/api/v2/pokemon/127/\"},{\"name\":\"tauros\",\"url\":\"https://pokeapi.co/api/v2/pokemon/128/\"},{\"name\":\"magikarp\",\"url\":\"https://pokeapi.co/api/v2/pokemon/129/\"},{\"name\":\"gyarados\",\"url\":\"https://pokeapi.co/api/v2/pokemon/130/\"},{\"name\":\"lapras\",\"url\":\"https://pokeapi.co/api/v2/pokemon/131/\"},{\"name\":\"ditto\",\"url\":\"https://pokeapi.co/api/v2/pokemon/132/\"},{\"name\":\"eevee\",\"url\":\"https://pokeapi.co/api/v2/pokemon/133/\"},{\"name\":\"vaporeon\",\"url\":\"https://pokeapi.co/api/v2/pokemon/134/\"},{\"name\":\"jolteon\",\"url\":\"https://pokeapi.co/api/v2/pokemon/135/\"},{\"name\":\"flareon\",\"url\":\"https://pokeapi.co/api/v2/pokemon/136/\"},{\"name\":\"porygon\",\"url\":\"https://pokeapi.co/api/v2/pokemon/137/\"},{\"name\":\"omanyte\",\"url\":\"https://pokeapi.co/api/v2/pokemon/138/\"},{\"name\":\"omastar\",\"url\":\"https://pokeapi.co/api/v2/pokemon/139/\"},{\"name\":\"kabuto\",\"url\":\"https://pokeapi.co/api/v2/pokemon/140/\"},{\"name\":\"kabutops\",\"url\":\"https://pokeapi.co/api/v2/pokemon/141/\"},{\"name\":\"aerodactyl\",\"url\":\"https://pokeapi.co/api/v2/pokemon/142/\"},{\"name\":\"snorlax\",\"url\":\"https://pokeapi.co/api/v2/pokemon/143/\"},{\"name\":\"articuno\",\"url\":\"https://pokeapi.co/api/v2/pokemon/144/\"},{\"name\":\"zapdos\",\"url\":\"https://pokeapi.co/api/v2/pokemon/145/\"},{\"name\":\"moltres\",\"url\":\"https://pokeapi.co/api/v2/pokemon/146/\"},{\"name\":\"dratini\",\"url\":\"https://pokeapi.co/api/v2/pokemon/147/\"},{\"name\":\"dragonair\",\"url\":\"https://pokeapi.co/api/v2/pokemon/148/\"},{\"name\":\"dragonite\",\"url\":\"https://pokeapi.co/api/v2/pokemon/149/\"},{\"name\":\"mewtwo\",\"url\":\"https://pokeapi.co/api/v2/pokemon/150/\"},{\"name\":\"mew\",\"url\":\"https://pokeapi.co/api/v2/pokemon/151/\"}]}"
    }
}