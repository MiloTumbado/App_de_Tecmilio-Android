package com.tecmilenio.hawkconnect2

import com.google.gson.annotations.SerializedName

data class Campus(
    @SerializedName("CampusID") val campusID: Int,
    @SerializedName("CampusName") val campusName: String,
    @SerializedName("IsActive") val isActive: Boolean
)

data class  Campuses(
    @SerializedName("Campuses") val campuses: List<Campus>
)


data class RegisterRequest(
    @SerializedName("NewUser") val newUser: NewUser
)

data class NewUser(
    @SerializedName("Name") val name: String,
    @SerializedName("LastName") val lastName: String,
    @SerializedName("Email") val email: String,
    @SerializedName("Password") val password: String,
    @SerializedName("StudentNumber") val studentNumber: String,
    @SerializedName("CampusID") val campusId: Int? = null // Hacer que CampusID sea opcional
)


data class RegisterResponse(
    @SerializedName("d") val data: RegisterResult
)

data class RegisterResult(
    @SerializedName("ExecuteResult") val executeResult: String,
    @SerializedName("Message") val message: String
)


// Models.kt

data class FriendFilterRequest(
    @SerializedName("FriendsFilter") val friendsFilter: FriendsFilter
)

data class FriendsFilter(
    @SerializedName("LoggedUserID") val loggedUserId: Int,
    @SerializedName("CampusID") val campusId: Int,
    @SerializedName("Name") val name: String = ""
)

data class FriendResponse(
    @SerializedName("d") val data: FriendData
)

data class FriendData(
    @SerializedName("ExecuteResult") val executeResult: String,
    @SerializedName("Message") val message: String,
    @SerializedName("Friends") val friends: List<Friend>?
)

data class Friend(
    @SerializedName("UserID") val userId: Int,
    @SerializedName("CompleteName") val completeName: String,
    @SerializedName("Email") val email: String,
    @SerializedName("StudentNumber") val studentNumber: Int,
    @SerializedName("CampusID") val campusId: Int,
    @SerializedName("CampusName") val campusName: String,
    @SerializedName("IsFriend") val isFriend: Boolean
)

// Request to save the list of friends
data class SaveFriendsRequest(
    @SerializedName("FriendsList") val friendsList: FriendsList
)

data class FriendsList(
    @SerializedName("LoggedUserID") val loggedUserId: Int,
    @SerializedName("Friends") val friends: String // comma-separated list of friend IDs
)

data class SaveFriendsResponse(
    @SerializedName("d") val data: SaveFriendData
)

data class SaveFriendData(
    @SerializedName("ExecuteResult") val executeResult: String,
    @SerializedName("Message") val message: String
)
