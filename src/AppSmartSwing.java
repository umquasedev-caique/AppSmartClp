import java.awt.*;
import javax.swing.*;

public class AppSmartSwing extends JFrame {
    static PlcConnector connector;
    private Thread threadEstoque;
    private Thread threadExpedicao;
    private volatile boolean running = false;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new AppSmartSwing().setVisible(true));
    }

    private static JTextField ipField;
    private JTextField dbField;
    private JTextField startField;
    private JTextField tipoField;
    private JTextField valorField;
    private JTextArea resultadoArea; // Para mostrar resultado na tela

    public AppSmartSwing() {
        initComponents();
    }

    private void initComponents() {

        setTitle("Interface de Comunicação com CLP");
        setSize(800, 600); // Frame maior
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ===== Painel Superior =====
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createTitledBorder("Configurações CLP"));
        topPanel.setLayout(new GridLayout(5, 2, 10, 10));
        topPanel.setPreferredSize(new Dimension(800, 200));

        ipField = new JTextField();
        dbField = new JTextField();
        startField = new JTextField();
        tipoField = new JTextField();
        valorField = new JTextField();
        resultadoArea = new JTextArea();
        resultadoArea.setEditable(false);
        resultadoArea.setBorder(BorderFactory.createTitledBorder("Resultado"));

        add(new JScrollPane(resultadoArea), BorderLayout.EAST);

        topPanel.add(new JLabel("IP:"));
        topPanel.add(ipField);

        topPanel.add(new JLabel("DB:"));
        topPanel.add(dbField);

        topPanel.add(new JLabel("Start Position:"));
        topPanel.add(startField);

        topPanel.add(new JLabel("Tipo:"));
        topPanel.add(tipoField);

        topPanel.add(new JLabel("Valor:"));
        topPanel.add(valorField);

        add(topPanel, BorderLayout.NORTH);

        // ===== Painel Central (para futuros painéis) =====
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2, 10, 10));

        JPanel leftPanel = new JPanel();
        leftPanel.setBorder(BorderFactory.createTitledBorder("Estoque"));

        JPanel rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createTitledBorder("Expedição"));

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        add(centerPanel, BorderLayout.CENTER);

        // ===== Painel Inferior (Botões) =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Botões"));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));

        JButton leitura = new JButton("Leitura");
        JButton escrita = new JButton("Escrita");
        JButton conectarBtn = new JButton("Conectar");
        JButton desconectarBtn = new JButton("Desconectar");

        leitura.setPreferredSize(new Dimension(120, 35));
        escrita.setPreferredSize(new Dimension(120, 35));
        conectarBtn.setPreferredSize(new Dimension(120, 35));
        desconectarBtn.setPreferredSize(new Dimension(120, 35));

        bottomPanel.add(leitura);
        leitura.addActionListener(e -> {
            processarLeitura();
        });

        bottomPanel.add(conectarBtn);
        conectarBtn.addActionListener(e -> {
            conectarPLC();
        });

        bottomPanel.add(desconectarBtn);
        desconectarBtn.addActionListener(e -> {
            desconectarPLC();
        });

        bottomPanel.add(escrita);
        escrita.addActionListener(e -> {
            processarEscrita();
        });

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void conectarPLC() {
        try {
            String ip = ipField.getText();
            int porta = 102;

            if (ip.endsWith(".10")) {
                iniciarThreadEstoque();
            } else if (ip.endsWith(".40")) {
                iniciarThreadExpedicao();
            }

            connector = new PlcConnector(ip, porta);
            connector.connect();

            resultadoArea.setText("Conectado com sucesso ao PLC!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao conectar: " + e.getMessage(),
                    "Erro de Conexão",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private void iniciarThreadExpedicao() {
        running = true;
        threadExpedicao = new Thread(() -> {

        while (running) {
            try {

                int status = connector.readInt(2, 0);

                SwingUtilities.invokeLater(() -> {
                    System.out.println("Expedição Atualizada");
                });

                Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }
        }
    });
    threadExpedicao.start();
    }

    private void iniciarThreadEstoque() {
        running = true;

        threadEstoque = new Thread(() -> {

            while (running) {
                try {

                    // EXEMPLO - adapte para seus métodos reais
                    int valor1 = connector.readInt(1, 0);
                    float valor2 = connector.readFloat(1, 4);

                    SwingUtilities.invokeLater(() -> {
                        // Atualizar painel Estoque
                        System.out.println("Estoque Atualizado");
                    });

                    Thread.sleep(1000); // Atualiza a cada 1 segundo

                } catch (Exception e) {
                    e.printStackTrace();
                    running = false;
                }
            }
        });

        threadEstoque.start();
    }

    private void processarLeitura() {
        try {
            int db = Integer.parseInt(dbField.getText());
            int offset = Integer.parseInt(startField.getText());
            // System.out.println("Tipo: 1-Bit, 2-Byte, 3-Int, 4-Float, 5-String");
            int tipo = Integer.parseInt(tipoField.getText());

            switch (tipo) {
                case 1:
                    String bitStr = JOptionPane.showInputDialog(this, "Bit Number (0-7):");
                    int bit = Integer.parseInt(bitStr);
                    boolean bitResult = connector.readBit(db, offset, bit);
                    resultadoArea.setText("Resultado: " + bitResult);
                    break;

                case 2:
                    byte byteResult = connector.readByte(db, offset);
                    resultadoArea.setText("Resultado: " + byteResult);
                    break;

                case 3:
                    int intResult = connector.readInt(db, offset);
                    resultadoArea.setText("Resultado: " + intResult);
                    break;

                case 4:
                    float floatResult = connector.readFloat(db, offset);
                    resultadoArea.setText("Resultado: " + floatResult);
                    break;

                case 5:
                    String sizeStr = JOptionPane.showInputDialog(this, "Tamanho da String:");
                    int size = Integer.parseInt(sizeStr);
                    String strResult = connector.readString(db, offset, size);
                    resultadoArea.setText("Resultado: " + strResult);
                    break;

                case 6:
                    String bSizeStr = JOptionPane.showInputDialog(this, "Tamanho do Bloco:");
                    int bSize = Integer.parseInt(bSizeStr);
                    byte[] data = connector.readBlock(db, offset, bSize);
                    resultadoArea.setText("Dados (Hex): " + bytesToHex(data));
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "Tipo inválido!");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro na leitura: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processarEscrita() {

        try {
            int db = Integer.parseInt(dbField.getText());
            int offset = Integer.parseInt(startField.getText());
            int tipo = Integer.parseInt(tipoField.getText());
            String valor = valorField.getText();

            boolean sucesso = false;

            switch (tipo) {
                case 1:
                    String bitStr = JOptionPane.showInputDialog(this, "Bit Number (0-7):");
                    int bit = Integer.parseInt(bitStr);
                    sucesso = connector.writeBit(db, offset, bit, Boolean.parseBoolean(valor));
                    break;

                case 2:
                    sucesso = connector.writeByte(db, offset, Byte.parseByte(valor));
                    break;

                case 3:
                    sucesso = connector.writeInt(db, offset, Integer.parseInt(valor));
                    break;

                case 4:
                    sucesso = connector.writeFloat(db, offset, Float.parseFloat(valor));
                    break;

                case 5:
                    sucesso = connector.writeString(db, offset, valor.length(), valor);
                    break;
            }

            resultadoArea.setText(
                    sucesso ? "Escrita realizada com sucesso!" : "Falha na escrita.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro na escrita: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desconectarPLC() {
    running = false;

    try {
        connector.disconnect();
    } catch (Exception e) {
        e.printStackTrace();
    }

    resultadoArea.setText("Desconectado.");
    dispose(); // Fecha a tela
    System.exit(0); //Para todas as funcionalidades.
}

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

}
