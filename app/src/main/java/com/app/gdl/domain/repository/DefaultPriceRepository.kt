package com.app.gdl.domain.repository

import com.app.gdl.data.model.PriceResponse

interface DefaultPriceRepository {
  suspend fun  getDefautPrice(priceclass:String): PriceResponse
}