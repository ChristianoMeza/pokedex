package cmeza.pokedex.ui.main.views
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import cmeza.pokedex.databinding.WidgetCellBinding
import cmeza.pokedex.models.PokemonResult
import cmeza.pokedex.ui.main.android.MainInterfaces

class HomeAdapter (val list: List<PokemonResult>, val event: MainInterfaces) : RecyclerView.Adapter<HomeViewHolder>(),
    Filterable {

    var mFilter: List<PokemonResult>
    init {
        mFilter = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding  = WidgetCellBinding.inflate(inflater, parent, false)
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val pokemon = mFilter[position]
        holder.bind(pokemon, event)
    }

    override fun getItemCount(): Int {
        return mFilter.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().lowercase()
                if (charString.isEmpty()) {
                    mFilter = list
                } else {
                    val filteredList = ArrayList<PokemonResult>()
                    for (row in list) {
                        if (row.name.lowercase().trim().contains(charString)) {
                            filteredList.add(row)
                        }
                    }
                    mFilter = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = mFilter
                return filterResults
            }
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                mFilter = filterResults.values as List<PokemonResult>
                if (mFilter.size != list.size){
                    notifyItemRangeInserted(0, mFilter.size)
                }
            }
        }
    }
}

class HomeViewHolder (val binding: WidgetCellBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(pokemon: PokemonResult,  event: MainInterfaces) {
        binding.cell = pokemon
        binding.event = event
        binding.executePendingBindings()
    }
}