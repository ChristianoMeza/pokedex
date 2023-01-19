package cmeza.pokedex.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cmeza.pokedex.models.*

interface PokemonRepository {
    fun filtrarPokemon(str: String)
    fun obtenerPokemon(): MutableLiveData<String>
    fun solicitarPokelist()
    fun obtenerPokelist(): LiveData<List<PokemonResult>>
    fun resetDatosPokemon()
    fun obtenerInfoPokemon(): MutableLiveData<PokemonResult>
    fun solicitarInfoPokemon(pr: PokemonResult)
}