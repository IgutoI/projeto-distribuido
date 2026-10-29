
package br.edu.datacenter.sensor;

import br.edu.datacenter.proto.SensorData;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class TemperatureSensor {

    private static final String GATEWAY_HOST = "localhost";
    private static final int GATEWAY_UDP_PORT = 5000;
    private static final String SOURCE_ID = "rack-01-temperatura";
    private static final double ALERT_THRESHOLD = 30.0;

    public static void main(String[] args) {

        DiscoveryAnnouncer.announce(
        SOURCE_ID,
        "TEMPERATURE",
        0,
        "ACTIVE"
);

        Random random = new Random();
        double temperature = 24.0;
        double previousTemperature = temperature;

        System.out.println("[SENSOR] Sensor de temperatura iniciado.");

        try (DatagramSocket socket = new DatagramSocket()) {

            InetAddress address = InetAddress.getByName(GATEWAY_HOST);

            while (true) {

                previousTemperature = temperature;

                temperature += (random.nextDouble() - 0.5) * 1.2;

                temperature = Math.max(
                        18.0,
                        Math.min(40.0, temperature)
                );

                long timestamp = System.currentTimeMillis();

                // Detecta a passagem do limite de baixo para cima.
                if (previousTemperature < ALERT_THRESHOLD
                        && temperature >= ALERT_THRESHOLD) {

                    SensorData alert = SensorData.newBuilder()
                            .setSourceId(SOURCE_ID)
                            .setSensorType("TEMPERATURE")
                            .setValue(temperature)
                            .setUnit("C")
                            .setTimestamp(timestamp)
                            .setIsAlert(true)
                            .setEventType("HIGH_TEMPERATURE")
                            .build();

                    byte[] alertBytes = alert.toByteArray();

                    DatagramPacket alertPacket = new DatagramPacket(
                            alertBytes,
                            alertBytes.length,
                            address,
                            GATEWAY_UDP_PORT
                    );

                    socket.send(alertPacket);

                    System.out.printf(
                            "[ALERTA] Evento enviado imediatamente: "
                                    + "%.2f °C (limite: %.2f °C)%n",
                            temperature,
                            ALERT_THRESHOLD
                    );
                }

                // Envio periódico normal, mantido a cada 3 segundos.
                SensorData data = SensorData.newBuilder()
                        .setSourceId(SOURCE_ID)
                        .setSensorType("TEMPERATURE")
                        .setValue(temperature)
                        .setUnit("C")
                        .setTimestamp(timestamp)
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
                        "[SENSOR] Enviado via Protobuf: %.2f °C%n",
                        temperature
                );

                Thread.sleep(3000);
            }

        } catch (Exception e) {
            System.err.println("[SENSOR] Erro: " + e.getMessage());
        }
    }
}
