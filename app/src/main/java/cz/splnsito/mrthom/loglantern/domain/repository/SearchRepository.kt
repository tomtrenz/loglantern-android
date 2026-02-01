package cz.splnsito.mrthom.loglantern.domain.repository

import cz.splnsito.mrthom.loglantern.feature.search.model.JobStatusContent
import cz.splnsito.mrthom.loglantern.feature.search.model.SplunkSearchResultResponse

interface SearchRepository {
    /**
     * Vytvoří nový search job
     * @param searchQuery SPL (Search Processing Language) query
     * @return Search ID (sid)
     */
    suspend fun createSearchJob(searchQuery: String): Result<String>

    /**
     * Získá status search jobu
     * @param sid Search ID
     * @return Job status informace
     */
    suspend fun getJobStatus(sid: String): Result<JobStatusContent>

    /**
     * Získá výsledky dokončeného search jobu
     * @param sid Search ID
     * @param count Maximální počet výsledků (0 = všechny)
     * @return Search výsledky
     */
    suspend fun getSearchResults(sid: String, count: Int = 0): Result<SplunkSearchResultResponse>

    /**
     * Čeká na dokončení search jobu s polling
     * @param sid Search ID
     * @param maxWaitTime Maximální čas čekání v ms
     * @param pollingInterval Interval pollingu v ms
     * @return Job status po dokončení nebo timeout
     */
    suspend fun waitForJobCompletion(
        sid: String,
        maxWaitTime: Long = 60000,
        pollingInterval: Long = 1000
    ): Result<JobStatusContent>
}
