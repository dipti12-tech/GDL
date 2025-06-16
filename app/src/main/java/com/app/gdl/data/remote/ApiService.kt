package com.app.gdl.data.remote

import ProductDetailsResponse
import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.model.GetPopularCategoryResponse
import com.app.gdl.data.model.LoginRequest
import com.app.gdl.data.model.LoginResponse
import com.app.gdl.data.model.ProductResponse
import com.app.gdl.data.model.SignupRequest
import com.app.gdl.data.model.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("get_categories/featured")
    suspend fun getCategories(): CategoryResponse

    @GET("get_categories/all")
    suspend fun getAllCategories(): CategoryResponse

    @POST("sign_up/")
    suspend fun signUp(@Body request: SignupRequest): SignupResponse

    @POST("login/")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @GET("get_categories/popular")
    suspend fun getPopularCategories(): GetPopularCategoryResponse

    @GET("get_products_by_categories/{id}")
    suspend fun getProducts(@Path("id") id: String): ProductResponse

    @GET("get_product_details/{id}")
    suspend fun getProductsDetails(@Path("id") id: String): ProductDetailsResponse

//    @GET("get_product_details/{id}")
//    suspend fun getProductsDetails(@Path("id") id: String): ProductDetailsResponse

    @GET("get_sub_categories/{id}")
    suspend fun getSubCategory(@Path("id") id:String): CategoryResponse

    @GET("get_popular_products/")
    suspend fun getPopularProducts(): ProductResponse

}

