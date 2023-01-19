package cmeza.pokedex.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cmeza.pokedex.core.server.ServerFailure
import cmeza.pokedex.models.*

interface PokemonRepository {
    fun obtenerPokemon(): MutableLiveData<String>
    fun solicitarPokelist(ctx: Context)
    fun obtenerPokelist(): LiveData<List<PokemonResult>>
    fun resetDatosPokemon()
    fun obtenerInfoPokemon(): MutableLiveData<PokemonResult>
    fun solicitarInfoPokemon(pr: PokemonResult, ctx: Context)
    fun obtenerPokeListError(): MutableLiveData<ServerFailure?>
    fun obtenerPokeDetailError(): MutableLiveData<ServerFailure?>
    fun resetPokeListError()
    fun resetPokeDetailError()
}