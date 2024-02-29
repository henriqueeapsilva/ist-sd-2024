import sys
import grpc
from concurrent import futures
sys.path.insert(0, '../Contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpcs

# define the port
PORT = 5001

if __name__ == '__main__':
    try:
        # print received arguments
        print("Received arguments:")
        for i in range(1, len(sys.argv)):
            print("  " + sys.argv[i])

        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
        server.add_insecure_port('[::]:'+ PORT)
        server.start()
        print('')
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("HelloServer stopped")
        exit(0)

