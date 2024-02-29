import sys
sys.path.insert(0, '../Contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
class NamingServerServiceImpl(pb2_grpc.NameServerServicer):

    def __init__(self, *args, **kwargs):
        pass

    def register(self, request, context):
        name

