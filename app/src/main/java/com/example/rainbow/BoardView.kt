package com.example.rainbow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class BoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) { //classe che rappresenta la vista del tabellone di gioco

    data class Brick(var x: Int, var y: Int, var color: Int) // classe che rappresenta singolo mattoncino

    enum class ColoriPrimari(val rgb: Int) { //enumerazione colori primari
        ROSSO(Color.RED),
        VERDE(Color.GREEN),
        BLU(Color.BLUE)
    }

    private val strokePaint = Paint().apply {  //pennello per tracciamento griglia
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 5F  // Spessore tracciamento
    }

    private val fallDelay = 250L // Ritardo per la caduta (ms)

    private var bricksList = mutableListOf<Brick>() //lista mattoncini mutabile (rimozione, aggiunta, mod)

    private val fallRunnable = object : Runnable { //{Runnable = interfaccia per definire un'azione che può essere eseguita da un thread}
        override fun run() { // chiamato appena si avvia il thread
            // Se il mattoncino è in movimento
            if (isBrickFalling) {
                brickY += gravitySpeed

                // Se il mattoncino raggiunge il fondo della griglia o tocca un altro mattoncino sotto
                if (brickY * cellHeight >= height - pixelsVerticale || isTouchingAnotherBrick()) {
                    // blocca il mattoncino sopra l'altro mattoncino o alla base della griglia
                    brickY = ((height - pixelsVerticale) / cellHeight).toInt() - 1
                    isBrickFalling = false // Imposta lo stato del mattoncino a bloccato
                    bricksList.add(Brick(brickX, brickY, currentBrickColor))
                    respawnBrick() // Genera un nuovo mattoncino
                }
            }

            invalidate() // aggiornamento della vista, con nuovo stato mattoncino
            postDelayed(this, fallDelay)
        }
    }

    private val gravitySpeed = 1 // Velocità caduta (da rivedere per grado di difficoltà)
    private var isBrickFalling = true // Variabile di stato per indicare se il mattoncino è in movimento
    private val percentualeDistanzaOrizzontale = 25
    private val percentualeDistanzaVerticale = 0
    private var brickX = (0..8).random()  // Posizione iniziale x del mattoncino
    private var brickY = 0 // Posizione iniziale y del mattoncino

    private val pixelsOrizzontale = TypedValue.applyDimension( //tutto da rivedere con simonci
        TypedValue.COMPLEX_UNIT_DIP,
        percentualeDistanzaOrizzontale.toFloat(),
        resources.displayMetrics
    )
    private val pixelsVerticale = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        percentualeDistanzaVerticale.toFloat(),
        resources.displayMetrics
    )
    private var cellWidth = 0f
    private var cellHeight = 0f
    private var currentBrickColor = Color.RED

    init { // Eseguito post creazione oggetto
        postDelayed(fallRunnable, fallDelay)
    }

    private fun respawnBrick() {// metodo per respawn nattoncino

        brickX = (0..8).random() // spawn in modo randomico in x (0,8 sono le celle della griglia)
        brickY = 0 // a 0 per farlo partire da y=0 ovvero il punto piu alto
        isBrickFalling = true
        currentBrickColor = ColoriPrimari.values().random().rgb //scelta randomica del colore dall'enumerazione
    }

    private fun isTouchingAnotherBrick(): Boolean { // metodo per controllare se il mattoncino tocca un'altro mattoncino

        val touchingBrickY = (brickY * cellHeight + cellHeight).toInt()
        return bricksList.any { it.x == brickX && it.y == touchingBrickY - 1 }
    }

    override fun onDraw(canvas: Canvas) {
        cellWidth = (width - 2 * pixelsOrizzontale) / 9f
        cellHeight = (height - 2 * pixelsVerticale) / 9f

        // Tracciamento bordo
        canvas.drawRect(
            pixelsOrizzontale,
            pixelsVerticale,
            (width - pixelsOrizzontale),
            (height - pixelsVerticale),
            strokePaint
        )

        // Disegna i mattoncini
        for (brick in bricksList) {
            drawBrick(canvas, brick.x, brick.y, brick.color)
        }

        // Disegna il mattoncino cadente
        if (isBrickFalling) {
            // Calcola la posizione della cella sotto il mattoncino cadente
            val nextBrickY = brickY + 1

            // Controlla se la cella sotto è occupata da un altro mattoncino
            val isNextBrickOccupied = bricksList.any { it.x == brickX && it.y == nextBrickY }

            // Se la cella sotto è occupata, ferma il mattoncino
            if (isNextBrickOccupied || nextBrickY * cellHeight >= height - pixelsVerticale) {
                isBrickFalling = false
                bricksList.add(Brick(brickX, brickY, currentBrickColor))
                respawnBrick()
            } else {
                drawBrick(canvas, brickX, brickY, currentBrickColor)
            }
        }

        // Ciclo per disegnare griglia 9x9
        for (i in 1 until 9) {
            val x = pixelsOrizzontale + i * cellWidth
            val y = pixelsVerticale + i * cellHeight

            // Disegna linee verticali
            canvas.drawLine(x, pixelsVerticale, x, height - pixelsVerticale, strokePaint)

            // Disegna linee orizzontali
            canvas.drawLine(pixelsOrizzontale, y, width - pixelsOrizzontale, y, strokePaint)
        }
    }


    private fun drawBrick(canvas: Canvas, x: Int, y: Int, color: Int) { //disegno mattoncino
        val cellWidth = (width - 2 * pixelsOrizzontale) / 9f
        val cellHeight = (height - 2 * pixelsVerticale) / 9f

        val brickPaint = Paint().apply {
            style = Paint.Style.FILL
            this.color = color
        }

        canvas.drawRect(
            pixelsOrizzontale + x * cellWidth,
            pixelsVerticale + y * cellHeight,
            pixelsOrizzontale + (x + 1) * cellWidth,
            pixelsVerticale + (y + 1) * cellHeight,
            brickPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean { //da cambiare, migliorare con scorrimento da sinistra a destra
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Imposta la posizione x del mattoncino sulla colonna toccata
                brickX = ((event.x - pixelsOrizzontale) / ((width - 2 * pixelsOrizzontale) / 9f)).toInt()
                invalidate()
            }
        }
        return true
    }
}
