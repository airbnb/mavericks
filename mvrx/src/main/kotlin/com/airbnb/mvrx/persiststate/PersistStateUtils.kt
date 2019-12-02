@file:SuppressWarnings("Detekt.StringLiteralDuplication")

package com.airbnb.mvrx.persiststate

import com.airbnb.mvrx.StatePersistor

fun statePersistorOrNull(): StatePersistor? {
    return try {
        Class.forName("com.airbnb.mvrx.persiststate.StatePersistorImpl")
    } catch (e: ClassNotFoundException) {
        null
    }?.kotlin?.objectInstance as? StatePersistor
}