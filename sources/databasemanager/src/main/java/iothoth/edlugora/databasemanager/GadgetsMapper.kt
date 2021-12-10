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
/*fun GadgetsEntity.toGadgetDomain(): Gadget {
    var res = MutableLiveData<Gadget>().emptyGadget()

    try {
     res = Gadget(
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
    } catch (ex : NullPointerException){
        Log.d("someOther", ex.message.toString())
    } catch (ex: Exception){
        Log.d("someOther", ex.message.toString())
    }

    return res


}*/

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

