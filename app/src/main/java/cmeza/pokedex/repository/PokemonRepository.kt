package cmeza.pokedex.repository

import androidx.lifecycle.LiveData
import cmeza.pokedex.models.PokemonResult

interface PokemonRepository {
    fun solicitarPokelist()
    fun obtenerPokelist(): LiveData<List<PokemonResult>>
}