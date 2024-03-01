import sys

import grpc

from NameServer_pb2_grpc import NameServerServicer
from NameServer_pb2 import registerResponse
from NameServer_pb2 import lookupResponse
from NameServer_pb2 import deleteResponse
from domain.ServerState import ServerEntry
from domain.ServerState import NamingServer


class NamingServerServiceImpl(NameServerServicer):

    def __init__(self, infomap=None):
        self.naming_server = NamingServer(infomap)

    def register(self, request, context):
        name = request.name
        qualifier = request.qualifier
        address = request.address
        print("name: ", name)
        print("qualifier: ", qualifier)
        print("address: ", address)
        try:
            # Add the server to the service entry in the naming server
            self.naming_server.add_server_to_service(name, ServerEntry(address, qualifier))
            print("teste")
            return registerResponse()
        except Exception as e:
            # Unable to register the server
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details("Not possible to register the server: {}".format(str(e)))
            raise grpc.RpcError(grpc.StatusCode.INTERNAL, "Not possible to register the server")


    def lookup(self, request, context):
        print("lookup")
        service = request.service
        qualifier = request.qualifier if hasattr(request, "A") else None

        if service not in self.naming_server.map:
            print("vazio")
            return lookupResponse(servers=[])

        service_entry = self.naming_server.map[service]
        if qualifier is None:
            servers = [entry.address for entry in service_entry.serverEntries]
        else:
            servers = [entry.address for entry in service_entry.serverEntries if entry.qualifier == qualifier]

        return lookupResponse(servers=servers)




    def delete(self, request, context):
        service_name = request.servicename
        server_address = request.address

        if service_name not in self.naming_server.map:
            # Service does not exist
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Service does not exist.")
            return deleteResponse()

        service_entry = self.naming_server.map[service_name]
        if server_address not in [entry.address for entry in service_entry.serverEntries]:
            # Server not found in the service
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Server not found in the service.")
            return deleteResponse()

        # Try to remove the server from the service entry
        try:
            self.naming_server.remove_server_from_service(service_name, server_address)
            # Return an empty response indicating successful deletion
            return deleteResponse()
        except Exception as e:
            # Unable to remove the server
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details("Not possible to remove the server: {}".format(str(e)))
            raise grpc.RpcError(grpc.StatusCode.INTERNAL, "Not possible to remove the server")



