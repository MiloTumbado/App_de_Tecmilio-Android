package com.tecmilenio.hawkconnect2

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import com.google.gson.annotations.SerializedName


interface APIService {

    @POST("UserLogin")
    suspend fun loginUser(@Body loginData: LoginRequest): Response<LoginResponse>

    @POST("UserUI")
    suspend fun registerUser(@Body newUser: RegisterRequest): Response<RegisterResponse>

    @POST("Friends")
    suspend fun getFriends(@Body filterRequest: FriendFilterRequest): Response<FriendResponse>

    @POST("FriendsUI")
    suspend fun saveFriends(@Body saveRequest: SaveFriendsRequest): Response<SaveFriendsResponse>
}




data class LoginRequest(
    @SerializedName("LoginData") val loginData: LoginData
)

data class LoginData(
    @SerializedName("Email") val email: String,
    @SerializedName("Password") val password: String
)

data class LoginResponse(
    @SerializedName("d") val data: LoginResult
)

data class LoginResult(
    @SerializedName("ExecuteResult") val executeResult: String,
    @SerializedName("Message") val message: String,
    @SerializedName("UserLogged") val userLogged: List<User>?
)

data class User(
    @SerializedName("UserId") val userId: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("LastName") val lastName: String,
    @SerializedName("Email") val email: String,
    @SerializedName("StudentNumber") val studentNumber: Int,
    @SerializedName("CampusID") val campusId: Int,
    @SerializedName("CampusName") val campusName: String
)