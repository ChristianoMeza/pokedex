package cmeza.pokedex.ui.main.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import cmeza.pokedex.R
import cmeza.pokedex.core.widgets.PokemonGridLayoutManager
import cmeza.pokedex.core.widgets.WidgetLoading
import cmeza.pokedex.databinding.FragmentHomeBinding
import cmeza.pokedex.models.PokemonResult
import cmeza.pokedex.ui.main.android.MainInterfaces
import cmeza.pokedex.ui.main.android.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHome: Fragment(), MainInterfaces {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomeAdapter
    private lateinit var loading: WidgetLoading

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        loading = WidgetLoading(requireContext())
        loading.Create()

        binding.apply {
            rvPokemon.layoutManager = PokemonGridLayoutManager(requireContext(),null,0,0)
        }
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading.Open()

        viewModel.observePokemons.observe(viewLifecycleOwner){
            adapter  = HomeAdapter(it, this)
            binding.rvPokemon.adapter = adapter
            binding.rvPokemon.viewTreeObserver.addOnGlobalLayoutListener { loading.Close() }
        }

        viewModel.observeFilterPokemon.observe(viewLifecycleOwner){
            if (::adapter.isInitialized){
                adapter.filter.filter(it)
            }
        }

        viewModel.observePokemonDetail.observe(viewLifecycleOwner){
            loading.Close()
            if (it != null){
                val navHostFragment = activity?.supportFragmentManager!!
                    .findFragmentById(R.id.nav_host) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.nav_detail)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        val TAG = "HomeFragment"
    }

    override fun onClick(pokemon: PokemonResult) {
        loading.Open()
        viewModel.getPokemonDetail(pokemon)
    }
}