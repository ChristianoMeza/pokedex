package cmeza.pokedex.ui.main.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import cmeza.pokedex.R
import cmeza.pokedex.core.widgets.PokemonGridLayoutManager
import cmeza.pokedex.core.widgets.WidgetLoading
import cmeza.pokedex.databinding.FragmentHomeBinding
import cmeza.pokedex.models.PokemonResult
import cmeza.pokedex.ui.main.android.MainInterfaces
import cmeza.pokedex.ui.main.android.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class FragmentHome: Fragment(), MainInterfaces {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomeAdapter
    private lateinit var loading: WidgetLoading
    private var isPokeClick: Boolean = false

    private var isCheckDetail: Boolean = false

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized){
            binding.apply {
                svPokemon.setQuery("", false)
                svPokemon.clearFocus()
                svPokemon.setIconified(true)
            }
        }
        isPokeClick = false
    }

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
            rvPokemon.recycledViewPool.clear()
            rvPokemon.postDelayed({
                rvPokemon.itemAnimator = DefaultItemAnimator()
            }, 120)

            svPokemon.queryHint = getString(R.string.search)
            svPokemon.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })

        }
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading.Open()

        viewModel.observePokemons.observe(viewLifecycleOwner){
            adapter  = HomeAdapter(it, this)
            binding.rvPokemon.swapAdapter(adapter, true)
            binding.rvPokemon.viewTreeObserver.addOnGlobalLayoutListener { loading.Close() }
        }

        viewModel.observePokemonDetail.observe(viewLifecycleOwner){
            Log.d(TAG, "observePokemonDetail: ${it}")
            if (isCheckDetail){
                if (it != null){
                    isCheckDetail = false
                    val navHostFragment = activity?.supportFragmentManager!!
                        .findFragmentById(R.id.nav_host) as NavHostFragment
                    val navController = navHostFragment.navController
                    navController.navigate(R.id.nav_detail)
                    loading.Close()
                }
            }
        }

        viewModel.observePokemonErrorList.observe(viewLifecycleOwner){
            if (it != null){
                Log.d(TAG, "observePokemonErrorList: ${it}")
                loading.Close()
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                viewModel.resetPokeListError()
            }
        }

        viewModel.observePokemonErrorDetail.observe(viewLifecycleOwner){
            if (it != null) {
                Log.d(TAG, "observePokemonErrorDetail: ${it}")
                loading.Close()
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                viewModel.resetPokeDetailError()
                isPokeClick = false
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val confirmacion = AlertDialog.Builder(requireContext())
                confirmacion.setTitle(R.string.app_name)
                confirmacion.setIcon(R.mipmap.ic_launcher)
                confirmacion.setMessage(R.string.app_close)
                confirmacion.setPositiveButton(R.string.global_si) { dialog, which ->
                    dialog.dismiss()
                    activity?.finish()
                }
                confirmacion.setNegativeButton(R.string.global_no) { dialog, which ->
                    dialog.dismiss()
                }
                confirmacion.show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        val TAG = "HomeFragment"
    }

    override fun onClick(pokemon: PokemonResult) {
        if (!isPokeClick){
            val imm: InputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.windowToken, 0)

            if (imm.isAcceptingText()) {
                CoroutineScope(Dispatchers.IO).launch {
                    delay(500)
                    withContext(Dispatchers.Main) {
                        loading.Open()
                        isCheckDetail = true
                        viewModel.getPokemonDetail(pokemon)
                    }
                }
            }else{
                loading.Open()
                isCheckDetail = true
                viewModel.getPokemonDetail(pokemon)
            }
            isPokeClick = true
        }
    }
}