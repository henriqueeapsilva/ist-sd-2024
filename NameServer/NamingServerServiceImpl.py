import sys
from NameServer_pb2_grpc import NameServerServicer
from NameServer_pb2 import registerResponse
class NamingServerServiceImpl(NameServerServicer):

    def __init__(self, *args, **kwargs):
        pass

    def register(self, request, context):
        name = request.name
        qualifier = request.qualifier
        address = request.address

        response = registerResponse(register="")
        return response

