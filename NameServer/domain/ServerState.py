class ServerEntry:

    def __init__(self, address, qualifier):
        self.address = address
        self.qualifier = qualifier


class ServiceEntry:

    def __init__(self, name, entries=None):
        self.serviceName = name
        self.serverEntries = entries if entries is not None else []

    def add_server_entry(self, server_entry):
        self.serverEntries.append(server_entry)


class NamingServer:
    def __init__(self, infomap):
        self.map = infomap #if infomap is not None else {}

    def getMap(self):
        return self.map

    def add_service_entry(self, service_name, service_entry):
        self.map[service_name] = service_entry

    def remove_service_entry(self, service_name):
        if service_name in self.map:
            del self.map[service_name]

    def add_server_to_service(self, service_name, server_entry):
        if service_name in self.map:
            self.map[service_name].add_server_entry(server_entry)

    def remove_server_from_service(self, service_name, server_address):
        if service_name in self.map:
            self.map[service_name].serverEntries = [entry for entry in self.map[service_name].serverEntries if
                                                    entry.address != server_address]

    def get_service_entry(self, service_name):
        return self.map.get(service_name, None)

    def get_all_services(self):
        return list(self.map.keys())

    def get_all_servers(self):
        servers = []
        for service_entry in self.map.values():
            servers.extend([entry.address for entry in service_entry.serverEntries])
        return servers
