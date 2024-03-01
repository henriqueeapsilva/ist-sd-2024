import sys
import grpc
from concurrent import futures
from NamingServerServiceImpl import NamingServerServiceImpl
import NameServer_pb2 as pb2
from NameServer_pb2_grpc import add_NameServerServicer_to_server

# define the port
PORT = 5001

if __name__ == '__main__':
    try:
        # print received arguments
        print("Received arguments:")
        for i in range(1, len(sys.argv)):
            print("  " + sys.argv[i])
        infoMap = {}
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
        add_NameServerServicer_to_server(NamingServerServiceImpl(infoMap), server)
        server.add_insecure_port('[::]:'+ str(PORT))
        server.start()

        server.wait_for_termination()

    except KeyboardInterrupt:
        print("HelloServer stopped")
        exit(0)