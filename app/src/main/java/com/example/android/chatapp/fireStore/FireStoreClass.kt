package com.example.android.chatapp.fireStore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.android.chatapp.activities.*
import com.example.android.chatapp.models.Message
import com.example.android.chatapp.models.User
import com.example.android.chatapp.models.UserState
import com.example.android.chatapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun registerUser(activity: AuthActivity, userInfo: User) {
        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it.
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the user.",
                    e
                )
            }
    }


    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun getUserDetails(lifecycleOwner: LifecycleOwner) {
        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i(lifecycleOwner.javaClass.simpleName, document.toString())
                // Here we have received the document snapshot which is converted into the User Data model object.
                val user = document.toObject(User::class.java)!!

                val context = if (lifecycleOwner is Activity){
                    ( lifecycleOwner as Activity)
                }else{
                    (lifecycleOwner as Fragment).requireActivity()
                }
                val sharedPreferences=context.getSharedPreferences(
                    Constants.CHAT_APP_PREFERENCES,
                    Context.MODE_PRIVATE
                )
                val editor: SharedPreferences.Editor=sharedPreferences.edit()
                //Key:logged_in_username
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    user.userName
                )
                editor.apply()
                // START
                when (lifecycleOwner) {
                    is LoginActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        lifecycleOwner.userLoggedInSuccess(user)
                    }
                    is ChatActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        lifecycleOwner.getCurrentUserDetailsSuccess(user)
                    }
                    is LatestMessagesActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        lifecycleOwner.getCurrentUserDetailsSuccess(user)
                    }
                    is NewMessagesActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        lifecycleOwner.getCurrentUserDetailsSuccess(user)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log.
                when (lifecycleOwner) {
                    is LoginActivity -> {
                        lifecycleOwner.hideProgressDialog()
                    }
                    is ChatActivity -> {
                        lifecycleOwner.hideProgressDialog()
                    }
                }
                Log.e(lifecycleOwner.javaClass.simpleName,
                    "Error while getting user details.", e)
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String) {
        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(activity, imageFileURI))
        //adding the file to reference
        sRef.putFile(imageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e("Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())
                        // Here call a function of base activity for transferring the result to it.
                        when (activity) {
                            is AuthActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }
                        }
                    }
            }
            .addOnFailureListener { exception ->
                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is AuthActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    exception.message,
                    exception
                )
            }
    }

    fun getAllUsersList(activity: Activity) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.USERS)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                Log.e("users List", document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents) {

                    val user = i.toObject(User::class.java)
                    user!!.id= i.id

                    usersList.add(user)
                }

                when (activity) {
                    is NewMessagesActivity -> {
                        activity.successUsersListFromFireStore(usersList)
                    }
                    is LatestMessagesActivity -> {
                        activity.successUsersListFromFireStore(usersList)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                when (activity) {
                    is NewMessagesActivity -> {
                        activity.hideProgressDialog()
                    }
                    is LatestMessagesActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("Get user List", "Error while getting all users list.", e)
            }
    }

    fun sendMessagesToFirebase(activity: ChatActivity, message: Message){
        val reference = database.getReference(Constants.MESSAGES).push()
        message.id = reference.key!!
        reference.setValue(message)
                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName, "sending user message successfully")
                }.addOnFailureListener { e ->
                    Log.e(activity.javaClass.simpleName, "Error while sending user message.", e)
                }
    }

    fun listenForMessages(activity: ChatActivity, currentUserId: String, otherUserId: String){
        val mChat: ArrayList<Message> = ArrayList()

        database.getReference(Constants.MESSAGES).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mChat.clear()
                for (i in snapshot.children){
                    val chatMessage: Message = i.getValue(Message::class.java) as Message
                    if (chatMessage.sender == currentUserId && chatMessage.receiver == otherUserId ||
                            chatMessage.receiver == currentUserId && chatMessage.sender == otherUserId ){
                        mChat.add(chatMessage)
                    }
                    activity.viewMessagesSuccessfully(mChat)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun updateUserStatus (state : String){

        val currentUserId :String=getCurrentUserID()
        val calendar: Calendar = Calendar.getInstance()
        val currentDate: String = SimpleDateFormat("MMM dd, yyyy").format(calendar.time)
        val currentTime: String = SimpleDateFormat("hh:mm a").format(calendar.time)

        val userState =UserState(
                currentDate,
                currentUserId,
                state,
                currentTime)
            database.getReference(Constants.USERS).
            child(currentUserId).setValue(userState)
    }


    fun listenForUserState(activity: ChatActivity, otherUserId: String){
        database.getReference(Constants.USERS).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (i in snapshot.children){
                    if (otherUserId==i.key!!){
                        val userState: UserState = i.getValue(UserState::class.java) as UserState
                        activity.viewUserStateSuccessfully(userState)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    fun listenForAllUserState(activity: LatestMessagesActivity){
        val mUsersStateList: ArrayList<UserState> = ArrayList()

        database.getReference(Constants.USERS).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val userState:UserState = i.getValue(UserState::class.java) as UserState
                    mUsersStateList.add(userState)
                }
                activity.viewUserStateSuccessfully(mUsersStateList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun updateSeenMessage (otherUserId: String){

        val seenMessageMap = HashMap<String,Any?>()
        val currentUserId :String=getCurrentUserID()
        database.getReference(Constants.MESSAGES).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val message :Message = i.getValue(Message::class.java) as Message
                    if (message.receiver==currentUserId && message.sender==otherUserId) {
                        if (message.sender!=currentUserId){
                            seenMessageMap["seen"] = true
                            i.ref.updateChildren(seenMessageMap)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


}