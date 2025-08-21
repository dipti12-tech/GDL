package com.app.gdl.data.remote

import ProductDetailsResponse
import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.model.GetPopularCategoryResponse
import com.app.gdl.data.model.LoginRequest
import com.app.gdl.data.model.LoginResponse
import com.app.gdl.data.model.MyOrdersResponse
import com.app.gdl.data.model.OrderRequest
import com.app.gdl.data.model.OrderResponse
import com.app.gdl.data.model.PriceResponse
import com.app.gdl.data.model.ProductListResponse
import com.app.gdl.data.model.ProductResponse
import com.app.gdl.data.model.SignupRequest
import com.app.gdl.data.model.SignupResponse
import com.app.gdl.data.model.WarehouseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
    suspend fun getProducts(
        @Path("id") id: String,
        @Query("price_class") priceClass: String
    ): ProductResponse

    @GET("get_product_details/{id}")
    suspend fun getProductsDetails(
        @Path("id") id: String,
        @Query("price_class") priceClass: String
    ): ProductDetailsResponse

    @GET("get_popular_products/")
    suspend fun getPopularProducts(@Query("price_class") priceClass: String): ProductResponse

    @GET("get_sub_categories/{id}")
    suspend fun getSubCategory(@Path("id") id: String): CategoryResponse


    @GET("get_warehouses/")
    suspend fun getWarehouses(): WarehouseResponse

    @GET("get_custom_lists/")
    suspend fun getCustomLists(@Query("price_class") priceclass: String,@Query("warehouse") warehouse:String): ProductListResponse

    @POST("create_order/")
    suspend fun placeOrder(@Body request: OrderRequest): Response<OrderResponse>

    @GET("get_order_history/{id}")
    suspend fun getOrderHistory(@Path("id") id: Int): MyOrdersResponse

}

