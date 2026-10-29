
package br.edu.datacenter.sensor;

import br.edu.datacenter.proto.SensorData;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class PowerSensor {

    private static final String GATEWAY_HOST = "localhost";
    private static final int GATEWAY_UDP_PORT = 5000;

    private static final String SOURCE_ID = "pdu-rack-01-potencia";

    public static void main(String[] args) {

        DiscoveryAnnouncer.announce(
        SOURCE_ID,
        "POWER",
        0,
        "ACTIVE"
);

        Random random = new Random();
        double power = 1200.0;

        System.out.println(
                "[POWER] Sensor de potencia iniciado."
        );

        try (DatagramSocket socket = new DatagramSocket()) {

            InetAddress address =
                    InetAddress.getByName(GATEWAY_HOST);

            while (true) {

                power += (random.nextDouble() - 0.5) * 100.0;

                power = Math.max(
                        500.0,
                        Math.min(2500.0, power)
                );

                SensorData data = SensorData.newBuilder()
                        .setSourceId(SOURCE_ID)
                        .setSensorType("POWER")
                        .setValue(power)
                        .setUnit("W")
                        .setTimestamp(System.currentTimeMillis())
                        .build();

                byte[] bytes = data.toByteArray();

                DatagramPacket packet = new DatagramPacket(
                        bytes,
                        bytes.length,
                        address,
                        GATEWAY_UDP_PORT
                );

                socket.send(packet);

                System.out.printf(
                        "[POWER] Consumo enviado via Protobuf: %.2f W%n",
                        power
                );

                Thread.sleep(5000);
            }

        } catch (Exception e) {
            System.err.println(
                    "[POWER] Erro: " + e.getMessage()
            );
        }
    }
}
