package dam.pmdm.buscalapareja;

import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // --- VARIABLES DE ESTADO DEL JUEGO ---
    private int puntos;
    private int parejasEncontradas;
    private int primerIndice = -1;     // -1 indica que no hay ninguna carta seleccionada aún
    private boolean bloqueado = false; // Evita que el usuario pulse cartas mientras se oculta una pareja errónea

    // --- ESTRUCTURAS DE DATOS ---
    private int[] mapaImagenes;        // Array que guarda qué ID de imagen (R.drawable) tiene cada casilla
    private ImageButton[] botones;     // Referencias a los objetos visuales para cambiar su imagen
    private List<Integer> figuras;     // Lista dinámica para barajar las imágenes
    private TextView txtPuntos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtPuntos = findViewById(R.id.txtPuntos);
        findViewById(R.id.btnEmpezar).setOnClickListener(v -> empezarPartida());

        initTablero();    // Construimos la rejilla de juego una sola vez
        empezarPartida(); // Iniciamos la primera partida
    }

    // Crea los botones y prepara la lista de imágenes.
    private void initTablero() {
        // 1. Cargar las 20 figuras (mantenemos tu estructura original, es la más clara)
        figuras = new ArrayList<>();
        figuras.addAll(Arrays.asList(R.drawable.figura01, R.drawable.figura02, R.drawable.figura03, R.drawable.figura04, R.drawable.figura05));
        figuras.addAll(Arrays.asList(R.drawable.figura06, R.drawable.figura07, R.drawable.figura08, R.drawable.figura09, R.drawable.figura10));
        figuras.addAll(Arrays.asList(R.drawable.figura11, R.drawable.figura12, R.drawable.figura13, R.drawable.figura14, R.drawable.figura15));
        figuras.addAll(Arrays.asList(R.drawable.figura16, R.drawable.figura17, R.drawable.figura18, R.drawable.figura19, R.drawable.figura20));

        figuras.addAll(new ArrayList<>(figuras)); // Duplicamos para tener parejas (20x2)

        // 2. Inicializar arrays (Usamos nombres sencillos)
        mapaImagenes = new int[figuras.size()];
        botones = new ImageButton[figuras.size()];

        GridLayout grid = findViewById(R.id.grid);
        grid.setColumnCount(4); // Solo necesitamos definir columnas

        // 3. Un solo bucle para crear los 40 botones (Sin anidar filas y columnas)
        for (int i = 0; i < figuras.size(); i++) {
            ImageButton btn = new ImageButton(this);
            btn.setTag(i); // Guardamos la posición
            btn.setOnClickListener(v -> clickCasilla((int) v.getTag()));

            // Configuración visual mínima
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dpToPx(80);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Se reparte el ancho solo
            btn.setLayoutParams(params);

            botones[i] = btn;
            grid.addView(btn);
        }
    }

    private void clickCasilla(int indice) {
        // No hacemos nada en estos casos:
        //   - el tablero está bloqueado,
        //   - se pulsa la misma carta dos veces,
        //   - la carta ya está descubierta.
        if (bloqueado || indice == primerIndice || botones[indice].getDrawable() != null) return;

        // Volteamos la carta mostrando su imagen
        botones[indice].setImageResource(mapaImagenes[indice]);

        if (primerIndice == -1) {
            // CASO A: Es la primera carta que levanta
            primerIndice = indice;
        } else {
            // CASO B: Es la segunda carta, comparamos con la primera
            if (mapaImagenes[indice] == mapaImagenes[primerIndice]) {
                // --- ¡ACIERTO! ---
                puntos += calcularPuntos(primerIndice, indice);
                parejasEncontradas++;
                primerIndice = -1; // Reset para el próximo intento

                actualizarMarcador();
            } else {
                // --- ¡ERROR! ---
                bloqueado = true; // Bloqueamos clics para que el usuario vea el error
                int tmpPrimero = primerIndice; // Guardamos el índice actual antes de resetearlo

                // Retardo de 500ms para que dé tiempo a memorizar antes de ocultar
                new Handler().postDelayed(() -> {
                    botones[tmpPrimero].setImageResource(0); // oculta la imagen
                    botones[indice].setImageResource(0);
                    primerIndice = -1;
                    bloqueado = false; // Desbloqueamos el tablero
                }, 500);
            }
        }
    }

    private void empezarPartida() {
        Collections.shuffle(figuras); // Mezclamos las cartas aleatoriamente
        for (int i = 0; i < figuras.size(); i++) {
            mapaImagenes[i] = figuras.get(i); // Asignamos imagen barajada a la posición
            botones[i].setImageResource(0);   // Ocultamos todas las imágenes
            botones[i].setAlpha(1.0f);        // Restablecemos opacidad por si acaso
        }
        puntos = 0;
        parejasEncontradas = 0;
        primerIndice = -1;
        actualizarMarcador();
    }

    // Calcula puntos basándose en la distancia entre cartas.
    private int calcularPuntos(int p1, int p2) {
        int fil1 = p1 / 4, col1 = p1 % 4; // Carta 1
        int fil2 = p2 / 4, col2 = p2 % 4; // Carta 2

        // Fórmula de distancia: cuanto más lejos estén, más puntos
        return (Math.abs(fil1 - fil2) * 15) + (Math.abs(col1 - col2) * 10);
    }

    private void actualizarMarcador() {
        String mensaje;
        if (parejasEncontradas == figuras.size() / 2) {
            mensaje = "¡FELICIDADES!\nTotal: " + puntos + " puntos.";
        } else {
            mensaje = "Puntos: " + puntos;
        }
        txtPuntos.setText(mensaje);
    }

    // Convierte valores DP (Density Independent Pixels) a Píxeles reales del dispositivo.
    // Fundamental para que la UI se vea igual en todos los móviles.
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}