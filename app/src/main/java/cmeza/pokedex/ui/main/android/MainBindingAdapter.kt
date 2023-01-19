package cmeza.pokedex.ui.main.android

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import cmeza.pokedex.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition


object MainBindingAdapter {
    @JvmStatic
    @BindingAdapter("app:pokemonImg")
    fun setSignatureUser(imageView: ImageView, imgUrl: String) {
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(imageView.context)
            .load(imgUrl)
            .apply(requestOptions)
            .placeholder(R.drawable.pokemon)
            .error(R.drawable.pokemon)
            .skipMemoryCache( true )
            .centerCrop()
            .into(object : CustomTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable,
                    @Nullable transition: Transition<in Drawable?>?
                ) {
                    imageView.setImageDrawable(resource)
                }
                override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
            })
    }
}