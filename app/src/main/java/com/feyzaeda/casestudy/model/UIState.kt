package com.feyzaeda.casestudy.model

import com.feyzaeda.casestudy.datasource.Person

data class UIState(
    val people: List<Person> = emptyList(),
    val isFetching: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val reachedEndOfThePeople: Boolean = false,
    val willDisplayReachedEndOfThePeopleMessage: Boolean = false,
    val noPeople: Boolean = false,
)
