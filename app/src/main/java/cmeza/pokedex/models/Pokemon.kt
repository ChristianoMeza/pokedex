package cmeza.pokedex.models

data class Pokemon(
    val results: ArrayList<PokemonResult>
)

data class PokemonResult(
    val name: String,
    val url : String,
    val imgUrl : String
)

data class ResultDetail(
    val abilities: ArrayList<Abilities>,
    val id: Int,
    val height: Int,
    val location_area_encounters: String,
    val moves: ArrayList<Moves>,
    val types: ArrayList<Types>,
    val species: NameUrl,
    val chain: Chain?
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
    val types: ArrayList<NameUrl>
)

data class Abilities(
    val ability: Ability,
    val is_hidden: Boolean,
    val slot: Int
)

data class Ability(
    val name: String,
    val url: String
)

data class NameUrl(
    val name: String,
    val url: String
)