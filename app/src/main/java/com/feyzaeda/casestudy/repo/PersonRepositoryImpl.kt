package com.feyzaeda.casestudy.repo

import com.feyzaeda.casestudy.datasource.DataSource
import com.feyzaeda.casestudy.datasource.FetchResponse
import com.feyzaeda.casestudy.util.Results
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
class PersonRepositoryImpl(private val dataSource: DataSource) : PersonRepository {

    override suspend fun fetchPeople(nextPage: String?): Results<FetchResponse> =
        withContext(Dispatchers.IO) {
            dataSource.fetchAwait(nextPage)
        }

}


@OptIn(ExperimentalCoroutinesApi::class)
suspend fun DataSource.fetchAwait(nextPage: String?) =
    suspendCancellableCoroutine { cont ->
        fetch(nextPage) { fetchResponse, fetchError ->
            if (fetchError != null) {
                cont.resume(Results.Error(fetchError.errorDescription), null)
            } else if (fetchResponse != null) {
                cont.resume(Results.Success(fetchResponse), null)
            } else {
                cont.resume(Results.Error("Unknown error"), null)
            }
        }
    }