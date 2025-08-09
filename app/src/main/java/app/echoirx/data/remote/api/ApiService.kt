package app.echoirx.data.remote.api

import app.echoirx.data.remote.dto.PlaybackResponseDto
import app.echoirx.data.remote.dto.SearchItemDto
import app.echoirx.data.remote.dto.SearchResponseDto
import app.echoirx.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject

class ApiService @Inject constructor(
    private val client: HttpClient,
    private val settingsRepository: SettingsRepository
) {
    private suspend fun getBaseUrl(): String = settingsRepository.getServerUrl()

    suspend fun search(query: String, type: String): List<SearchItemDto> =
        withContext(Dispatchers.IO) {
            val baseUrl = getBaseUrl()

            val response = client.get("$baseUrl/search") {
                parameter("query", query)
                parameter("type", type)
            }.body<SearchResponseDto>()

            response.items
        }

    suspend fun getAlbumTracks(albumId: Long): List<SearchItemDto> =
        withContext(Dispatchers.IO) {
            val baseUrl = getBaseUrl()

            val response = client.get("$baseUrl/album/tracks") {
                parameter("id", albumId)
            }.body<SearchResponseDto>()

            response.items
        }

    suspend fun getDownloadInfo(
        trackId: Long,
        quality: String,
        modes: List<String>?
    ): Pair<PlaybackResponseDto, Map<String, String>> =
        withContext(Dispatchers.IO) {
            val baseUrl = getBaseUrl()

            val mode = when {
                modes?.contains("DOLBY_ATMOS") == true -> "DOLBY_ATMOS"
                modes?.contains("STEREO") == true -> "STEREO"
                else -> null
            }

            coroutineScope {
                val playback = async {
                    client.get("$baseUrl/track/playback") {
                        parameter("id", trackId)
                        parameter("quality", quality)
                        if (mode != null) {
                            parameter("mode", mode)
                        }
                    }.body<PlaybackResponseDto>()
                }

                val metadata = async {
                    client.get("$baseUrl/track/metadata") {
                        parameter("id", trackId)
                    }.body<Map<String, String>>()
                }

                Pair(playback.await(), metadata.await())
            }
        }

    suspend fun getTrackPreview(trackId: Long): PlaybackResponseDto =
        withContext(Dispatchers.IO) {
            val baseUrl = getBaseUrl()

            client.get("$baseUrl/track/preview") {
                parameter("id", trackId)
            }.body()
        }

    suspend fun downloadFile(url: String): ByteArray =
        withContext(Dispatchers.IO) {
            client.get(url).body()
        }

    suspend fun downloadFileToStream(url: String, outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            client.prepareGet(url).execute {
                it.bodyAsChannel().copyTo(outputStream)
            }
        }
    }
}