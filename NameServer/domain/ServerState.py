class ServerEntry:

    def __init__(self, address, qualifier):
        self.address = address
        self.qualifier = qualifier


class ServiceEntry:

    def __init__(self, name, entries):
        self.serviceName = name
        self.serverEntries = entries


class NamingServer:

    def __init__(self, infomap):
        self.map = infomap
