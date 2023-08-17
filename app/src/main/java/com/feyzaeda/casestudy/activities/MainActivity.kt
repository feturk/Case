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

        binding.rcvPeople.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (peopleListViewModel.uiState.value.endOfThePage) {
                    return
                }


                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val lastVisibleItemPosition = layoutManager?.findLastVisibleItemPosition()

                if (lastVisibleItemPosition != null && lastVisibleItemPosition != -1
                    && lastVisibleItemPosition >= peopleListViewModel.uiState.value.peopleList.size - 3
                ) {
                    peopleListViewModel.onEvent(Event.END_OF_THE_PAGE)
                }
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                peopleListViewModel.uiState.onEach { state ->
                    if (state.endOfThePage && state.msgEndOfThePage) {
                        getString(R.string.displayed_all_data).toast(applicationContext)
                        peopleListViewModel.onEvent(
                            Event.SHOW_END_OF_THE_PAGE_MSG
                        )
                    }

                    adapter.submitList(state.peopleList)

                    binding.swpRefreshPeople.isRefreshing = state.isStateRefreshing
                    binding.loadingIndicator.isVisible = state.isStateFetching && !state.isStateRefreshing
                    binding.txtNoPeople.isVisible = state.emptyPeople

                    state.error?.let { errorMessage ->
                        errorMessage.toast(applicationContext)
                        peopleListViewModel.onEvent(Event.SHOW_ERROR_MSG)
                    }
                }.collect()
            }
        }
        binding.swpRefreshPeople.setOnRefreshListener {
            peopleListViewModel.onEvent(Event.SWIPE_REFRESH)
        }
    }
}