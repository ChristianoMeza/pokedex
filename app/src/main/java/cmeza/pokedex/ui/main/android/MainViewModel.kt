package cmeza.pokedex.ui.main.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cmeza.pokedex.models.PokemonResult
import cmeza.pokedex.repository.PokemonRepository
import dagger.assisted.AssistedFactory
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
}