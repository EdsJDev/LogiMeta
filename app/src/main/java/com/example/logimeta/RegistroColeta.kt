
import java.io.Serializable

data class RegistroColeta(
//    val moduloSelecionado: String?, // Mantendo String, pois vem assim do Bundle
//    val totalEnderecos: Int?,
//    val totalItens: Int?,
//    val nomeSeparador: String?,
    val tempoColeta: String, // Para "mm:ss"
    val ruaEndereco: String,
    val qtdItensColetados: String,
    val produtoFoiEmbalado: Boolean?,
    val corteNoEndereco: Boolean?,
    val caixaFechada: Boolean?
 ): Serializable
