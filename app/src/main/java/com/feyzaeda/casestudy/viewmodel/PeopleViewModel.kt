package com.feyzaeda.casestudy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feyzaeda.casestudy.R
import com.feyzaeda.casestudy.repo.PersonRepository
import com.feyzaeda.casestudy.model.UIState
import com.feyzaeda.casestudy.util.Constants
import com.feyzaeda.casestudy.util.Event
import com.feyzaeda.casestudy.util.Results
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PeopleViewModel @Inject constructor(private val personRepository: PersonRepository, @ApplicationContext context: Context) : ViewModel() {

    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private var nextPage: String? = null
    private var fetchPeopleJob: Job? = null
    private var tryAgainTxt = StringBuilder().append(context.getString(R.string.try_again), Constants.Second.TRY_SECOND)

    init {
        fetchPeople()
    }
    fun onEvent(event: Event) {
        when (event) {
            Event.SHOW_ERROR_MSG -> {
                _uiState.update {
                    it.copy(error = null)
                }
            }
            Event.END_OF_THE_PAGE -> fetchPeople()
            Event.SWIPE_REFRESH -> fetchPeople(true)
            Event.SHOW_END_OF_THE_PAGE_MSG -> _uiState.update {
                it.copy(msgEndOfThePage = false)
            }
        }
    }

    private fun fetchPeople(isRefreshing: Boolean = false) {
        if (isRefreshing) {
            nextPage = null
            _uiState.update {
                it.copy(peopleList = emptyList(), endOfThePage = false, emptyPeople = false)
            }
        }

        if (_uiState.value.endOfThePage) {
            return
        }

        cancelFetchPeopleJob()

        _uiState.update {
            it.copy(isStateFetching = true, isStateRefreshing = isRefreshing)
        }

        fetchPeopleJob = viewModelScope.launch {
            val result = personRepository.fetchPeople(nextPage)
            when (result) {
                is Results.Loading -> {
                    _uiState.update {
                        it.copy(isStateFetching = true)
                    }
                }

                is Results.Error -> {
                    _uiState.update {
                        it.copy(
                            isStateFetching = false,
                            isStateRefreshing = false,
                            error = result.error + tryAgainTxt
                        )
                    }

                    delay(Constants.Second.TRY_SECOND.toLong())
                    fetchPeople(isRefreshing)
                }

                is Results.Success -> {
                    val people = _uiState.value.peopleList.toMutableList()
                    val peopleSizeOld = people.size

                    val currentPeopleIds =
                        _uiState.value.peopleList.mapTo(HashSet(_uiState.value.peopleList.size)) { it.id }

                    result.data.people.forEach { person ->
                        if (!currentPeopleIds.contains(person.id)) {
                            people.add(person)
                        }
                    }

                    val peopleSizeNew = people.size

                    val reachedEndOfThePeople =
                        people.isNotEmpty() && peopleSizeOld == peopleSizeNew

                    _uiState.update {
                        it.copy(
                            isStateFetching = false,
                            isStateRefreshing = false,
                            peopleList = people,
                            emptyPeople = people.isEmpty(),
                            endOfThePage = reachedEndOfThePeople,
                            msgEndOfThePage = reachedEndOfThePeople,
                        )
                    }
                    nextPage = result.data.next
                }
            }
        }
    }

    private fun cancelFetchPeopleJob() {
        fetchPeopleJob?.cancel()
        fetchPeopleJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelFetchPeopleJob()
    }
}