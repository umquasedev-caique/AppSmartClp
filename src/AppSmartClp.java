import java.util.Scanner;

public class AppSmartClp {

    private static Scanner sc = new Scanner(System.in);
    private static PlcConnector connector;
    public static void main(String[] args) throws Exception {

        System.out.println("============================================");
        System.out.println("== Terminal de Comunicação CLP Siemens S7 ==");
        System.out.println("============================================");

        System.out.print("Digite o IP do CLP: ");
        String ip = sc.nextLine();
        int porta = 102; // Porta padrão S7

        connector = new PlcConnector(ip, porta);

        try{    
            System.out.println("Conectando ao CLP....");
            connector.connect();
            System.out.println("Conectado com Sucesso!");

            boolean sair = false;
            while (!sair) {
                exibirMenu();
                int opcao = Integer.parseInt(sc.nextLine());

                switch (opcao) {
                    case 1: processarLeitura(); break;
                    case 2: processarEscrita(); break;
                    case 0:
                        connector.disconnect();
                        sair = true;
                        break;
                
                    default:
                        System.out.println("Opção Inválida!");
                        break;
                }
            }

        } catch(Exception e){
            System.out.println("Erro Crítico: " + e.getMessage());
        }
        finally{
            System.out.println("Aplicação encerrada!");
        }
    }

    private static void exibirMenu(){
        System.out.println("\n --- Menu Principal ---");
        System.out.println("1 - Ler Variável");
        System.out.println("2 - Escrever Variável");
        System.out.println("0 - Sair e Desconectar");
        System.out.println("Escolha uma opção: ");
    }
    //10.74.241.10
    //10.74.241.40
    private static void processarLeitura(){
        try {
            System.out.print("DB Number: ");
            int db = Integer.parseInt(sc.nextLine());
            System.out.print("Offset (Start Address): ");
            int offset = Integer.parseInt(sc.nextLine());
            
            System.out.println("Tipo: 1-Bit, 2-Byte, 3-Int, 4-Float, 5-String, 6-Block");
            int tipo = Integer.parseInt(sc.nextLine());

            switch (tipo) {
                case 1:
                    System.out.print("Bit Number (0-7): ");
                    int bit = Integer.parseInt(sc.nextLine());
                    System.out.println("Resultado: " + connector.readBit(db, offset, bit));
                    break;
                case 2:
                    System.out.println("Resultado: " + connector.readByte(db, offset));
                    break;
                case 3:
                    System.out.println("Resultado: " + connector.readInt(db, offset));
                    break;
                case 4:
                    System.out.println("Resultado: " + connector.readFloat(db, offset));
                    break;
                case 5:
                    System.out.print("Tamanho da String: ");
                    int size = Integer.parseInt(sc.nextLine());
                    System.out.println("Resultado: " + connector.readString(db, offset, size));
                    break;
                case 6:
                    System.out.print("Tamanho do Bloco (bytes): ");
                    int bSize = Integer.parseInt(sc.nextLine());
                    byte[] data = connector.readBlock(db, offset, bSize);
                    System.out.println("Dados (Hex): " + bytesToHex(data));
                    break;
            }
        } catch (Exception e) {
            System.out.println("Erro na leitura: " + e.getMessage());
        }
    }

    private static void processarEscrita(){
        try {
            System.out.print("DB Number: ");
            int db = Integer.parseInt(sc.nextLine());
            System.out.print("Offset (Start Address): ");
            int offset = Integer.parseInt(sc.nextLine());
            
            System.out.println("Tipo: 1-Bit, 2-Byte, 3-Int, 4-Float, 5-String");
            int tipo = Integer.parseInt(sc.nextLine());

            System.out.print("Digite o valor para escrita: ");
            String valor = sc.nextLine();

            boolean sucesso = false;
            switch (tipo) {
                case 1:
                    System.out.print("Bit Number (0-7): ");
                    int bit = Integer.parseInt(sc.nextLine());
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
            System.out.println(sucesso ? "Escrita realizada!" : "Falha na escrita.");
        } catch (Exception e) {
            System.out.println("Erro na escrita: " + e.getMessage());
        }
    }
 //13 - 80
    private static String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
