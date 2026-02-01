package cz.splnsito.mrthom.loglantern.domain.usecase

import cz.splnsito.mrthom.loglantern.domain.repository.SearchRepository
import cz.splnsito.mrthom.loglantern.feature.search.model.SplunkSearchResultResponse
import javax.inject.Inject

/**
 * Use case pro spuštění Splunk search a získání výsledků
 */
class ExecuteSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    /**
     * Spustí search, počká na dokončení a vrátí výsledky
     * @param searchQuery SPL query (např. "search index=main | head 100")
     * @param maxWaitTime Maximální čas čekání v ms
     * @param resultCount Maximální počet výsledků (0 = všechny)
     */
    suspend operator fun invoke(
        searchQuery: String,
        maxWaitTime: Long = 60000,
        resultCount: Int = 0
    ): Result<SplunkSearchResultResponse> {
        // 1. Vytvoříme search job
        val sidResult = searchRepository.createSearchJob(searchQuery)
        if (sidResult.isFailure) {
            return Result.failure(sidResult.exceptionOrNull()!!)
        }

        val sid = sidResult.getOrNull()!!

        // 2. Počkáme na dokončení jobu
        val statusResult = searchRepository.waitForJobCompletion(
            sid = sid,
            maxWaitTime = maxWaitTime
        )
        if (statusResult.isFailure) {
            return Result.failure(statusResult.exceptionOrNull()!!)
        }

        // 3. Získáme výsledky
        return searchRepository.getSearchResults(sid, resultCount)
    }
}
