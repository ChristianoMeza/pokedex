package cmeza.pokedex.ui.main.views
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cmeza.pokedex.databinding.WidgetCellBinding
import cmeza.pokedex.models.PokemonResult

class HomeAdapter (val list: List<PokemonResult>) : RecyclerView.Adapter<HomeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding  = WidgetCellBinding.inflate(inflater, parent, false)
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val pokemon = list[position]
        holder.bind(pokemon, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

class HomeViewHolder (val binding: WidgetCellBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(pokemon: PokemonResult,  idx: Int) {
        binding.cell = pokemon
        binding.executePendingBindings()
    }
}