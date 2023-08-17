package com.feyzaeda.casestudy.model

import com.feyzaeda.casestudy.datasource.Person

data class UIState(
    val peopleList: List<Person> = emptyList(),
    val isStateFetching: Boolean = false,
    val isStateRefreshing: Boolean = false,
    val error: String? = null,
    val endOfThePage: Boolean = false,
    val msgEndOfThePage: Boolean = false,
    val emptyPeople: Boolean = false,
)
