package br.edu.datacenter.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Serviço gRPC usado pelo Gateway para controlar fontes.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.1)",
    comments = "Source: datacenter.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class SourceControlGrpc {

  private SourceControlGrpc() {}

  public static final java.lang.String SERVICE_NAME = "datacenter.SourceControl";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<br.edu.datacenter.proto.Empty,
      br.edu.datacenter.proto.SourceState> getGetStateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetState",
      requestType = br.edu.datacenter.proto.Empty.class,
      responseType = br.edu.datacenter.proto.SourceState.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<br.edu.datacenter.proto.Empty,
      br.edu.datacenter.proto.SourceState> getGetStateMethod() {
    io.grpc.MethodDescriptor<br.edu.datacenter.proto.Empty, br.edu.datacenter.proto.SourceState> getGetStateMethod;
    if ((getGetStateMethod = SourceControlGrpc.getGetStateMethod) == null) {
      synchronized (SourceControlGrpc.class) {
        if ((getGetStateMethod = SourceControlGrpc.getGetStateMethod) == null) {
          SourceControlGrpc.getGetStateMethod = getGetStateMethod =
              io.grpc.MethodDescriptor.<br.edu.datacenter.proto.Empty, br.edu.datacenter.proto.SourceState>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetState"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.edu.datacenter.proto.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.edu.datacenter.proto.SourceState.getDefaultInstance()))
              .setSchemaDescriptor(new SourceControlMethodDescriptorSupplier("GetState"))
              .build();
        }
      }
    }
    return getGetStateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<br.edu.datacenter.proto.SetValueRequest,
      br.edu.datacenter.proto.GatewayResponse> getSetValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetValue",
      requestType = br.edu.datacenter.proto.SetValueRequest.class,
      responseType = br.edu.datacenter.proto.GatewayResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<br.edu.datacenter.proto.SetValueRequest,
      br.edu.datacenter.proto.GatewayResponse> getSetValueMethod() {
    io.grpc.MethodDescriptor<br.edu.datacenter.proto.SetValueRequest, br.edu.datacenter.proto.GatewayResponse> getSetValueMethod;
    if ((getSetValueMethod = SourceControlGrpc.getSetValueMethod) == null) {
      synchronized (SourceControlGrpc.class) {
        if ((getSetValueMethod = SourceControlGrpc.getSetValueMethod) == null) {
          SourceControlGrpc.getSetValueMethod = getSetValueMethod =
              io.grpc.MethodDescriptor.<br.edu.datacenter.proto.SetValueRequest, br.edu.datacenter.proto.GatewayResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.edu.datacenter.proto.SetValueRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  br.edu.datacenter.proto.GatewayResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SourceControlMethodDescriptorSupplier("SetValue"))
              .build();
        }
      }
    }
    return getSetValueMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SourceControlStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SourceControlStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SourceControlStub>() {
        @java.lang.Override
        public SourceControlStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SourceControlStub(channel, callOptions);
        }
      };
    return SourceControlStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SourceControlBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SourceControlBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SourceControlBlockingStub>() {
        @java.lang.Override
        public SourceControlBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SourceControlBlockingStub(channel, callOptions);
        }
      };
    return SourceControlBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SourceControlFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SourceControlFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SourceControlFutureStub>() {
        @java.lang.Override
        public SourceControlFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SourceControlFutureStub(channel, callOptions);
        }
      };
    return SourceControlFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Serviço gRPC usado pelo Gateway para controlar fontes.
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void getState(br.edu.datacenter.proto.Empty request,
        io.grpc.stub.StreamObserver<br.edu.datacenter.proto.SourceState> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetStateMethod(), responseObserver);
    }

    /**
     */
    default void setValue(br.edu.datacenter.proto.SetValueRequest request,
        io.grpc.stub.StreamObserver<br.edu.datacenter.proto.GatewayResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetValueMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service SourceControl.
   * <pre>
   * Serviço gRPC usado pelo Gateway para controlar fontes.
   * </pre>
   */
  public static abstract class SourceControlImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return SourceControlGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service SourceControl.
   * <pre>
   * Serviço gRPC usado pelo Gateway para controlar fontes.
   * </pre>
   */
  public static final class SourceControlStub
      extends io.grpc.stub.AbstractAsyncStub<SourceControlStub> {
    private SourceControlStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SourceControlStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SourceControlStub(channel, callOptions);
    }

    /**
     */
    public void getState(br.edu.datacenter.proto.Empty request,
        io.grpc.stub.StreamObserver<br.edu.datacenter.proto.SourceState> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetStateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setValue(br.edu.datacenter.proto.SetValueRequest request,
        io.grpc.stub.StreamObserver<br.edu.datacenter.proto.GatewayResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetValueMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service SourceControl.
   * <pre>
   * Serviço gRPC usado pelo Gateway para controlar fontes.
   * </pre>
   */
  public static final class SourceControlBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<SourceControlBlockingStub> {
    private SourceControlBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SourceControlBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SourceControlBlockingStub(channel, callOptions);
    }

    /**
     */
    public br.edu.datacenter.proto.SourceState getState(br.edu.datacenter.proto.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetStateMethod(), getCallOptions(), request);
    }

    /**
     */
    public br.edu.datacenter.proto.GatewayResponse setValue(br.edu.datacenter.proto.SetValueRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetValueMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service SourceControl.
   * <pre>
   * Serviço gRPC usado pelo Gateway para controlar fontes.
   * </pre>
   */
  public static final class SourceControlFutureStub
      extends io.grpc.stub.AbstractFutureStub<SourceControlFutureStub> {
    private SourceControlFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SourceControlFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SourceControlFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<br.edu.datacenter.proto.SourceState> getState(
        br.edu.datacenter.proto.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetStateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<br.edu.datacenter.proto.GatewayResponse> setValue(
        br.edu.datacenter.proto.SetValueRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetValueMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_STATE = 0;
  private static final int METHODID_SET_VALUE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_STATE:
          serviceImpl.getState((br.edu.datacenter.proto.Empty) request,
              (io.grpc.stub.StreamObserver<br.edu.datacenter.proto.SourceState>) responseObserver);
          break;
        case METHODID_SET_VALUE:
          serviceImpl.setValue((br.edu.datacenter.proto.SetValueRequest) request,
              (io.grpc.stub.StreamObserver<br.edu.datacenter.proto.GatewayResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetStateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              br.edu.datacenter.proto.Empty,
              br.edu.datacenter.proto.SourceState>(
                service, METHODID_GET_STATE)))
        .addMethod(
          getSetValueMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              br.edu.datacenter.proto.SetValueRequest,
              br.edu.datacenter.proto.GatewayResponse>(
                service, METHODID_SET_VALUE)))
        .build();
  }

  private static abstract class SourceControlBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SourceControlBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return br.edu.datacenter.proto.DataCenterProtocol.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SourceControl");
    }
  }

  private static final class SourceControlFileDescriptorSupplier
      extends SourceControlBaseDescriptorSupplier {
    SourceControlFileDescriptorSupplier() {}
  }

  private static final class SourceControlMethodDescriptorSupplier
      extends SourceControlBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    SourceControlMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (SourceControlGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SourceControlFileDescriptorSupplier())
              .addMethod(getGetStateMethod())
              .addMethod(getSetValueMethod())
              .build();
        }
      }
    }
    return result;
  }
}
