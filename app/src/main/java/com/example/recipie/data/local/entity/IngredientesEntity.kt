package com.example.recipie.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredientes",
    indices = [
        Index(value = ["descricao"], name = "index_ingredientes_descricao")
    ]
)
data class IngredientesEntity(

    @PrimaryKey
    val codigo: String,

    val classe: String,
    val descricao: String,

    val energia_kj: Double?,
    val energia_kcal: Double?,

    val umidade_g: Double?,
    val carboidrato_total_g: Double?,
    val carboidrato_disponivel_g: Double?,
    val proteina_g: Double?,
    val lipidios_g: Double?,
    val fibra_alimentar_g: Double?,
    val alcool_g: Double?,
    val cinzas_g: Double?,

    val colesterol_mg: Double?,
    val acidos_graxos_saturados_g: Double?,
    val acidos_graxos_monoinsaturados_g: Double?,
    val acidos_graxos_poliinsaturados_g: Double?,
    val acidos_graxos_trans_g: Double?,

    val calcio_mg: Double?,
    val ferro_mg: Double?,
    val sodio_mg: Double?,
    val magnesio_mg: Double?,
    val fosforo_mg: Double?,
    val potassio_mg: Double?,
    val zinco_mg: Double?,
    val cobre_mg: Double?,
    val selenio_mcg: Double?,

    val vitamina_a_re_mcg: Double?,
    val vitamina_a_rae_mcg: Double?,
    val vitamina_d_mcg: Double?,
    val alfa_tocoferol_vitamina_e_mg: Double?,
    val tiamina_mg: Double?,
    val riboflavina_mg: Double?,
    val niacina_mg: Double?,
    val vitamina_b6_mg: Double?,
    val vitamina_b12_mcg: Double?,
    val vitamina_c_mg: Double?,
    val equivalente_de_folato_mcg: Double?,

    val sal_de_adicao_g: Double?,
    val acucar_de_adicao_g: Double?
)

