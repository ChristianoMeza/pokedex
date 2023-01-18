package cmeza.pokedex.ui.main.android

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import cmeza.pokedex.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object MainBindingAdapter {
    @JvmStatic
    @BindingAdapter("app:pokemonImg")
    fun setSignatureUser(imageView: ImageView, imgUrl: String) {
        Log.d("MainBindingAdapter", imgUrl)

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

        Glide.with(imageView.context)
            .load(imgUrl)
            .apply(requestOptions)
            .placeholder(R.drawable.pokemon) // any placeholder to load at start
            .error(R.drawable.pokemon)  // any image in case of error
            .override(200, 200) // resizing
            .skipMemoryCache( true )
            .centerCrop()
            .into(imageView)
    }
}