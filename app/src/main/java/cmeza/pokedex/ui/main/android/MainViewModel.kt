package cmeza.pokedex.ui.main.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cmeza.pokedex.models.*
import cmeza.pokedex.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val pokemonRepository: PokemonRepository) : ViewModel() {
    val observePokemons: LiveData<List<PokemonResult>> = pokemonRepository.obtenerPokelist()

    init {
        loadPokemons()
    }
    private fun loadPokemons() {
        pokemonRepository.solicitarPokelist()
    }

    fun filterInPokemonList(str: String){
        pokemonRepository.filtrarPokemon(str)
    }
    val observeFilterPokemon: MutableLiveData<String> = pokemonRepository.obtenerPokemon()

    fun getPokemonDetail(pk: PokemonResult){
        pokemonRepository.solicitarInfoPokemon(pk)
    }
    fun resetPokemonDetail(){
        pokemonRepository.resetDatosPokemon()
    }
    val observePokemonDetail: LiveData<PokemonResult?> = pokemonRepository.obtenerInfoPokemon()
}