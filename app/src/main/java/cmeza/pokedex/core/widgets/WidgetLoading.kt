package cmeza.pokedex.core.widgets

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import cmeza.pokedex.R
import cmeza.pokedex.databinding.WidgetLoadingBinding


class WidgetLoading (private var ctx: Context) {

    val dialog       = Dialog(ctx)
    var isCancelable = true

    fun Create(){
        val binding: WidgetLoadingBinding = WidgetLoadingBinding.inflate(LayoutInflater.from(ctx))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawableResource(R.color.transparent)
        dialog.setCancelable(isCancelable)
    }
    fun Open(){
        dialog.show()
    }
    fun Close(){
        dialog.dismiss()
    }
}