
import java.io.Serializable

data class RegistroColeta(
    val tempoColeta: String, // Para "mm:ss"
    val ruaEndereco: String,
    val qtdItensColetados: String,
    val produtoFoiEmbalado: Boolean?,
    val corteNoEndereco: Boolean?,
    val caixaFechada: Boolean?
 ): Serializable
