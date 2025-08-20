package com.composeproject.brewerysearch_app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.composeproject.brewerysearch_app.data.models.BreweryDto
import androidx.core.net.toUri

@Composable
fun DetailsScreen(
    brewery: BreweryDto?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = brewery?.name ?: "Brewery details",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (brewery == null) {
            Text("No details available.")
        } else {
            InfoRow("Type", brewery.breweryType)
            // Clickable address block: tap to open in Google Maps or other map apps
            MapAddressRow("Address", brewery)
            InfoRow("Street", brewery.street)
            InfoRow("Address 1", brewery.addressOne)
            InfoRow("Address 2", brewery.addressTwo)
            InfoRow("Address 3", brewery.addressThree)
            InfoRow("City", brewery.city)
            InfoRow("State/Province", brewery.stateProvince ?: brewery.state)
            InfoRow("Postal Code", brewery.postalCode)
            InfoRow("Country", brewery.country)
            InfoRow("Phone", brewery.phone)
            LinkRow("Website", brewery.websiteUrl)
            Spacer(modifier = Modifier.height(16.dp))
        }
        Button(onClick = onBack, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LinkRow(label: String, rawUrl: String?) {
    if (!rawUrl.isNullOrBlank()) {
        val context = LocalContext.current
        val url = normalizeUrl(rawUrl)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            val linkColor: Color = MaterialTheme.colorScheme.primary
            val annotated = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(rawUrl)
                }
            }
            Text(
                text = annotated,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    runCatching {
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        context.startActivity(intent)
                    }
                }
            )
        }
    }
}

@Composable
private fun MapAddressRow(label: String, brewery: BreweryDto) {
    val context = LocalContext.current
    val address = buildAddressString(brewery)
    val linkColor: Color = MaterialTheme.colorScheme.primary
    if (address.isNotBlank() || (!brewery.latitude.isNullOrBlank() && !brewery.longitude.isNullOrBlank())) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            val annotated = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(address.ifBlank { "Open in Maps" })
                }
            }
            Text(
                text = annotated,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    openInMaps(context, brewery, address)
                }
            )
        }
    }
}

private fun openInMaps(context: android.content.Context, brewery: BreweryDto, address: String) {
    val lat = brewery.latitude?.toDoubleOrNull()
    val lon = brewery.longitude?.toDoubleOrNull()

    val uri: Uri = if (lat != null && lon != null) {
        // geo:lat,lon?q=lat,lon(Label)
        val label = brewery.name?.takeIf { it.isNotBlank() } ?: address
        Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(label)})")
    } else {
        // geo:0,0?q=encodedAddress
        Uri.parse("geo:0,0?q=${Uri.encode(address)}")
    }

    // Try Google Maps app first, then fallback to any capable app, then to web
    try {
        val gmmIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(gmmIntent)
    } catch (e: ActivityNotFoundException) {
        try {
            val anyMapIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(anyMapIntent)
        } catch (_: Exception) {
            // Fallback to web Google Maps
            val webUrl = if (lat != null && lon != null) {
                "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
            } else {
                "https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}"
            }
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
            }
        }
    }
}

private fun buildAddressString(brewery: BreweryDto): String {
    val street = listOfNotNull(
        brewery.street?.takeIf { it.isNotBlank() },
        brewery.addressOne?.takeIf { it.isNotBlank() },
        brewery.addressTwo?.takeIf { it.isNotBlank() },
        brewery.addressThree?.takeIf { it.isNotBlank() }
    ).firstOrNull()

    val parts = listOfNotNull(
        street,
        brewery.city?.takeIf { it.isNotBlank() },
        (brewery.stateProvince ?: brewery.state)?.takeIf { !it.isNullOrBlank() },
        brewery.postalCode?.takeIf { it.isNotBlank() },
        brewery.country?.takeIf { it.isNotBlank() }
    )

    return parts.joinToString(", ")
}

private fun normalizeUrl(input: String): String {
    val trimmed = input.trim()
    val lower = trimmed.lowercase()
    return if (lower.startsWith("http://") || lower.startsWith("https://")) trimmed else "http://$trimmed"
}
