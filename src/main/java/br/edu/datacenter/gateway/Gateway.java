
package br.edu.datacenter.gateway;



import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import br.edu.datacenter.proto.AnalyticsQuery;
import br.edu.datacenter.proto.AnalyticsResult;
import br.edu.datacenter.proto.ClientCommand;
import br.edu.datacenter.proto.ClientReply;
import br.edu.datacenter.proto.ClientRequest;
import br.edu.datacenter.proto.Empty;
import br.edu.datacenter.proto.GatewayResponse;
import br.edu.datacenter.proto.SetValueRequest;
import br.edu.datacenter.proto.SourceControlGrpc;
import br.edu.datacenter.proto.SourceState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Gateway {

        private static final int UDP_PORT = 5000;
        private static final int TCP_PORT = 5001;
        private static final int MULTICAST_PORT = 4446;
        private static final String MULTICAST_GROUP = "230.0.0.1";

        private final Map<String, Double> sourceValues = new ConcurrentHashMap<>();

        private final Map<String, String> sourceUnits = new ConcurrentHashMap<>();

        private final Map<String, String> sourceTypes = new ConcurrentHashMap<>();

        
private final Map<String, String> sourceIps = new ConcurrentHashMap<>();

private final Map<String, Integer> sourceGrpcPorts = new ConcurrentHashMap<>();

private final Map<String, String> sourceInitialStatuses = new ConcurrentHashMap<>();

// Último instante em que cada fonte enviou uma medição.
private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();

// Fontes cuja inatividade já foi registrada.
private final Map<String, Boolean> inactiveSources = new ConcurrentHashMap<>();

// Tempo máximo sem medições antes de considerar uma fonte inativa.
private static final long SENSOR_TIMEOUT_MS = 15_000;


        private final ConcurrentLinkedQueue<Measurement> history = new ConcurrentLinkedQueue<>();

        private record Measurement(
                        String sourceId,
                        double value,
                        String unit,
                        long timestamp) {
        }

        private static final Path CSV_PATH = Path.of("data", "medicoes.csv");

        private void saveMeasurement(Measurement measurement) {
                try {
                        Files.createDirectories(CSV_PATH.getParent());

                        boolean fileExists = Files.exists(CSV_PATH);

                        StringBuilder content = new StringBuilder();

                        if (!fileExists) {
                                content.append(
                                                "timestamp,source_id,source_type,value,unit\n");
                        }

                        String sourceType = sourceTypes.getOrDefault(
                                        measurement.sourceId(), "UNKNOWN");

                        content.append(String.format(
                                        java.util.Locale.US,
                                        "\"%s\",\"%s\",\"%s\",%.2f,\"%s\"%n",
                                        Instant.ofEpochMilli(measurement.timestamp()),
                                        measurement.sourceId().replace("\"", "\"\""),
                                        sourceType.replace("\"", "\"\""),
                                        measurement.value(),
                                        measurement.unit().replace("\"", "\"\"")));

                        Files.writeString(
                                        CSV_PATH,
                                        content.toString(),
                                        StandardCharsets.UTF_8,
                                        StandardOpenOption.CREATE,
                                        StandardOpenOption.APPEND);

                } catch (Exception e) {
                        System.err.println(
                                        "[GATEWAY] Erro ao salvar CSV: " + e.getMessage());
                }
        }

        
private List<Measurement> loadHistoryFromCsv() {
    List<Measurement> measurements = new ArrayList<>();

    if (!Files.exists(CSV_PATH)) {
        return measurements;
    }

    try (var reader = Files.newBufferedReader(
            CSV_PATH, StandardCharsets.UTF_8)) {

        String line = reader.readLine(); // Ignora o cabeçalho

        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            // O formato gravado pelo Gateway tem cinco colunas:
            // timestamp, source_id, source_type, value, unit
            String[] columns = line.split(
                    ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            if (columns.length != 5) {
                System.err.println(
                        "[GATEWAY] Linha inválida no CSV: " + line);
                continue;
            }

            try {
                String timestampText = columns[0]
                        .replace("\"", "").trim();

                String sourceId = columns[1]
                        .replace("\"", "").trim();

                double value = Double.parseDouble(
                        columns[3].trim());

                String unit = columns[4]
                        .replace("\"", "").trim();

                long timestamp = java.time.Instant
                        .parse(timestampText)
                        .toEpochMilli();

                measurements.add(new Measurement(
                        sourceId, value, unit, timestamp));

            } catch (Exception e) {
                System.err.println(
                        "[GATEWAY] Não foi possível ler uma linha do CSV: "
                                + line);
            }
        }

    } catch (Exception e) {
        System.err.println(
                "[GATEWAY] Erro ao ler histórico CSV: "
                        + e.getMessage());
    }

    return measurements;
}


        public void start() {
                System.out.println("================================");
                System.out.println("   GATEWAY DO DATA CENTER");
                System.out.println("================================");

                Thread discoveryThread = new Thread(
                                this::listenForDiscoveries, "multicast-discovery");
                discoveryThread.setDaemon(true);
                discoveryThread.start();

                Thread requestThread = new Thread(
                this::sendDiscoveryRequests,
                "multicast-discovery-request");

                requestThread.setDaemon(true);
                requestThread.start();

                Thread failureMonitorThread = new Thread(
                this::monitorSensorFailures,
                "sensor-failure-monitor");

                failureMonitorThread.setDaemon(true);
                failureMonitorThread.start();

                Thread tcpThread = new Thread(
                                this::startTcpServer, "tcp-client-server");
                tcpThread.setDaemon(true);
                tcpThread.start();

                System.out.println(
                                "Escutando sensores na porta UDP " + UDP_PORT);

                try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
                        byte[] buffer = new byte[1024];

                        while (true) {
                                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                                socket.receive(packet);

                                byte[] data = java.util.Arrays.copyOfRange(
                                                packet.getData(),
                                                packet.getOffset(),
                                                packet.getOffset() + packet.getLength());

                                processDatagram(data);
                        }
                } catch (Exception e) {
                        System.err.println(
                                        "[GATEWAY] Erro no recebimento UDP: "
                                                        + e.getMessage());
                }
        }

        private void listenForDiscoveries() {
                try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {

                        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);

                        socket.joinGroup(group);

                        System.out.println(
                                        "[GATEWAY] Descoberta multicast Protobuf ativa em "
                                                        + MULTICAST_GROUP + ":" + MULTICAST_PORT);

                        byte[] buffer = new byte[1024];

                        while (true) {
                                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                                socket.receive(packet);
                                System.out.println(
                                        "[GATEWAY] Pacote multicast recebido de "
                                        + packet.getAddress() + ":" + packet.getPort()
                                        + " | Bytes: " + packet.getLength()
                                );

                                byte[] data = java.util.Arrays.copyOfRange(
                                                packet.getData(),
                                                packet.getOffset(),
                                                packet.getOffset() + packet.getLength());

                                try {
                                        br.edu.datacenter.proto.SourceAnnouncement announcement = br.edu.datacenter.proto.SourceAnnouncement
                                                        .parseFrom(data);

                                                        System.out.println(
    "[DEBUG] Anúncio decodificado: "
    + announcement.getSourceId()
    + " | " + announcement.getSourceType()
);

                                        
if (!announcement.getSourceId().isBlank()
        && !announcement.getSourceType().isBlank()) {

    registerSource(announcement);
}


                                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                                        System.err.println(
                                                        "[GATEWAY] Anuncio multicast Protobuf invalido.");
                                }
                        }

                } catch (Exception e) {
                        System.err.println(
                                        "[GATEWAY] Erro na descoberta multicast: "
                                                        + e.getMessage());
                }
        }

        
