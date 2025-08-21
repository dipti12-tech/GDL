package com.app.gdl.di

import android.content.Context
import com.app.gdl.data.remote.ApiService
import com.app.gdl.data.repository.AllCategoryIdsImp
import com.app.gdl.data.repository.AllCategoryRepositoryImp
import com.app.gdl.data.repository.CategoryRepositoryImp
import com.app.gdl.data.repository.GetPastPopularCategoryImp
import com.app.gdl.data.repository.LoginRepositoryImpl
import com.app.gdl.data.repository.OrderHistoryImp
import com.app.gdl.data.repository.OrderPlaceImp
import com.app.gdl.data.repository.PopularItemRepositoryImp
import com.app.gdl.data.repository.ProductDetailsRepositoryImpl
import com.app.gdl.data.repository.ProductRepositoryImp
import com.app.gdl.data.repository.SignupRepositoryImpl
import com.app.gdl.data.repository.SubCategoryRepositoryImp
import com.app.gdl.data.repository.WarehouseImp
import com.app.gdl.domain.repository.AllCategoryIdRepository
import com.app.gdl.domain.repository.AllCategoryRepository
import com.app.gdl.domain.repository.CategoryRepository
import com.app.gdl.domain.repository.GetPopularCategoryRepository
import com.app.gdl.domain.repository.LoginRepository
import com.app.gdl.domain.repository.MyOrderHistoryRepository
import com.app.gdl.domain.repository.OrderRepository
import com.app.gdl.domain.repository.PopularItemRepository
import com.app.gdl.domain.repository.ProductDetailRepository
import com.app.gdl.domain.repository.ProductRepository
import com.app.gdl.domain.repository.SignUpRepository
import com.app.gdl.domain.repository.SubCategoryRepository
import com.app.gdl.domain.repository.WarehouseRepository
import com.app.gdl.utils.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val baseUrl = "https://services-gdl.onaotc.com/"

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // increase connection timeout
        .readTimeout(60, TimeUnit.SECONDS)     // increase read timeout
        .writeTimeout(60, TimeUnit.SECONDS)    // increase write timeout
        .build()

    @Provides
    @Singleton
    fun provideApiAservice(): ApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient) // <-- attach custom timeout client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(apiService: ApiService): CategoryRepository {
        return CategoryRepositoryImp(apiService)
    }

    @Provides
    @Singleton
    fun provideAllCategoryRepository(apiService: ApiService): AllCategoryRepository {
        return AllCategoryRepositoryImp(apiService)
    }

    @Provides
    @Singleton
    fun provideSignUpRepository(apiService: ApiService): SignUpRepository {
        return SignupRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideLoginRepository(
        @ApplicationContext context: Context,
        apiService: ApiService
    ): LoginRepository {
        return LoginRepositoryImpl(context, apiService)
    }

    @Provides
    @Singleton
    fun provideGetPopularRepository(
        apiService: ApiService
    ): GetPopularCategoryRepository {
        return GetPastPopularCategoryImp(apiService)
    }
    @Provides
    @Singleton
    fun provideGetProductRepository(
        apiService: ApiService
    ): ProductRepository{
        return  ProductRepositoryImp(apiService)
    }

    @Provides
    @Singleton
    fun provideGetProductDetailRepository(
        apiService: ApiService
    ): ProductDetailRepository{
        return  ProductDetailsRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideGetSubCategoryRepository(
        apiService: ApiService
    ): SubCategoryRepository{
        return  SubCategoryRepositoryImp(apiService)
    }

    @Provides
    @Singleton
    fun providePopularItemRepository(
        apiService: ApiService
    ): PopularItemRepository{
        return  PopularItemRepositoryImp(apiService)
    }

    @Provides
    @Singleton
    fun provideAllCategoryIdsRepository(
        apiService: ApiService
    ): AllCategoryIdRepository{
        return  AllCategoryIdsImp(apiService)
    }
    @Provides
    @Singleton
    fun provideWarehouseRepository(
        apiService: ApiService
    ): WarehouseRepository{
        return  WarehouseImp(apiService)
    }
    @Provides
    @Singleton
    fun provideOrderPlacedRepository(
        apiService: ApiService
    ): OrderRepository{
        return  OrderPlaceImp(apiService)
    }
    @Provides
    @Singleton
    fun provideOrderHistoryRepository(
        apiService: ApiService
    ): MyOrderHistoryRepository{
        return  OrderHistoryImp(apiService)
    }

    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context): SharedPref {
        return SharedPref(context)
    }
}
