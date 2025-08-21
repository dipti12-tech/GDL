package com.app.gdl.domain.repository

import com.app.gdl.data.model.ProductResponse

interface PopularItemRepository {
        suspend fun  getPopularItem(priceclass:String): ProductResponse

}