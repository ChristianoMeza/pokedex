package cmeza.pokedex.core

class Constants {
    val POKE_LIMIT        = 151
    val API_POKEDEX       = "https://pokeapi.co/api/v2/"
    val API_POKEDEX_ALL   = API_POKEDEX + "pokemon?limit=${POKE_LIMIT}"
    val API_POKEDEX_IMAGE = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/{id-pokemon}.png"
}