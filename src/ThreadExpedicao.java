import javax.swing.SwingUtilities;
import javax.swing.JTextArea;

public class ThreadExpedicao extends Thread {

    private PlcConnector connector;
    private JTextArea area;
    private volatile boolean running = true;

    public ThreadExpedicao(PlcConnector connector, JTextArea area) {
        this.connector = connector;
        this.area = area;
    }

    @Override
    public void run() {

        while (running) {
            try {

                // 🔹 EXEMPLO - adapte para seus métodos reais
                int status = connector.readInt(2, 0);
                int pedidos = connector.readInt(2, 4);

                SwingUtilities.invokeLater(() -> {
                    area.setText(
                        "=== EXPEDIÇÃO ===\n" +
                        "Status: " + status + "\n" +
                        "Pedidos: " + pedidos
                    );
                });

                Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }
        }
    }

    public void parar() {
        running = false;
    }
}