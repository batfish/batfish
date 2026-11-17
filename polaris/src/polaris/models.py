"""Validated data models for network security intent."""

from enum import StrEnum
from ipaddress import IPv4Network, IPv6Network
from typing import Annotated

from pydantic import BaseModel, ConfigDict, Field, IPvAnyNetwork, field_validator, model_validator


class Protocol(StrEnum):
    ANY = "ANY"
    TCP = "TCP"
    UDP = "UDP"
    ICMP = "ICMP"
    ICMPV6 = "ICMPV6"


class Expectation(StrEnum):
    MUST_ALLOW = "MUST_ALLOW"
    MUST_DENY = "MUST_DENY"


Port = Annotated[int, Field(ge=1, le=65535)]


class StrictModel(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)


class NetworkMetadata(StrictModel):
    name: str = Field(min_length=1, max_length=100)


class CommunicationRequirement(StrictModel):
    id: str = Field(min_length=1, max_length=100)
    source: str = Field(min_length=1)
    destination: str = Field(min_length=1)
    protocol: Protocol
    ports: list[Port] | None = None
    expectation: Expectation

    @field_validator("ports")
    @classmethod
    def validate_unique_ports(cls, value: list[int] | None) -> list[int] | None:
        if value is not None and len(value) != len(set(value)):
            raise ValueError("ports must not contain duplicates")
        return value

    @model_validator(mode="after")
    def validate_protocol_ports(self) -> "CommunicationRequirement":
        if self.ports and self.protocol not in {Protocol.TCP, Protocol.UDP}:
            raise ValueError("ports are supported only with TCP or UDP in the initial intent model")
        return self


class NetworkIntent(StrictModel):
    network: NetworkMetadata
    zones: dict[str, IPvAnyNetwork] = Field(min_length=1)
    requirements: list[CommunicationRequirement] = Field(min_length=1)

    @field_validator("zones")
    @classmethod
    def validate_zone_names(cls, value: dict[str, IPvAnyNetwork]) -> dict[str, IPvAnyNetwork]:
        normalized: dict[str, IPvAnyNetwork] = {}
        for name, subnet in value.items():
            clean_name = name.strip()
            if not clean_name:
                raise ValueError("zone names must not be empty")
            if clean_name in normalized:
                raise ValueError(f"duplicate zone name: {clean_name}")
            normalized[clean_name] = subnet
        return normalized

    @model_validator(mode="after")
    def validate_references_and_ids(self) -> "NetworkIntent":
        zone_names = set(self.zones)
        seen_ids: set[str] = set()

        for requirement in self.requirements:
            if requirement.id in seen_ids:
                raise ValueError(f"duplicate requirement id: {requirement.id}")
            seen_ids.add(requirement.id)

            if requirement.source not in zone_names:
                raise ValueError(
                    f"requirement {requirement.id} references unknown source zone "
                    f"{requirement.source!r}"
                )
            if requirement.destination not in zone_names:
                raise ValueError(
                    f"requirement {requirement.id} references unknown destination zone "
                    f"{requirement.destination!r}"
                )

        return self
