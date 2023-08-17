package com.feyzaeda.casestudy.repo

import com.feyzaeda.casestudy.datasource.FetchResponse
import com.feyzaeda.casestudy.util.Results


interface PersonRepository {

    suspend fun fetchPeople(nextPage: String? = null): Results<FetchResponse>

}