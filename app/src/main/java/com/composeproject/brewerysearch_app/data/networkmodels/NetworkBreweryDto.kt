package com.composeproject.brewerysearch_app.data.networkmodels

import com.squareup.moshi.Json

// Sample Response:
/*
[
    {
        "id": "5128df48-79fc-4f0f-8b52-d06be54d0cec",
        "name": "(405) Brewing Co",
        "brewery_type": "micro",
        "address_1": "1716 Topeka St",
        "address_2": null,
        "address_3": null,
        "city": "Norman",
        "state_province": "Oklahoma",
        "postal_code": "73069-8224",
        "country": "United States",
        "longitude": -97.46818222,
        "latitude": 35.25738891,
        "phone": "4058160490",
        "website_url": "http://www.405brewing.com",
        "state": "Oklahoma",
        "street": "1716 Topeka St"
    }
]
 */

data class NetworkBreweryDto(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "brewery_type") val breweryType: String?,
    @Json(name = "address_1") val addressOne: String?,
    @Json(name = "address_2") val addressTwo: String?,
    @Json(name = "address_3") val addressThree: String?,
    @Json(name = "city") val city: String?,
    @Json(name = "state_province") val stateProvince: String?,
    @Json(name = "postal_code") val postalCode: String?,
    @Json(name = "country") val country: String?,
    @Json(name = "longitude") val longitude: Any?,
    @Json(name = "latitude") val latitude: Any?,
    @Json(name = "phone") val phone: String?,
    @Json(name = "website_url") val websiteUrl: String?,
    @Json(name = "state") val state: String?,
    @Json(name = "street") val street: String?
)