package com.composeproject.brewerysearch_app.data.repository

import com.composeproject.brewerysearch_app.data.models.BreweryDto
import com.composeproject.brewerysearch_app.data.networkmodels.NetworkBreweryDto
import com.composeproject.brewerysearch_app.data.remote.NetworkDataSource

class AppRepository(
    private val networkDataSource: NetworkDataSource
) {
    suspend fun getBreweries(page: Int = 1, perPage: Int = 50): List<BreweryDto> {
        val pageIndex = if (page < 1) 1 else page
        val pageSize = perPage.coerceIn(1, 200)
        return networkDataSource.getBreweryList(pageIndex, pageSize).map { convertFromNetwork(it) }
    }

    suspend fun autocomplete(query: String): List<BreweryDto> {
        if (query.isBlank()) return emptyList()
        return networkDataSource.autocomplete(query).map { convertFromNetwork(it) }
    }

    suspend fun getBreweryById(id: String): BreweryDto? {
        if (id.isBlank()) return null
        return runCatching { networkDataSource.getBreweryById(id) }.getOrNull()?.let { convertFromNetwork(it) }
    }

    private fun convertFromNetwork(network: NetworkBreweryDto): BreweryDto {
        return BreweryDto(
            id = network.id ?: "",
            name = network.name ?: "",
            breweryType = network.breweryType ?: "",
            addressOne = network.addressOne ?: "",
            addressTwo = network.addressTwo ?: "",
            addressThree = network.addressThree ?: "",
            city = network.city ?: "",
            stateProvince = network.stateProvince ?: "",
            postalCode = network.postalCode ?: "",
            country = network.country ?: "",
            longitude = network.longitude?.toString() ?: "",
            latitude = network.latitude?.toString() ?: "",
            phone = network.phone ?: "",
            websiteUrl = network.websiteUrl ?: "",
            state = network.state ?: "",
            street = network.street ?: ""
        )
    }
}