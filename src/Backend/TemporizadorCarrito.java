package Backend;

import java.util.Timer;
import java.util.TimerTask;

public class TemporizadorCarrito {


    public interface EventoTemporizador {
        void alActualizarTiempo(String tiempoFormateado, int segundosRestantes);
        void alFaltarUnMinuto();
        void alExpirar();
    }

    private int tiempoLimiteSegundos;
    private int segundosRestantes;
    private boolean activo;
    private boolean avisoUnMinutoEnviado;

    private Timer timer;
    private EventoTemporizador listener;

    public TemporizadorCarrito(EventoTemporizador listener) {
        this(15 * 60, listener);
    }

    public TemporizadorCarrito(int tiempoLimiteSegundos, EventoTemporizador listener) {
        this.tiempoLimiteSegundos = tiempoLimiteSegundos;
        this.segundosRestantes = tiempoLimiteSegundos;
        this.listener = listener;
        this.activo = false;
        this.avisoUnMinutoEnviado = false;
    }

    public synchronized void iniciarOReiniciar() {
        detener();

        this.segundosRestantes = this.tiempoLimiteSegundos;
        this.avisoUnMinutoEnviado = false;
        this.activo = true;


        if (listener != null) {
            listener.alActualizarTiempo(formatearTiempo(), segundosRestantes);
        }

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        }, 1000, 1000);
    }

    private synchronized void tick() {
        if (!activo) return;

        segundosRestantes--;

        if (listener != null) {
            listener.alActualizarTiempo(formatearTiempo(), segundosRestantes);
        }

        if (segundosRestantes == 20 && !avisoUnMinutoEnviado) {
            avisoUnMinutoEnviado = true;
            if (listener != null) {
                listener.alFaltarUnMinuto();
            }
        }

        if (segundosRestantes <= 0) {
            detener();
            if (listener != null) {
                listener.alExpirar();
            }
        }
    }


    public synchronized void detener() {
        this.activo = false;
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }


    public synchronized void setTiempoLimiteMinutos(int minutos) {
        if (minutos > 0) {
            this.tiempoLimiteSegundos = minutos * 60;
        }
    }


    public synchronized void setTiempoLimiteSegundos(int segundos) {
        if (segundos > 0) {
            this.tiempoLimiteSegundos = segundos;
        }
    }

    public boolean estaActivo() {
        return activo;
    }

    public String formatearTiempo() {
        int minutos = Math.max(0, segundosRestantes) / 60;
        int segundos = Math.max(0, segundosRestantes) % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
}