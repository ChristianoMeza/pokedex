package cmeza.pokedex.models

data class Pokemon(
    val results: ArrayList<PokemonResult>
)

data class PokemonResult(
    val id: Int,
    val name: String,
    val url : String,
    val imgUrl : String,
    val evolutionUrl: String,
    var detail: PokemonDetail?
)

data class PokemonDetail(
    val abilities: ArrayList<Abilities>,
    val sprites: Sprites,
    var locations: List<Ubicacion>,
    var evolutions: Evolucion,
    val height: Int,
    val location_area_encounters: String,
    val moves: ArrayList<Moves>,
    val types: ArrayList<Types>
)

data class Sprites(
    val front_shiny: String
)

data class Ubicacion(
    val location_area: NameUrl
)

data class Evolucion(
    val chain: Chain
)
data class Chain(
    val evolves_to: ArrayList<EvolvesTo>
)
data class EvolvesTo(
    val species: NameUrl
)

data class Moves(
    val move: NameUrl
)

data class Types(
    val slot: Int,
    val type: NameUrl
)

data class Abilities(
    val ability: NameUrl,
    val is_hidden: Boolean,
    val slot: Int
)

data class NameUrl(
    val name: String,
    val url: String
)