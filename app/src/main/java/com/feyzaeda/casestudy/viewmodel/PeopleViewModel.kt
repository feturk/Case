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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PeopleViewModel @Inject constructor(private val personRepository: PersonRepository) : ViewModel() {

    private var _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private var nextPage: String? = null
    private var fetchPeopleJob: Job? = null

    fun onEvent(event: Event, context: Context) {
        when (event) {
            Event.SHOW_ERROR_MSG -> {
                _uiState.update {
                    it.copy(error = null)
                }
            }
            Event.END_OF_THE_PAGE -> fetchPeople(context = context)
            Event.SWIPE_REFRESH -> fetchPeople(true, context)
            Event.SHOW_END_OF_THE_PAGE_MSG -> _uiState.update {
                it.copy(willDisplayReachedEndOfThePeopleMessage = false)
            }
        }
    }

    fun fetchPeople(isRefreshing: Boolean = false, context : Context) {
        if (isRefreshing) {
            nextPage = null
            _uiState.update {
                it.copy(people = emptyList(), reachedEndOfThePeople = false, noPeople = false)
            }
        }

        if (_uiState.value.reachedEndOfThePeople) {
            return
        }

        cancelFetchPeopleJob()

        _uiState.update {
            it.copy(isFetching = true, isRefreshing = isRefreshing)
        }
        val tryAgain = context.getString(R.string.try_again)
        val formatTryAgain = String.format(tryAgain, Constants.Second.TRY_SECOND)
        fetchPeopleJob = viewModelScope.launch {
            when (val result = personRepository.fetchPeople(nextPage)) {
                is Results.Loading -> {
                    _uiState.update {
                        it.copy(isFetching = true)
                    }
                }

                is Results.Error -> {
                    _uiState.update {
                        it.copy(
                            isFetching = false,
                            isRefreshing = false,
                            error = result.error + formatTryAgain
                        )
                    }

                    delay(Constants.Second.TRY_SECOND.toLong())
                    fetchPeople(isRefreshing, context)
                }

                is Results.Success -> {
                    val people = _uiState.value.people.toMutableList()
                    val peopleSizeOld = people.size

                    val currentPeopleIds =
                        _uiState.value.people.mapTo(HashSet(_uiState.value.people.size)) { it.id }

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
                            isFetching = false,
                            isRefreshing = false,
                            people = people,
                            noPeople = people.isEmpty(),
                            reachedEndOfThePeople = reachedEndOfThePeople,
                            willDisplayReachedEndOfThePeopleMessage = reachedEndOfThePeople,
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