package cz.splnsito.mrthom.loglantern.data.repository

import android.util.Log
import cz.splnsito.mrthom.loglantern.core.network.ApiServiceFactory
import cz.splnsito.mrthom.loglantern.core.security.TokenStorage
import cz.splnsito.mrthom.loglantern.domain.repository.SearchRepository
import cz.splnsito.mrthom.loglantern.feature.search.model.JobStatusContent
import cz.splnsito.mrthom.loglantern.feature.search.model.SplunkSearchResultResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val apiServiceFactory: ApiServiceFactory,
    private val tokenStorage: TokenStorage
) : SearchRepository {

    private suspend fun getAuthHeader(): String {
        val token = tokenStorage.getToken().first()
        return "Bearer $token"
    }

    override suspend fun createSearchJob(searchQuery: String): Result<String> {
        return try {
            val splunkApiService = apiServiceFactory.create()
            val authHeader = getAuthHeader()

            Log.d("SearchRepository", "Creating search job with query: $searchQuery")

            val response = splunkApiService.createSearchJob(
                token = authHeader,
                searchQuery = searchQuery
            )

            Log.d("SearchRepository", "Search job created with SID: ${response.sid}")
            Result.success(response.sid)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to create search job", e)
            Result.failure(e)
        }
    }

    override suspend fun getJobStatus(sid: String): Result<JobStatusContent> {
        return try {
            val splunkApiService = apiServiceFactory.create()
            val authHeader = getAuthHeader()

            val response = splunkApiService.getJobStatus(
                token = authHeader,
                sid = sid
            )

            val statusContent = response.entry.firstOrNull()?.content
            if (statusContent != null) {
                Log.d("SearchRepository", "Job $sid status: isDone=${statusContent.isDone}, " +
                        "progress=${statusContent.doneProgress}, resultCount=${statusContent.resultCount}")
                Result.success(statusContent)
            } else {
                Result.failure(Exception("No status content found for job $sid"))
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to get job status", e)
            Result.failure(e)
        }
    }

    override suspend fun getSearchResults(sid: String, count: Int): Result<SplunkSearchResultResponse> {
        return try {
            val splunkApiService = apiServiceFactory.create()
            val authHeader = getAuthHeader()

            Log.d("SearchRepository", "Fetching results for job: $sid")

            val response = splunkApiService.getResults(
                token = authHeader,
                sid = sid,
                count = count
            )

            Log.d("SearchRepository", "Received ${response.results.size} results, " +
                    "${response.fields.size} fields, preview=${response.preview}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to get search results", e)
            Result.failure(e)
        }
    }

    override suspend fun waitForJobCompletion(
        sid: String,
        maxWaitTime: Long,
        pollingInterval: Long
    ): Result<JobStatusContent> {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < maxWaitTime) {
            val statusResult = getJobStatus(sid)

            if (statusResult.isFailure) {
                return statusResult
            }

            val status = statusResult.getOrNull()!!

            if (status.isDone) {
                Log.d("SearchRepository", "Job $sid completed successfully")
                return Result.success(status)
            }

            if (status.isFailed) {
                Log.e("SearchRepository", "Job $sid failed")
                return Result.failure(Exception("Search job failed"))
            }

            Log.d("SearchRepository", "Job $sid in progress: ${status.doneProgress * 100}%")
            delay(pollingInterval)
        }

        Log.w("SearchRepository", "Job $sid timed out after ${maxWaitTime}ms")
        return Result.failure(Exception("Search job timed out"))
    }
}
