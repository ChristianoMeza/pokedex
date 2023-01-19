package cmeza.pokedex.core.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager

class PokemonGridLayoutManager(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : GridLayoutManager(context, attrs, defStyleAttr, defStyleRes) {

    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }

    override fun setSpanCount(spanCount: Int) {
        super.setSpanCount(3)
    }

}