package cmeza.pokedex.ui.main.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import cmeza.pokedex.R
import cmeza.pokedex.core.widgets.WidgetLoading
import cmeza.pokedex.databinding.FragmentDetailBinding
import cmeza.pokedex.models.Abilities
import cmeza.pokedex.models.Moves
import cmeza.pokedex.models.Types
import cmeza.pokedex.models.Ubicacion
import cmeza.pokedex.ui.main.android.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentDetail: Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var loading: WidgetLoading

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        loading = WidgetLoading(requireContext())
        loading.Create()

        binding.apply {
            ubicacionPokemon = " - "
            tipoPokemon = " - "
            ataquePokemon = " - "
            habilidadPokemon = " - "
            evolucionPokemon = " - "
        }

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observePokemonDetail.observe(viewLifecycleOwner){
            if (it!=null){
                binding.pokemon = it
                binding.tipoPokemon = crearStringTipos(it.detail!!.types)
                binding.ataquePokemon = crearStringAtaques(it.detail!!.moves)
                binding.habilidadPokemon = crearStringHabilidad(it.detail!!.abilities)
                binding.ubicacionPokemon = crearStringUbicacion(it.detail!!.locations)
                binding.evolucionPokemon = it.detail!!.evolutions.chain.evolves_to[0].species.name
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.resetPokemonDetail()
                activity?.supportFragmentManager!!.popBackStack()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        val TAG = "detailFragment"
    }

    private fun crearStringHabilidad(abi: ArrayList<Abilities>): String{
        var str = ""
        abi.forEach {
            str += it.ability.name + "\n"
        }
        return str
    }

    private fun crearStringTipos(typesArr: ArrayList<Types>): String{
        var str = ""
        typesArr.forEach {
            str += it.type.name + "\n"
        }
        return str
    }

    private fun crearStringAtaques(typesArr: ArrayList<Moves>): String{
        var str = ""
        typesArr.forEach {
            str += it.move.name + "\n"
        }
        return str
    }

    private fun crearStringUbicacion(location: List<Ubicacion>): String{
        var str = ""
        location.forEach {
            str += it.location_area.name + "\n"
        }
        return str
    }
}