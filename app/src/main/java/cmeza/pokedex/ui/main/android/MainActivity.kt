package cmeza.pokedex.ui.main.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cmeza.pokedex.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}