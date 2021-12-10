package iothoth.edlugora.databasemanager

import iothoth.edlugora.domain.Gadget

fun List<GadgetsEntity>.toGadgetDomainList() = map(GadgetsEntity::toGadgetDomain)


fun GadgetsEntity.toGadgetDomain() = Gadget(
    id = id,
    unitId= unitId,
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
)

fun Gadget.toGadgetEntity() = GadgetsEntity(
    id = id,
    unitId= unitId,
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
)

