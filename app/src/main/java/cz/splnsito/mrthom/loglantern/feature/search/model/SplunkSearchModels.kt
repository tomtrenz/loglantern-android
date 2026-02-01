package cz.splnsito.mrthom.loglantern.feature.search.model

import com.google.gson.annotations.SerializedName

// Response při vytvoření search jobu
data class SplunkSearchJobResponse(
    val sid: String  // Search ID
)

// Status search jobu
data class SplunkJobStatusResponse(
    val entry: List<JobStatusEntry> = emptyList()
)

data class JobStatusEntry(
    val name: String? = null,
    val content: JobStatusContent? = null
)

data class JobStatusContent(
    val sid: String? = null,
    @SerializedName("isDone")
    val isDone: Boolean = false,
    @SerializedName("isFailed")
    val isFailed: Boolean = false,
    @SerializedName("isFinalized")
    val isFinalized: Boolean = false,
    @SerializedName("dispatchState")
    val dispatchState: String? = null,
    @SerializedName("doneProgress")
    val doneProgress: Double = 0.0,
    @SerializedName("scanCount")
    val scanCount: Int = 0,
    @SerializedName("eventCount")
    val eventCount: Int = 0,
    @SerializedName("resultCount")
    val resultCount: Int = 0
)

// Výsledky search jobu
data class SplunkSearchResultResponse(
    val results: List<Map<String, Any>> = emptyList(),
    val fields: List<ResultField> = emptyList(),
    val preview: Boolean = false
)

data class ResultField(
    val name: String? = null
)
