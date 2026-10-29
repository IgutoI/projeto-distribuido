
package br.edu.datacenter.gateway;

import br.edu.datacenter.proto.Empty;
import br.edu.datacenter.proto.GatewayResponse;
import br.edu.datacenter.proto.SetValueRequest;
import br.edu.datacenter.proto.SourceControlGrpc;
import br.edu.datacenter.proto.SourceState;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CoolingClient implements AutoCloseable {

    private final ManagedChannel channel;
    private final SourceControlGrpc.SourceControlBlockingStub stub;

    public CoolingClient(String host, int port) {
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        stub = SourceControlGrpc.newBlockingStub(channel);
    }

    public void showState() {
        SourceState state = stub.getState(Empty.newBuilder().build());

        System.out.println("\n=== ESTADO DA REFRIGERACAO ===");
        System.out.println("Fonte: " + state.getSourceId());
        System.out.println("Tipo: " + state.getSourceType());
        System.out.println("Estado: " + state.getStatus());
        System.out.println(
                "Temperatura desejada: " + state.getValue()
                + " " + state.getUnit()
        );
    }

    public void setTemperature(double temperature) {
        SetValueRequest request = SetValueRequest.newBuilder()
                .setValue(temperature)
                .build();

        GatewayResponse response = stub.setValue(request);

        System.out.println("[GATEWAY] " + response.getMessage());
    }

    @Override
    public void close() {
    channel.shutdown();
}

    public static void main(String[] args) {
        double temperature = 20.0;

        if (args.length > 0) {
            try {
                temperature = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Informe uma temperatura numerica valida.");
                return;
            }
        }

        try (CoolingClient client = new CoolingClient("localhost", 50051)) {
            client.showState();
            client.setTemperature(temperature);
            client.showState();
        } catch (Exception e) {
            System.err.println(
                    "[GATEWAY] Falha na comunicacao gRPC: " + e.getMessage()
            );
        }
    }
}
