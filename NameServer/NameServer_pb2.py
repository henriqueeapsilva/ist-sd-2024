# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: NameServer.proto
# Protobuf Python Version: 4.25.1
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x10NameServer.proto\x12\x33pt.ulisboa.tecnico.tuplespaces.centralized.contract\"C\n\x0fregisterRequest\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x11\n\tqualifier\x18\x02 \x01(\t\x12\x0f\n\x07\x61\x64\x64ress\x18\x03 \x01(\t\"$\n\x10registerResponse\x12\x10\n\x08response\x18\x01 \x01(\t\"3\n\rlookupRequest\x12\x0f\n\x07service\x18\x01 \x01(\t\x12\x11\n\tqualifier\x18\x02 \x01(\t\"!\n\x0elookupResponse\x12\x0f\n\x07servers\x18\x01 \x03(\t\"5\n\rdeleteRequest\x12\x13\n\x0bservicename\x18\x01 \x01(\t\x12\x0f\n\x07\x61\x64\x64ress\x18\x02 \x01(\t\"\"\n\x0e\x64\x65leteResponse\x12\x10\n\x08response\x18\x01 \x01(\t2\xce\x03\n\nNameServer\x12\x97\x01\n\x08register\x12\x44.pt.ulisboa.tecnico.tuplespaces.centralized.contract.registerRequest\x1a\x45.pt.ulisboa.tecnico.tuplespaces.centralized.contract.registerResponse\x12\x91\x01\n\x06lookup\x12\x42.pt.ulisboa.tecnico.tuplespaces.centralized.contract.lookupRequest\x1a\x43.pt.ulisboa.tecnico.tuplespaces.centralized.contract.lookupResponse\x12\x91\x01\n\x06\x64\x65lete\x12\x42.pt.ulisboa.tecnico.tuplespaces.centralized.contract.deleteRequest\x1a\x43.pt.ulisboa.tecnico.tuplespaces.centralized.contract.deleteResponseb\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'NameServer_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:
  DESCRIPTOR._options = None
  _globals['_REGISTERREQUEST']._serialized_start=73
  _globals['_REGISTERREQUEST']._serialized_end=140
  _globals['_REGISTERRESPONSE']._serialized_start=142
  _globals['_REGISTERRESPONSE']._serialized_end=178
  _globals['_LOOKUPREQUEST']._serialized_start=180
  _globals['_LOOKUPREQUEST']._serialized_end=231
  _globals['_LOOKUPRESPONSE']._serialized_start=233
  _globals['_LOOKUPRESPONSE']._serialized_end=266
  _globals['_DELETEREQUEST']._serialized_start=268
  _globals['_DELETEREQUEST']._serialized_end=321
  _globals['_DELETERESPONSE']._serialized_start=323
  _globals['_DELETERESPONSE']._serialized_end=357
  _globals['_NAMESERVER']._serialized_start=360
  _globals['_NAMESERVER']._serialized_end=822
# @@protoc_insertion_point(module_scope)
