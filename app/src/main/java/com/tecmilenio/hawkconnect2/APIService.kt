package com.tecmilenio.hawkconnect2

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET


interface APIService {

    @POST("users.aspx/UserLogin")
    suspend fun loginUser(@Body loginData: LoginRequest): Response<LoginResponse>


    @POST("users.aspx/UserUI")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("users.aspx/Friends")
    suspend fun getFriends(@Body request: FriendFilterRequest): Response<FriendResponse>


    @POST("users.aspx/FriendsUI")
    suspend fun saveFriends(@Body request: SaveFriendsRequest): Response<SaveFriendsResponse>


    @GET("campus.aspx")
    suspend fun getCampuses(): Response<CampusResponse>

    @POST("posts.aspx/Posts")
    suspend fun getPosts(@Body request: PostFilterRequest): Response<PostResponse>


    @POST("posts.aspx/PostsUI")
    suspend fun createPost(@Body request: NewPostRequest): Response<NewPostResponse>

}