package com.app.gdl.di

import android.content.Context
import com.app.gdl.data.remote.ApiService
import com.app.gdl.data.repository.AllCategoryRepositoryImp
import com.app.gdl.data.repository.CategoryRepositoryImp
import com.app.gdl.data.repository.GetPastPopularCategoryImp
import com.app.gdl.data.repository.LoginRepositoryImpl
import com.app.gdl.data.repository.SignupRepositoryImpl
import com.app.gdl.domain.repository.AllCategoryRepository
import com.app.gdl.domain.repository.CategoryRepository
import com.app.gdl.domain.repository.GetPopularCategoryRepository
import com.app.gdl.domain.repository.LoginRepository
import com.app.gdl.domain.repository.SignUpRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val baseUrl = "https://services-gdl.onaotc.com/"

    @Provides
    @Singleton
    fun provideApiAservice(): ApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
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
}
