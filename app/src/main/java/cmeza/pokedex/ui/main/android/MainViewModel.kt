package cmeza.pokedex.ui.main.android

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cmeza.pokedex.core.server.ServerFailure
import cmeza.pokedex.models.PokemonResult
import cmeza.pokedex.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val pokemonRepository: PokemonRepository, @ApplicationContext val context: Context) : ViewModel() {
    val observePokemons: LiveData<List<PokemonResult>> = pokemonRepository.obtenerPokelist()

    init {
        loadPokemons()
    }
    private fun loadPokemons() {
        pokemonRepository.solicitarPokelist(context)
    }

    fun getPokemonDetail(pk: PokemonResult){
        pokemonRepository.solicitarInfoPokemon(pk, context)
    }
    fun resetPokemonDetail(){
        pokemonRepository.resetDatosPokemon()
    }
    val observePokemonDetail: LiveData<PokemonResult?> = pokemonRepository.obtenerInfoPokemon()

    val observePokemonErrorList: LiveData<ServerFailure?> = pokemonRepository.obtenerPokeListError()
    val observePokemonErrorDetail: LiveData<ServerFailure?> = pokemonRepository.obtenerPokeDetailError()

    fun resetPokeListError(){
        pokemonRepository.resetPokeListError()
    }
    fun resetPokeDetailError(){
        pokemonRepository.resetPokeDetailError()
    }
}