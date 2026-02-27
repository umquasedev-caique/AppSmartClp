import javax.swing.SwingUtilities;
import javax.swing.JTextArea;

public class ThreadEstoque extends Thread {

    private PlcConnector connector;
    private JTextArea area;
    private volatile boolean running = true;

    public ThreadEstoque(PlcConnector connector, JTextArea area) {
        this.connector = connector;
        this.area = area;
    }

    @Override
    public void run() {

        while (running) {
            try {

                // 🔹 EXEMPLO - adapte para seus métodos reais
                int quantidade = connector.readInt(1, 0);
                float peso = connector.readFloat(1, 4);

                SwingUtilities.invokeLater(() -> {
                    area.setText(
                        "=== ESTOQUE ===\n" +
                        "Quantidade: " + quantidade + "\n" +
                        "Peso: " + peso
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