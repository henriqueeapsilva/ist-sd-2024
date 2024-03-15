import grpc

from NameServer_pb2_grpc import NameServerServicer
import NameServer_pb2 as pb2
from domain.ServerState import ServerEntry, ServiceEntry, NamingServer


class NamingServerServiceImpl(NameServerServicer):

    def __init__(self, infomap=None):
        self.naming_server = NamingServer(infomap)

    def register(self, request, context):
        name = request.name
        qualifier = request.qualifier
        address = request.address
        try:
            if name not in self.naming_server.map:
                service_entry = ServiceEntry(name)
                self.naming_server.add_service_entry(name, service_entry)

            self.naming_server.add_server_to_service(name, ServerEntry(address, qualifier))

            return pb2.registerResponse(response="")
        except Exception as e:
            # Unable to register the server
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details("Not possible to register the server: {}".format(str(e)))
            raise grpc.RpcError(grpc.StatusCode.INTERNAL, "Not possible to register the server")

    def lookup(self, request, context):
        service = request.service
        qualifier = request.qualifier

        if service not in self.naming_server.map:
            return pb2.lookupResponse(servers=[])

        service_entry = self.naming_server.map[service]
        if qualifier is None or qualifier == "":
            servers = sorted(service_entry.serverEntries, key=lambda x: x.qualifier)
            servers = [entry.address for entry in servers]
        else:
            servers = [entry.address for entry in service_entry.serverEntries if entry.qualifier == qualifier]

        return pb2.lookupResponse(servers=servers)

    def delete(self, request, context):
        service_name = request.servicename
        server_address = request.address

        if service_name not in self.naming_server.map:
            # Service does not exist
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Service does not exist.")
            return

        service_entry = self.naming_server.map[service_name]
        if server_address not in [entry.address for entry in service_entry.serverEntries]:
            # Server not found in the service
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Server not found in the service.")
            return

        # Try to remove the server from the service entry
        try:
            self.naming_server.remove_server_from_service(service_name, server_address)
            # Return an empty response indicating successful deletion
            return pb2.deleteResponse(response="")
        except Exception as e:
            # Unable to remove the server
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details("Not possible to remove the server: {}".format(str(e)))
            raise grpc.RpcError(grpc.StatusCode.INTERNAL, "Not possible to remove the server")
