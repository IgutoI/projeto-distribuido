
package br.edu.datacenter.gateway;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class MulticastDiscovery {

    private static final int PORT = 4446;
    private static final String GROUP = "230.0.0.1";

    public void start() {
        InetAddress group = null;

        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            group = InetAddress.getByName(GROUP);
            socket.joinGroup(group);

            System.out.println("================================");
            System.out.println(" DESCOBERTA MULTICAST DO GATEWAY");
            System.out.println("================================");
            System.out.println("Grupo: " + GROUP);
            System.out.println("Porta: " + PORT);
            System.out.println("Aguardando anúncios das fontes...");

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet =
                    new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                String message = new String(
                    packet.getData(),
                    packet.getOffset(),
                    packet.getLength(),
                    StandardCharsets.UTF_8
                );

                System.out.println(
                    "[DESCOBERTA] Fonte encontrada: " + message
                );
            }

        } catch (Exception e) {
            System.err.println(
                "[DESCOBERTA] Erro: " + e.getMessage()
            );
        } finally {
            // O socket é fechado automaticamente pelo try-with-resources.
        }
    }

    public static void main(String[] args) {
        new MulticastDiscovery().start();
    }
}
