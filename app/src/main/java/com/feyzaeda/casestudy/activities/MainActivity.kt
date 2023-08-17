package com.feyzaeda.casestudy.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feyzaeda.casestudy.R
import com.feyzaeda.casestudy.adapters.PeopleAdapter
import com.feyzaeda.casestudy.databinding.ActivityMainBinding
import com.feyzaeda.casestudy.util.Event
import com.feyzaeda.casestudy.util.toast
import com.feyzaeda.casestudy.viewmodel.PeopleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val peopleListViewModel: PeopleViewModel by viewModels()
    private val adapter = PeopleAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.rcvPeople.adapter = adapter

        peopleListViewModel.fetchPeople(context = applicationContext)

        binding.rcvPeople.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (peopleListViewModel.uiState.value.reachedEndOfThePeople) {
                    return
                }


                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val lastVisibleItemPosition = layoutManager?.findLastVisibleItemPosition()

                if (lastVisibleItemPosition != null && lastVisibleItemPosition != -1
                    && lastVisibleItemPosition >= peopleListViewModel.uiState.value.people.size - 3
                ) {
                    peopleListViewModel.onEvent(Event.END_OF_THE_PAGE, applicationContext)
                }
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                peopleListViewModel.uiState.onEach { state ->
                    if (state.reachedEndOfThePeople && state.willDisplayReachedEndOfThePeopleMessage) {
                        getString(R.string.displayed_all_data).toast(applicationContext)
                        peopleListViewModel.onEvent(
                            Event.SHOW_END_OF_THE_PAGE_MSG,
                            applicationContext
                        )
                    }

                    adapter.submitList(state.people)

                    binding.swpRefreshPeople.isRefreshing = state.isRefreshing
                    binding.loadingIndicator.isVisible = state.isFetching && !state.isRefreshing
                    binding.txtNoPeople.isVisible = state.noPeople

                    state.error?.let { errorMessage ->
                        errorMessage.toast(applicationContext)
                        peopleListViewModel.onEvent(Event.SHOW_ERROR_MSG, applicationContext)
                    }
                }.collect()
            }
        }
        binding.swpRefreshPeople.setOnRefreshListener {
            peopleListViewModel.onEvent(Event.SWIPE_REFRESH, applicationContext)
        }
    }
}