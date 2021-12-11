package iothoth.edlugora.databasemanager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.emptyGadget
import java.lang.Exception
import java.lang.NullPointerException

fun List<GadgetsEntity>.toGadgetDomainList() = map(GadgetsEntity::toGadgetDomain)


fun GadgetsEntity.toGadgetDomain() = MutableLiveData<Gadget>().emptyGadget().copy(
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
)

