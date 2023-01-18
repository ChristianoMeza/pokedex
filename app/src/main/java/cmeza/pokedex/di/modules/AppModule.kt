package cmeza.pokedex.di.modules

import cmeza.pokedex.repository.PokemonRepository
import cmeza.pokedex.repository.PokemonRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun providePokemonRepository(): PokemonRepository = PokemonRepositoryImpl()
}