private void processDatagram(byte[] bytes) {
    // Primeiro, tenta interpretar a mensagem como Protocol Buffers.
    try {
        br.edu.datacenter.proto.SensorData data =
                br.edu.datacenter.proto.SensorData.parseFrom(bytes);

        if (!data.getSourceId().isBlank()
                && !data.getUnit().isBlank()) {
            processSensorData(data);
            return;
        }

    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        // Se não for Protobuf válido, tenta o formato antigo.
    }

    // Compatibilidade temporária com o sensor de energia.
    String message = new String(
            bytes,
            java.nio.charset.StandardCharsets.UTF_8
    );

    processMessage(message);
}



private void registerSource(
        br.edu.datacenter.proto.SourceAnnouncement announcement) {

    String id = announcement.getSourceId();
    String type = announcement.getSourceType();

    String previous = sourceTypes.put(id, type);

    sourceIps.put(id, announcement.getSourceIp());
    sourceGrpcPorts.put(id, announcement.getGrpcPort());
    sourceInitialStatuses.put(id, announcement.getInitialStatus());

    if (previous == null) {
        System.out.println(
                "[GATEWAY] Nova fonte descoberta via Protobuf: " + id
                        + " | Tipo: " + type
                        + " | IP: " + announcement.getSourceIp()
                        + " | Porta gRPC: " + announcement.getGrpcPort()
                        + " | Estado inicial: "
                        + announcement.getInitialStatus());
    }
}




        private void processSensorData(
                        br.edu.datacenter.proto.SensorData data) {

                String id = data.getSourceId();
                double value = data.getValue();
                String unit = data.getUnit();

                if (data.getIsAlert()) {
                        System.out.printf(
                        "[ALERTA] Evento recebido: %s | Tipo: %s | "
                        + "Temperatura: %.2f %s%n",
                        data.getSourceId(),
                        data.getEventType(),
                        data.getValue(),
                        data.getUnit()
                );

                return;
}
                registerSensorActivity(id);

                sourceValues.put(id, value);
                sourceUnits.put(id, unit);
                sourceValues.put(id, value);
                sourceUnits.put(id, unit);

                sourceTypes.putIfAbsent(id, data.getSensorType());

                long timestamp = data.getTimestamp() > 0
                                ? data.getTimestamp()
                                : System.currentTimeMillis();

                Measurement measurement = new Measurement(id, value, unit, timestamp);

                history.add(measurement);
                saveMeasurement(measurement);

                System.out.printf(
                                "[GATEWAY] Protobuf recebido: %s = %.2f %s%n",
                                id, value, unit);

                
        }

        private void processMessage(String message) {
                try {
                        String[] parts = message.split(";");

                        if (parts.length != 3) {
                                System.err.println(
                                                "[GATEWAY] Mensagem invalida: " + message);
                                return;
                        }

                        String id = parts[0];
                        double value = Double.parseDouble(parts[1]);
                        String unit = parts[2];
                        
                        registerSensorActivity(id);

                        sourceValues.put(id, value);
                        sourceUnits.put(id, unit);
                        sourceValues.put(id, value);
                        sourceUnits.put(id, unit);

                        Measurement measurement = new Measurement(
                                        id, value, unit, System.currentTimeMillis());

                        history.add(measurement);
                        saveMeasurement(measurement);

                        System.out.printf(
                                        "[GATEWAY] Recebido: %s = %.2f %s%n",
                                        id, value, unit);

                        if (unit.equals("C") && value >= 30.0) {
                                System.out.println(
                                                "[ALERTA] Temperatura elevada no rack: " + id);
                        }
                } catch (Exception e) {
                        System.err.println(
                                        "[GATEWAY] Nao foi possivel processar: " + message);
                }
        }

        private void startTcpServer() {
                try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
                        System.out.println(
                                        "[GATEWAY] Servidor TCP ativo na porta " + TCP_PORT);

                        while (true) {
                                Socket client = serverSocket.accept();

                                Thread clientThread = new Thread(
                                                () -> handleClient(client));
                                clientThread.setDaemon(true);
                                clientThread.start();
                        }
                } catch (Exception e) {
                        System.err.println(
                                        "[GATEWAY] Erro no servidor TCP: " + e.getMessage());
                }
        }

        private void handleClient(Socket socket) {
                try (socket;
                                InputStream in = socket.getInputStream();
                                OutputStream out = socket.getOutputStream()) {

                        while (true) {
                                ClientRequest request = ClientRequest.parseDelimitedFrom(in);

                                if (request == null) {
                                        break;
                                }

                                ClientReply reply = processRequest(request);
                                reply.writeDelimitedTo(out);
                                out.flush();
                        }
                } catch (Exception e) {
                        System.err.println(
                                        "[GATEWAY] Erro ao atender cliente TCP: "
                                                        + e.getMessage());
                }
        }

        private ClientReply processRequest(ClientRequest request) {
                if (request.hasAnalyticsQuery()) {
                        AnalyticsQuery query = request.getAnalyticsQuery();
                        String type = query.getQueryType()
                                        .trim().toUpperCase(Locale.ROOT);

                        return ClientReply.newBuilder()
                                        .setAnalyticsResult(runAnalytics(type, 0))
                                        .build();
                }

                if (request.hasClientCommand()) {
                        ClientCommand command = request.getClientCommand();
                        String type = command.getCommand()
                                        .trim().toUpperCase(Locale.ROOT);

                        if (type.equals("ABOVE_THRESHOLD")) {
                                return ClientReply.newBuilder()
                                                .setAnalyticsResult(
                                                                runAnalytics(type, command.getValue()))
                                                .build();
                        }

                        if (type.equals("SET_TEMPERATURE")) {
                                return ClientReply.newBuilder()
                                                .setGatewayResponse(
                                                                setCoolingTemperature(command.getValue()))
                                                .build();
                        }

                        return ClientReply.newBuilder()
                                        .setGatewayResponse(GatewayResponse.newBuilder()
                                                        .setSuccess(false)
                                                        .setMessage("Comando desconhecido: " + type)
                                                        .build())
                                        .build();
                }

                return ClientReply.newBuilder()
                                .setGatewayResponse(GatewayResponse.newBuilder()
                                                .setSuccess(false)
                                                .setMessage("Requisicao sem conteudo.")
                                                .build())
                                .build();
        }

        private AnalyticsResult runAnalytics(String type, double threshold) {
                List<Measurement> measurements = new ArrayList<>(history);
                List<String> rows = new ArrayList<>();
                String description;

                switch (type) {

                        
case "HISTORY" -> {
    List<Measurement> savedMeasurements =
            loadHistoryFromCsv();

    savedMeasurements.sort(
            Comparator.comparingLong(Measurement::timestamp)
                    .reversed());

    description = "Historico de medicoes persistidas no CSV.";

    int limit = Math.min(savedMeasurements.size(), 100);

    for (int i = 0; i < limit; i++) {
        Measurement m = savedMeasurements.get(i);

        rows.add(
                java.time.Instant
                        .ofEpochMilli(m.timestamp())
                        + " | Fonte: " + m.sourceId()
                        + " | Valor: "
                        + String.format(Locale.US, "%.2f", m.value())
                        + " " + m.unit());
    }
}



                        case "LIST_SOURCES" -> {
    description = "Fontes descobertas e ultimos valores.";

    sourceTypes.forEach((id, sourceType) -> {

        if (sourceType.equalsIgnoreCase("REFRIGERATION")) {
            SourceState state = getCoolingState();

            if (state != null) {
                rows.add(
                        id
                        + " | Tipo: " + sourceType
                        + " | Estado: " + state.getStatus()
                        + " | Ultimo valor: "
                        + String.format(
                                Locale.US,
                                "%.2f %s",
                                state.getValue(),
                                state.getUnit())
                );
            } else {
                rows.add(
                        id
                        + " | Tipo: " + sourceType
                        + " | Estado indisponivel via gRPC"
                );
            }

        } else {
            Double value = sourceValues.get(id);
            String unit = sourceUnits.getOrDefault(id, "");

            rows.add(
                    id + " | Tipo: " + sourceType
                    + " | Ultimo valor: "
                    + (value == null
                            ? "aguardando medicao"
                            : String.format(
                                    Locale.US,
                                    "%.2f %s",
                                    value,
                                    unit))
            );
        }
    });
}

                        case "AVERAGE" -> {
                                description = "Media das medicoes por fonte.";

                                Map<String, List<Measurement>> grouped = measurements.stream().collect(
                                                Collectors.groupingBy(Measurement::sourceId));

                                grouped.forEach((id, list) -> {
                                        double average = list.stream()
                                                        .mapToDouble(Measurement::value)
                                                        .average().orElse(0);

                                        String unit = list.get(0).unit();

                                        rows.add(String.format(
                                                        "%s | Media: %.2f %s | Amostras: %d",
                                                        id, average, unit, list.size()));
                                });
                        }

                        case "MAX" -> {
                                description = "Maior medicao registrada por fonte.";

                                Map<String, List<Measurement>> grouped = measurements.stream().collect(
                                                Collectors.groupingBy(Measurement::sourceId));

                                grouped.forEach((id, list) -> {
                                        Measurement maximum = list.stream()
                                                        .max(Comparator.comparingDouble(
                                                                        Measurement::value))
                                                        .orElseThrow();

                                        rows.add(String.format(
                                                        "%s | Maximo: %.2f %s",
                                                        id, maximum.value(), maximum.unit()));
                                });
                        }

                        case "ABOVE_THRESHOLD" -> {
                                description = String.format(
                                                "Medicoes de temperatura acima de %.2f C.",
                                                threshold);

                                measurements.stream()
                                                .filter(m -> m.unit().equals("C"))
                                                .filter(m -> m.value() > threshold)
                                                .sorted(Comparator.comparingLong(
                                                                Measurement::timestamp).reversed())
                                                .limit(100)
                                                .forEach(m -> rows.add(String.format(
                                                                "%s | %.2f C", m.sourceId(), m.value())));
                        }

                        default -> description = "Consulta desconhecida: " + type;
                }

                if (rows.isEmpty()) {
                        rows.add("Nenhum resultado disponivel.");
                }

                return AnalyticsResult.newBuilder()
                                .setDescription(description)
                                .addAllRows(rows)
                                .build();
        }

        private GatewayResponse setCoolingTemperature(double temperature) {
                ManagedChannel channel = ManagedChannelBuilder
                                .forAddress("localhost", 50051)
                                .usePlaintext()
                                .build();

                try {
                        SetValueRequest request = SetValueRequest.newBuilder()
                                        .setValue(temperature)
                                        .build();

                        return SourceControlGrpc.newBlockingStub(channel)
                                        .setValue(request);
                } catch (Exception e) {
                        return GatewayResponse.newBuilder()
                                        .setSuccess(false)
                                        .setMessage(
                                                        "Falha ao controlar a refrigeracao: "
                                                                        + e.getMessage())
                                        .build();
                } finally {
                        channel.shutdown();
                }
        }


        private SourceState getCoolingState() {
    ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 50051)
            .usePlaintext()
            .build();

    try {
        return SourceControlGrpc.newBlockingStub(channel)
                .getState(Empty.getDefaultInstance());

    } catch (Exception e) {
        System.err.println(
                "[GATEWAY] Erro ao consultar refrigeracao: "
                        + e.getMessage());
        return null;

    } finally {
        channel.shutdown();
    }
}

        public void listSources() {
                AnalyticsResult result = runAnalytics("LIST_SOURCES", 0);
                System.out.println("\n=== FONTES REGISTRADAS ===");
                result.getRowsList().forEach(System.out::println);
        }

        public static void main(String[] args) {
                new Gateway().start();
        }



        private void monitorSensorFailures() {
    while (true) {
        try {
            long now = System.currentTimeMillis();

            for (Map.Entry<String, String> entry : sourceTypes.entrySet()) {
                String sourceId = entry.getKey();
                String sourceType = entry.getValue();

                // A refrigeração é controlada por gRPC e não envia
                // medições periódicas via UDP neste projeto.
                if ("REFRIGERATION".equalsIgnoreCase(sourceType)) {
                    continue;
                }

                Long lastTime = lastSeen.get(sourceId);

                // Ainda não houve nenhuma medição dessa fonte.
                if (lastTime == null) {
                    continue;
                }

                long elapsed = now - lastTime;

                if (elapsed >= SENSOR_TIMEOUT_MS) {
                    Boolean wasInactive = inactiveSources.put(
                            sourceId, true);

                    if (!Boolean.TRUE.equals(wasInactive)) {
                        System.err.printf(
                                "[ALERTA] Sensor %s ficou INATIVO. "
                                + "Sem medições há %.1f segundos.%n",
                                sourceId,
                                elapsed / 1000.0);
                    }
                }
            }

            Thread.sleep(3000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(
                    "[GATEWAY] Monitor de falhas encerrado.");
            return;

        } catch (Exception e) {
            System.err.println(
                    "[GATEWAY] Erro no monitor de falhas: "
                    + e.getMessage());
        }
    }
}

 private void registerSensorActivity(String sourceId) {
    lastSeen.put(sourceId, System.currentTimeMillis());

    Boolean wasInactive = inactiveSources.remove(sourceId);

    if (Boolean.TRUE.equals(wasInactive)) {
        System.out.println(
                "[RECUPERAÇÃO] Sensor " + sourceId
                + " voltou a enviar medições.");
    }
}
        
        
private void sendDiscoveryRequests() {
    try (DatagramSocket socket = new DatagramSocket()) {

        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);

        while (true) {
            br.edu.datacenter.proto.DiscoveryRequest request =
                    br.edu.datacenter.proto.DiscoveryRequest.newBuilder()
                            .setRequesterId("datacenter-gateway")
                            .build();

            byte[] data = request.toByteArray();

            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    group,
                    4447);

            socket.send(packet);

            System.out.println(
                    "[GATEWAY] Solicitação multicast de descoberta enviada.");

            Thread.sleep(5000);
        }

    } catch (Exception e) {
        System.err.println(
                "[GATEWAY] Erro ao solicitar descoberta: "
                        + e.getMessage());
    }
}

}
