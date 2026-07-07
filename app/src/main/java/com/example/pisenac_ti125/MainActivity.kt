package com.example.pisenac_ti125

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private data class Sala(
        val titulo: String,
        val pasta: String,
        val layout: Int
    )

    private data class Idioma(
        val codigo: String,
        val pastas: List<String>
    )

    private val salas = listOf(
        Sala("O Museu e o Edificio", "museu_e_edificio", R.layout.museu_e_edificio),
        Sala("Sala Dos Povos", "salas_povos", R.layout.salas_povos),
        Sala("Vestigios Arqueologicos", "vestigios_arqueologicos", R.layout.vestigios_arqueologicos),
        Sala("Fundacao e a Primeira Capela", "fundacao_primeira_capela", R.layout.fundacao_primeria_capela),
        Sala("Desenvolvimento Urbano", "desenvolvimento_urbano", R.layout.desenvolvimento_urbano),
        Sala("Manufaturas e Industrializacao", "manufatura_industrializacao", R.layout.manufatura_industrializacao),
        Sala("Cultura e Cotidiano", "cultura_cotidiano", R.layout.cultura_cotidiano),
        Sala("Religiosidade", "religiosidade", R.layout.religiosidade)
    )

    private val idiomas = mapOf(
        R.id.imageButton4 to Idioma("pt", listOf("pt", "portugues", "portuguese")),
        R.id.imageButton5 to Idioma("es", listOf("es", "espanhol", "spanish")),
        R.id.imageButton6 to Idioma("en", listOf("en", "ingles", "english")),
        R.id.imageButton7 to Idioma("it", listOf("it", "italiano", "italian")),
        R.id.imageButton8 to Idioma("ad", listOf("ad", "audio_descricao", "audiodescricao")),
        R.id.imageButton9 to Idioma("li", listOf("li", "libras"))
    )

    private var salaAtual = 0
    private var idiomaAtual = idiomas.getValue(R.id.imageButton4)
    private var textureView: TextureView? = null
    private var mediaPlayer: MediaPlayer? = null
    private var surface: Surface? = null
    private var videoAtual: File? = null
    private var playerPronto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        solicitarPermissao()
        abrirSala(salaAtual)

        // Imagem que vai Mudar
        val imgBandeira = findViewById<ImageView>(R.id.imageView2)

        // Botoes
        val bntBrasil = findViewById<ImageButton>(R.id.imageButton4)
        val bntEspanha = findViewById<ImageButton>(R.id.imageButton5)
        val bntEua = findViewById<ImageButton>(R.id.imageButton6)
        val bntItalia = findViewById<ImageButton>(R.id.imageButton7)
        val bntAudioVisual = findViewById<ImageButton>(R.id.imageButton8)
        val bntLibras = findViewById<ImageButton>(R.id.imageButton9)
        val bntSair = findViewById<ImageButton>(R.id.btnSair)

    }

    private fun abrirSala(indice: Int) {
        salaAtual = indice
        liberarPlayer()
        surface?.release()
        surface = null
        setContentView(salas[salaAtual].layout)
        textureView = findViewById(R.id.videoViewMuseu)
        configurarVideo()
        configurarToques()
        reproduzirVideoAtual()
    }

    private fun configurarVideo() {
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                surface?.release()
                surface = Surface(surfaceTexture)
                videoAtual?.let { iniciarPlayer(it) }
            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) = Unit

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                liberarPlayer()
                surface?.release()
                surface = null
                return true
            }
        }

        if (textureView?.isAvailable == true) {
            surface?.release()
            textureView?.surfaceTexture?.let { surfaceTexture ->
                surface = Surface(surfaceTexture)
            }
        }
    }

    private fun configurarToques() {
        findViewById<View>(R.id.view2).setOnClickListener { mostrarListaDeSalas() }
        findViewById<View>(R.id.view5).setOnClickListener { mostrarListaDeSalas() }

        findViewById<View>(R.id.imageView).setOnClickListener { mostrarCreditos() }

        findViewById<View>(R.id.textView3).setOnClickListener { mostrarListaDeSalas() }
        findViewById<View>(R.id.btnSair)?.setOnClickListener { toggleFullScreen(false) }

        idiomas.forEach { (botaoId, idioma) ->
            findViewById<ImageButton>(botaoId).setOnClickListener {
                if (idiomaAtual === idioma){
                    toggleFullScreen(true)

                    val imgBandeira = findViewById<ImageView>(R.id.imageView2)
                    imgBandeira.visibility = View.VISIBLE

                    when (botaoId) {
                        R.id.imageButton4 -> imgBandeira.setImageResource(R.drawable.brasil)
                        R.id.imageButton5 -> imgBandeira.setImageResource(R.drawable.espanha)
                        R.id.imageButton6 -> imgBandeira.setImageResource(R.drawable.eua)
                        R.id.imageButton7 -> imgBandeira.setImageResource(R.drawable.italia)
                        R.id.imageButton8 -> imgBandeira.setImageResource(R.drawable.audio_descricao)
                        R.id.imageButton9 -> imgBandeira.setImageResource(R.drawable.libras)

                    }
                    findViewById<ImageView>(R.id.btnSair).setOnClickListener {
                        findViewById<ImageView>(R.id.imageView2).visibility = View.INVISIBLE
                        toggleFullScreen(false)
                    }
                } else {
                    idiomaAtual = idioma
                    reproduzirVideoAtual()
                    toggleFullScreen(true)

                    val imgBandeira = findViewById<ImageView>(R.id.imageView2)
                    imgBandeira.visibility = View.VISIBLE

                    when (botaoId) {
                        R.id.imageButton4 -> imgBandeira.setImageResource(R.drawable.brasil)
                        R.id.imageButton5 -> imgBandeira.setImageResource(R.drawable.espanha)
                        R.id.imageButton6 -> imgBandeira.setImageResource(R.drawable.eua)
                        R.id.imageButton7 -> imgBandeira.setImageResource(R.drawable.italia)
                        R.id.imageButton8 -> imgBandeira.setImageResource(R.drawable.audio_descricao)
                        R.id.imageButton9 -> imgBandeira.setImageResource(R.drawable.libras)

                    }
                    findViewById<ImageView>(R.id.btnSair).setOnClickListener {
                        findViewById<ImageView>(R.id.imageView2).visibility = View.INVISIBLE
                        toggleFullScreen(false)
                    }
                }
            }
        }
    }

    private fun toggleFullScreen(fullScreen: Boolean) {
        val visibility = if (fullScreen) View.GONE else View.VISIBLE
        findViewById<View>(R.id.view2)?.visibility = visibility
        findViewById<View>(R.id.view5)?.visibility = visibility
        findViewById<View>(R.id.imageView)?.visibility = visibility
        findViewById<View>(R.id.textView3)?.visibility = visibility
        findViewById<View>(R.id.view)?.visibility = visibility

        idiomas.keys.forEach { id ->
            findViewById<View>(id)?.visibility = visibility
        }

        findViewById<View>(R.id.btnSair)?.visibility = if (fullScreen) View.VISIBLE else View.GONE

        val videoView = textureView ?: return
        val params = videoView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        if (fullScreen) {
            params.topToBottom = -1
            params.bottomToTop = -1
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID

            // Ocultar barras do sistema para tela cheia real
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        } else {
            params.topToBottom = R.id.view2
            params.bottomToTop = R.id.view
            params.topToTop = -1
            params.bottomToBottom = -1

            // Mostrar barras do sistema ao sair
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        videoView.layoutParams = params
    }

    private fun mostrarListaDeSalas() {
        val nomes = salas.map { it.titulo }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Escolha a sala")
            .setItems(nomes) { _, which -> abrirSala(which) }
            .show()
    }

    // NOVO: Função para exibir os créditos do projeto
    private fun mostrarCreditos() {
        AlertDialog.Builder(this)
            .setTitle("Criação e Desenvolvimento de interface interativa para os tablets")
            .setMessage(
                """
                -- Aplicativo Desenvolvido por: --
                Docente Técnico em Informatica 1.25
                Victor Christofoleti Mansolelli
                
                - Estudantes:
                Marlon Douglas Hermann
                Marcos Eduardo Mendes de Brito
                
                -- Layout Desenvolvido por: --
                Docente Técnico em Design Gráfico 1.25
                Keila Vieira de Carvalho

                - Estudantes:
                Beatriz Carolina Silva dos Santos
                Bryan Valle de Lima
                Carla Eduardo Ribas
                Eduardo Vechiato Castellini
                Enzo Gabriel Omodei de Sales
                Fernando Matos Baragão
                Julia Fabiano Saboia
                Raielli Gabriela Duarte da Rosa
                Ravi Martins Inocêncio
                Thiago Thome de Lima
                """.trimIndent()
            )
            .setPositiveButton("Fechar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun reproduzirVideoAtual() {
        val video = encontrarVideo(salas[salaAtual], idiomaAtual)

        if (video == null) {
            liberarPlayer()
            Toast.makeText(
                this,
                "Video nao encontrado: ${salas[salaAtual].pasta}/${idiomaAtual.codigo}",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        videoAtual = video
        iniciarPlayer(video)
    }

    private fun iniciarPlayer(video: File) {
        val superficie = surface ?: return
        liberarPlayer()
        playerPronto = false

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(video.absolutePath)
                setSurface(superficie)
                isLooping = true
                setOnPreparedListener { player ->
                    playerPronto = true
                    player.start()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@MainActivity, "Erro ao abrir: ${video.name}", Toast.LENGTH_LONG).show()
                    true
                }
                prepareAsync()
            }
        } catch (erro: Exception) {
            Toast.makeText(this, "Nao foi possivel abrir: ${video.name}", Toast.LENGTH_LONG).show()
            liberarPlayer()
        }
    }

    private fun encontrarVideo(sala: Sala, idioma: Idioma): File? {
        val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val bases = listOf(
            File(movies, "MuseuDeSalto"),
            File(movies, "Museu de Salto"),
            movies
        )

        val pastasDaSala = listOf(
            sala.pasta,
            "${sala.pasta}1",
            sala.pasta.replace("_", "")
        )

        bases.forEach { base ->
            pastasDaSala.forEach { pastaSala ->
                idioma.pastas.forEach { pastaIdioma ->
                    primeiroVideoEm(File(File(base, pastaSala), pastaIdioma))?.let { return it }
                }
            }
        }

        bases.forEach { base ->
            pastasDaSala.forEach { pastaSala ->
                idioma.pastas.forEach { pastaIdioma ->
                    primeiroVideoEm(File(base, "${pastaSala}_$pastaIdioma"))?.let { return it }
                    primeiroVideoEm(File(base, "${pastaSala}-$pastaIdioma"))?.let { return it }
                    videoComNome(File(base, pastaSala), pastaIdioma)?.let { return it }
                }
            }
        }

        return null
    }

    private fun primeiroVideoEm(pasta: File): File? {
        return pasta.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in EXTENSOES_VIDEO }
            ?.sortedBy { it.name.lowercase() }
            ?.firstOrNull()
    }

    private fun videoComNome(pasta: File, nome: String): File? {
        return EXTENSOES_VIDEO
            .map { File(pasta, "$nome.$it") }
            .firstOrNull { it.exists() && it.isFile }
    }

    private fun solicitarPermissao() {
        val permissao = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permissao) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permissao), REQUEST_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            reproduzirVideoAtual()
        }
    }

    override fun onResume() {
        super.onResume()
        if (playerPronto) mediaPlayer?.start()
    }

    override fun onPause() {
        if (playerPronto) mediaPlayer?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        liberarPlayer()
        surface?.release()
        surface = null
        super.onDestroy()
    }

    private fun liberarPlayer() {
        playerPronto = false
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val REQUEST_PERMISSION = 100
        private val EXTENSOES_VIDEO = setOf("mp4", "mkv", "avi", "mov", "webm")
    }
}