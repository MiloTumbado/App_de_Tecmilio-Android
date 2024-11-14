package com.tecmilenio.hawkconnect2

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET


interface APIService {

    @POST("UserLogin")
    suspend fun loginUser(@Body loginData: LoginRequest): Response<LoginResponse>

    @POST("UserUI")
    suspend fun registerUser(@Body newUser: RegisterRequest): Response<RegisterResponse>

    @POST("Friends")
    suspend fun getFriends(@Body filterRequest: FriendFilterRequest): Response<FriendResponse>

    @POST("FriendsUI")
    suspend fun saveFriends(@Body saveRequest: SaveFriendsRequest): Response<SaveFriendsResponse>

    @GET("campus.aspx")
    suspend fun getCampuses(): Response<CampusResponse>

    @POST("posts.aspx/Posts")
    suspend fun getPosts(@Body request: PostFilterRequest): Response<PostResponse>


    @POST("posts.aspx/PostsUI")
    suspend fun createPost(@Body request: NewPostRequest): Response<PostResponse>


}