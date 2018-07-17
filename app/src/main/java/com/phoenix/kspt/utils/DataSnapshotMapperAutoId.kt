package com.phoenix.kspt.utils


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.kelvinapps.rxfirebase.RxFirebaseChildEvent
import com.kelvinapps.rxfirebase.exceptions.RxFirebaseDataCastException
import com.phoenix.kspt.models.ParentItem
import rx.exceptions.Exceptions
import rx.functions.Func1
import java.util.*

/**
 * Base generic class for firebase model (goggling)
 */
abstract class DataSnapshotMapperAutoId<T, U> private constructor() : Func1<T, U> {

    private class TypedDataSnapshotMapper<U>(private val clazz: Class<U>) : DataSnapshotMapperAutoId<DataSnapshot, U>() {

        override fun call(dataSnapshot: DataSnapshot): U? {
            return if (dataSnapshot.exists()) {
                getDataSnapshotTypedValue(dataSnapshot, clazz)
            } else {
                null
            }
        }
    }

    private class TypedListDataSnapshotMapper<U>(private val clazz: Class<U>) : DataSnapshotMapperAutoId<DataSnapshot, List<U>>() {

        override fun call(dataSnapshot: DataSnapshot): List<U> {
            val items = ArrayList<U>()
            for (childSnapshot in dataSnapshot.children) {
                val item = getDataSnapshotTypedValue(childSnapshot, clazz)
                if (item is ParentItem) {
                    if(childSnapshot.key != null)
                        (item as ParentItem).id = childSnapshot.key!!
                }
                items.add(item)
            }
            return items
        }
    }

    private class TypedMapDataSnapshotMapper<U>(private val clazz: Class<U>) : DataSnapshotMapperAutoId<DataSnapshot, LinkedHashMap<String, U>>() {

        override fun call(dataSnapshot: DataSnapshot): LinkedHashMap<String, U> {
            val items = LinkedHashMap<String, U>()
            for (childSnapshot in dataSnapshot.children) {
                val item = getDataSnapshotTypedValue(childSnapshot, clazz)
                if (item is ParentItem) {
                    (item as ParentItem).id = childSnapshot.key!!
                }
                if(dataSnapshot.key != null)
                    items[childSnapshot.key!!] = item
            }
            return items
        }
    }

    private class GenericTypedDataSnapshotMapper<U>(private val genericTypeIndicator: GenericTypeIndicator<U>) : DataSnapshotMapperAutoId<DataSnapshot, U>() {

        override fun call(dataSnapshot: DataSnapshot): U? {
            return if (dataSnapshot.exists()) {
                dataSnapshot.getValue(genericTypeIndicator)
                        ?: throw Exceptions.propagate(RxFirebaseDataCastException(
                                "unable to cast firebase data response to generic type"))
            } else {
                null
            }
        }
    }

    private class ChildEventDataSnapshotMapper<U>(private val clazz: Class<U>) : DataSnapshotMapperAutoId<RxFirebaseChildEvent<DataSnapshot>, RxFirebaseChildEvent<U>>() {

        override fun call(rxFirebaseChildEvent: RxFirebaseChildEvent<DataSnapshot>): RxFirebaseChildEvent<U> {
            val dataSnapshot = rxFirebaseChildEvent.value
            if (dataSnapshot.exists()) {
                val item = getDataSnapshotTypedValue(dataSnapshot, clazz)
                if (item is ParentItem) {
                    if(dataSnapshot.key != null)
                        (item as ParentItem).id = dataSnapshot.key!!
                }
                return RxFirebaseChildEvent(
                        dataSnapshot.key!!,
                        getDataSnapshotTypedValue(dataSnapshot, clazz),
                        rxFirebaseChildEvent.previousChildName,
                        rxFirebaseChildEvent.eventType)
            } else {
                throw Exceptions.propagate(RuntimeException("child dataSnapshot doesn't exist"))
            }
        }
    }

    companion object {

        fun <U> of(clazz: Class<U>): DataSnapshotMapperAutoId<DataSnapshot, U> {
            return TypedDataSnapshotMapper(clazz)
        }

        fun <U> listOf(clazz: Class<U>): DataSnapshotMapperAutoId<DataSnapshot, List<U>> {
            return TypedListDataSnapshotMapper(clazz)
        }

        fun <U> mapOf(clazz: Class<U>): DataSnapshotMapperAutoId<DataSnapshot, LinkedHashMap<String, U>> {
            return TypedMapDataSnapshotMapper(clazz)
        }

        fun <U> of(genericTypeIndicator: GenericTypeIndicator<U>): DataSnapshotMapperAutoId<DataSnapshot, U> {
            return GenericTypedDataSnapshotMapper(genericTypeIndicator)
        }

        fun <U> ofChildEvent(clazz: Class<U>): DataSnapshotMapperAutoId<RxFirebaseChildEvent<DataSnapshot>, RxFirebaseChildEvent<U>> {
            return ChildEventDataSnapshotMapper(clazz)
        }

        private fun <U> getDataSnapshotTypedValue(dataSnapshot: DataSnapshot, clazz: Class<U>): U {
            return dataSnapshot.getValue(clazz)
                    ?: throw Exceptions.propagate(RxFirebaseDataCastException(
                            "unable to cast firebase data response to " + clazz.simpleName))
        }
    }
}
