
package br.edu.datacenter.client;

import br.edu.datacenter.proto.AnalyticsQuery;
import br.edu.datacenter.proto.ClientCommand;
import br.edu.datacenter.proto.ClientReply;
import br.edu.datacenter.proto.ClientRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class DataCenterClient {

    private static final String HOST = "localhost";
    private static final int PORT = 5001;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("\n=== CLIENTE DO DATA CENTER ===");
                System.out.println("1 - Listar fontes e estados");
                System.out.println("2 - Media das medicoes");
                System.out.println("3 - Maior medicao");
                System.out.println("4 - Temperaturas acima de um limite");
                System.out.println("5 - Alterar temperatura da refrigeracao");
                System.out.println("6 - Consultar historico de medicoes");
                System.out.println("0 - Sair");
                System.out.print("Escolha: ");

                String option = scanner.nextLine().trim();

                if (option.equals("0")) {
                    System.out.println("Cliente encerrado.");
                    break;
                }

                ClientRequest request;

                switch (option) {
                    case "1" -> request = analytics("LIST_SOURCES");
                    case "2" -> request = analytics("AVERAGE");
                    case "3" -> request = analytics("MAX");

                    case "4" -> {
                        System.out.print("Limite de temperatura em C: ");
                        double threshold = readNumber(scanner);

                        request = ClientRequest.newBuilder()
                                .setClientCommand(ClientCommand.newBuilder()
                                        .setSourceId("rack-01-temperatura")
                                        .setCommand("ABOVE_THRESHOLD")
                                        .setValue(threshold)
                                        .build())
                                .build();
                    }

                    case "5" -> {
                        System.out.print(
                                "Nova temperatura desejada (16 a 30 C): ");
                        double temperature = readNumber(scanner);

                        request = ClientRequest.newBuilder()
                                .setClientCommand(ClientCommand.newBuilder()
                                        .setSourceId("cooling-system-01")
                                        .setCommand("SET_TEMPERATURE")
                                        .setValue(temperature)
                                        .build())
                                .build();
                    }

                    case "6" -> request = analytics("HISTORY");

                    default -> {
                        System.out.println("Opcao invalida.");
                        continue;
                    }
                }

                sendRequest(request);
            }

        } catch (Exception e) {
            System.err.println(
                    "Erro no cliente: " + e.getMessage());
        }
    }

    private static ClientRequest analytics(String type) {
        return ClientRequest.newBuilder()
                .setAnalyticsQuery(AnalyticsQuery.newBuilder()
                        .setQueryType(type)
                        .build())
                .build();
    }

    private static double readNumber(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim().replace(',', '.');

            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Digite um numero valido: ");
            }
        }
    }

    private static void sendRequest(ClientRequest request) {
        try (Socket socket = new Socket(HOST, PORT);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            request.writeDelimitedTo(out);
            out.flush();

            ClientReply reply = ClientReply.parseDelimitedFrom(in);

            if (reply == null) {
                System.out.println("O Gateway encerrou a conexao sem resposta.");
                return;
            }

            if (reply.hasAnalyticsResult()) {
                System.out.println("\n=== RESULTADO DA CONSULTA ===");
                System.out.println(
                        reply.getAnalyticsResult().getDescription());

                reply.getAnalyticsResult()
                        .getRowsList()
                        .forEach(System.out::println);

            } else if (reply.hasGatewayResponse()) {
                System.out.println("\n=== RESPOSTA DO GATEWAY ===");
                System.out.println(
                        reply.getGatewayResponse().getMessage());
                System.out.println(
                        "Sucesso: "
                                + reply.getGatewayResponse().getSuccess());

            } else {
                System.out.println("Resposta sem conteudo reconhecido.");
            }

        } catch (Exception e) {
            System.err.println(
                    "Nao foi possivel comunicar com o Gateway: "
                            + e.getMessage());
        }
    }
}
