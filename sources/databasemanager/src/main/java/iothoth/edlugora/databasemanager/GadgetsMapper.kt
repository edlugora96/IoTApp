package iothoth.edlugora.databasemanager

import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.emptyGadget

fun List<GadgetsEntity>.toGadgetDomainList() = map(GadgetsEntity::toGadgetDomain)


fun GadgetsEntity.toGadgetDomain() = emptyGadget().copy(
    id = id,
    unitId = unitId,
    name = name,
    icon = icon,
    actions = actions,
    values = values,
    ipAddress = ipAddress,
    ssid = ssid,
    ssidPassword = ssidPassword,
    wifiOwnership = wifiOwnership,
    location = location,
    coordinates = coordinates,
    setupInfo = setupInfo,
    type = type,
    status = status,
    showing = showing
)

fun Gadget.toGadgetEntity() = GadgetsEntity(
    id = id,
    unitId = unitId,
    name = name,
    icon = icon,
    actions = actions,
    values = values,
    ipAddress = ipAddress,
    ssid = ssid,
    ssidPassword = ssidPassword,
    wifiOwnership = wifiOwnership,
    location = location,
    coordinates = coordinates,
    setupInfo = setupInfo,
    type = type,
    status = status,
    showing = showing
)

