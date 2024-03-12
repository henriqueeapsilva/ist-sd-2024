import grpc
from concurrent import futures
from NamingServerServiceImpl import NamingServerServiceImpl
from NameServer_pb2_grpc import add_NameServerServicer_to_server

# define the port
PORT = 5001

if __name__ == '__main__':
    try:
        infoMap = {}
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=3))
        add_NameServerServicer_to_server(NamingServerServiceImpl(infoMap), server)
        server.add_insecure_port('[::]:' + str(PORT))
        server.start()

        server.wait_for_termination()

    except KeyboardInterrupt:
        print("HelloServer stopped")
        exit(0)
