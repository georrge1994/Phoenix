package com.phoenix.kspt.firebase

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kelvinapps.rxfirebase.RxFirebaseDatabase
import com.phoenix.kspt.BuildConfig
import com.phoenix.kspt.utils.DataSnapshotMapperAutoId
import com.phoenix.kspt.models.User
import com.phoenix.kspt.ovt.models.Stage
import com.phoenix.kspt.ovt.models.StudentResults
import com.phoenix.kspt.ovt.models.SystemVars
import com.phoenix.kspt.utils.DEVELOP
import rx.Observable
import java.util.*
import com.phoenix.kspt.utils.STUDENT


// first level
private const val USERS: String = "users"
private const val SYSTEM_VARS: String = "systemVars"

private const val OVT: String = "ovt"
private const val THEME_ONE: String = "theme_one"
private const val PERCENT_OVT_1: String = "percentOvt1"
private const val COUNT_ATTEMPTS: String = "countAttempts"
private const val USER_ANSWERS: String = "userAnswers"
private const val NUMBER_ONE: String = "number1"
private const val NUMBER_TWO: String = "number2"
private const val PERCENT: String = "percent"
private const val STAGES_LIST: String = "stagesList"
private const val STAGE_COMPLETED: String = "stageCompleted"
private const val SECRET_REGISTRATION_WORD: String = "secretRegistrationWord"
private const val URL_STORAGE_REFERENCE_REAL: String = "gs://kspt-phoenix.appspot.com"
private const val URL_STORAGE_REFERENCE_DEVELOP: String = "gs://phoenix-develop.appspot.com"
const val AVATARS: String = "avatars"
private const val AVATAR: String = "avatar"

/**
 * FirebaseHelper is class which organization work with firebase database
 */
class FirebaseHelper {

    private val database = FirebaseDatabase.getInstance().reference
    private var storage = FirebaseStorage.getInstance()

    /**
     * Return @user object
     */
    fun fetchUserById(userId: String): Observable<User> {
        return RxFirebaseDatabase
                .observeSingleValueEvent(database.child(USERS).child(userId), User::class.java)
    }

    /**
     * Save secret registration word (only for Professor)
     */
    fun pushNewSecretRegistrationWord(secret: String) {
        val childUpdates = HashMap<String, Any>()
        childUpdates[SECRET_REGISTRATION_WORD] = secret
        database.updateChildren(childUpdates)
    }

    /**
     * Save user
     */
    fun pushNewUser(user: User) {

        val childUpdates = HashMap<String, Any>()
        childUpdates["$USERS/${user.id}"] = user.toMap()
        database.updateChildren(childUpdates)
    }

    /**
     * Return only stages from student's results (theme # 1)
     */
    fun fetchStages(userId: String): Observable<List<Stage>> {
        return RxFirebaseDatabase
                .observeSingleValueEvent(database.child(OVT).child(THEME_ONE).child(userId).child(STAGES_LIST),
                        DataSnapshotMapperAutoId.listOf(Stage::class.java))
    }

    /**
     * Return all student's results (theme # 1)
     */
    fun fetchStudentAnswersTaskOne(userId: String): Observable<StudentResults> {
        return RxFirebaseDatabase
                .observeSingleValueEvent(database.child(OVT).child(THEME_ONE).child(userId), StudentResults::class.java)
    }

    /**
     * Save student's result (theme # 1)
    */
    fun pushStudentAnswersTaskOne(userId: String, studentResult: StudentResults) {
        // removed an old result
        removeUserAnswersTaskOne(userId)

        val childUpdates = HashMap<String, Any>()

        // set percent in user profile (doesn't a good solve)
        childUpdates["$USERS/$userId/$PERCENT_OVT_1"] = studentResult.percent

        // save user result in studentResults
        childUpdates["$OVT/$THEME_ONE/$userId/$NUMBER_ONE"] = studentResult.number1
        childUpdates["$OVT/$THEME_ONE/$userId/$NUMBER_TWO"] = studentResult.number2
        childUpdates["$OVT/$THEME_ONE/$userId/$PERCENT"] = studentResult.percent

        for (stage in studentResult.stages) {
            childUpdates["$OVT/$THEME_ONE/$userId/$STAGES_LIST/${stage.key}/$COUNT_ATTEMPTS"] = stage.value.countAttempts
            childUpdates["$OVT/$THEME_ONE/$userId/$STAGES_LIST/${stage.key}/$STAGE_COMPLETED"] = stage.value.stageCompleted

            val userAnswers = stage.value.userAnswers
            for (i in 0 until userAnswers.size)
                childUpdates["$OVT/$THEME_ONE/$userId/$STAGES_LIST/${stage.key}/$USER_ANSWERS/$i"] = userAnswers[i].toMap()
        }

        database.updateChildren(childUpdates)
    }

    /**
     * Return system variables
     */
    fun fetchSystemVars(): Observable<SystemVars> {
        return RxFirebaseDatabase
                .observeSingleValueEvent(database.child(SYSTEM_VARS), SystemVars::class.java)
    }

    /**
     * Remove student's answer (theme # 1)
     */
    fun removeUserAnswersTaskOne(userId: String) {
        database.child(OVT).child(THEME_ONE).child(userId).ref.removeValue()
        database.child(USERS).child(userId).child(PERCENT_OVT_1).ref.removeValue()
    }

    /**
     * Remove user object by id
     */
    fun removeUser(userId: String) {
        database.child(USERS).child(userId).removeValue()
    }

    /**
     * Change link of the user's avatar
     */
    fun setUserAvatarInProfile(userId: String, uri: String) {
        database.child(USERS).child(userId).child(AVATAR).setValue(uri)
    }

    /**
     * Return a storageReference
     */
    fun gerStorageRef(): StorageReference {
        return if (BuildConfig.FLAVOR.equals(DEVELOP))
            storage.getReferenceFromUrl(URL_STORAGE_REFERENCE_DEVELOP).child(AVATARS)
        else
            storage.getReferenceFromUrl(URL_STORAGE_REFERENCE_REAL).child(AVATARS)
    }

    /**
     * Return all users in system
     */
    fun fetchAllUsers(): Observable<List<User>> {
        return RxFirebaseDatabase
                .observeSingleValueEvent(database.child(USERS), DataSnapshotMapperAutoId.listOf(User::class.java))
    }

    /**
     * Return secret registration word
     */
    fun fetchSecretWord(): Observable<String> {
        return RxFirebaseDatabase.observeSingleValueEvent(database.child(SYSTEM_VARS).child(SECRET_REGISTRATION_WORD), String::class.java)
    }

    /**
     * Remove all student's result (only for professor, every year new students)
     */
    fun globalRemoveData(onlyStudentResult: Boolean) {
        fetchAllUsers().subscribe {
            if (!onlyStudentResult)
                for (user in it)
                    if (user.userStatus == STUDENT)
                        database.child(USERS).child(user.id).removeValue()

            database.child(OVT).removeValue()
        }
    }
}
