
package br.edu.datacenter.sensor;

import br.edu.datacenter.proto.Empty;
import br.edu.datacenter.proto.GatewayResponse;
import br.edu.datacenter.proto.SetValueRequest;
import br.edu.datacenter.proto.SourceControlGrpc;
import br.edu.datacenter.proto.SourceState;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class CoolingSystem extends SourceControlGrpc.SourceControlImplBase {

    private static final int GRPC_PORT = 50051;

    // Estado inicial simulado do sistema de refrigeração.
    private volatile double targetTemperature = 22.0;
    private volatile boolean active = true;

    @Override
    public void getState(
            Empty request,
            StreamObserver<SourceState> responseObserver) {

        SourceState state = SourceState.newBuilder()
                .setSourceId("cooling-system-01")
                .setSourceType("REFRIGERATION")
                .setStatus(active ? "ON" : "OFF")
                .setValue(targetTemperature)
                .setUnit("C")
                .build();

        responseObserver.onNext(state);
        responseObserver.onCompleted();
    }

    @Override
    public void setValue(
            SetValueRequest request,
            StreamObserver<GatewayResponse> responseObserver) {

        double newTemperature = request.getValue();

        if (newTemperature < 16.0 || newTemperature > 30.0) {
            GatewayResponse response = GatewayResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Temperatura desejada deve ficar entre 16 e 30 C.")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        targetTemperature = newTemperature;
        active = true;

        System.out.printf(
                "[COOLING] Temperatura desejada alterada para %.1f C%n",
                targetTemperature
        );

        GatewayResponse response = GatewayResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Temperatura desejada atualizada.")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    
public static void main(String[] args) throws Exception {

    CoolingSystem coolingSystem = new CoolingSystem();

    Server server = ServerBuilder
            .forPort(GRPC_PORT)
            .addService(coolingSystem)
            .build()
            .start();

    System.out.println("[COOLING] Sistema de refrigeracao iniciado.");
    System.out.println("[COOLING] Servico gRPC na porta " + GRPC_PORT);
    System.out.println("[COOLING] Temperatura desejada inicial: 22.0 C");

    DiscoveryAnnouncer.announce(
            "cooling-system-01",
            "REFRIGERATION",
            GRPC_PORT,
            "ON"
    );

    server.awaitTermination();
}

}
