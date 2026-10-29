
package br.edu.datacenter.sensor;

import br.edu.datacenter.proto.DiscoveryRequest;
import br.edu.datacenter.proto.SourceAnnouncement;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class DiscoveryAnnouncer {

    private static final String MULTICAST_GROUP = "230.0.0.1";

    // Porta usada para os anúncios das fontes.
    private static final int ANNOUNCEMENT_PORT = 4446;

    // Porta separada para as solicitações do Gateway.
    private static final int REQUEST_PORT = 4447;

    public static void announce(String sourceId, String sourceType) {
        announce(sourceId, sourceType, 0, "");
    }

    public static void announce(
            String sourceId,
            String sourceType,
            int grpcPort,
            String initialStatus) {

        sendAnnouncement(
                sourceId, sourceType, grpcPort, initialStatus);

        Thread listener = new Thread(
                () -> listenForRequests(
                        sourceId, sourceType, grpcPort, initialStatus),
                "discovery-listener-" + sourceId);

        listener.setDaemon(true);
        listener.start();
    }

    private static void sendAnnouncement(
            String sourceId,
            String sourceType,
            int grpcPort,
            String initialStatus) {

        try (MulticastSocket socket = new MulticastSocket()) {

            String localIp = InetAddress.getLocalHost().getHostAddress();

            SourceAnnouncement announcement =
                    SourceAnnouncement.newBuilder()
                            .setSourceId(sourceId)
                            .setSourceType(sourceType)
                            .setSourceIp(localIp)
                            .setGrpcPort(grpcPort)
                            .setInitialStatus(initialStatus)
                            .build();

            byte[] data = announcement.toByteArray();

            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);

            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    group,
                    ANNOUNCEMENT_PORT);

            socket.send(packet);

            System.out.println(
                    "[DESCOBERTA] Anúncio enviado: "
                            + sourceId + " | " + sourceType
                            + " | IP: " + localIp
                            + " | Porta gRPC: " + grpcPort);

        } catch (Exception e) {
            System.err.println(
                    "[DESCOBERTA] Erro ao anunciar fonte: "
                            + e.getMessage());
        }
    }

    private static void listenForRequests(
            String sourceId,
            String sourceType,
            int grpcPort,
            String initialStatus) {

        try (MulticastSocket socket = new MulticastSocket(null)) {

            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(REQUEST_PORT));

            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                byte[] data = java.util.Arrays.copyOfRange(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getOffset() + packet.getLength());

                try {
                    DiscoveryRequest request =
                            DiscoveryRequest.parseFrom(data);

                    if (!request.getRequesterId().isBlank()) {
                        System.out.println(
                                "[DESCOBERTA] Solicitação recebida: "
                                        + request.getRequesterId());

                        sendAnnouncement(
                                sourceId,
                                sourceType,
                                grpcPort,
                                initialStatus);
                    }

                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    System.err.println(
                            "[DESCOBERTA] Solicitação Protobuf inválida.");
                }
            }

        } catch (Exception e) {
            System.err.println(
                    "[DESCOBERTA] Erro ao escutar solicitações: "
                            + e.getMessage());
        }
    }
}
