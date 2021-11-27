package iothoth.edlugora.com.database

import iothoth.edlugora.com.domain.Gadget

fun List<GadgetsEntity>.toGadgetDomainList() = map(GadgetsEntity::toGadgetDomain)

fun GadgetsEntity.toGadgetDomain() = Gadget(
    id,
    name,
    icon,
    actions,
    values,
    ipAddress,
    ssid,
    ssidPassword,
    wifiOwnership,
    location,
    coordinates,
    setupInfo,
    type
)

fun Gadget.toGadgetEntity() = GadgetsEntity(
    id,
    name,
    icon,
    actions,
    values,
    ipAddress,
    ssid,
    ssidPassword,
    wifiOwnership,
    location,
    coordinates,
    setupInfo,
    type
)