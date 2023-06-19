package eu.darken.sdmse.systemcleaner.ui.details

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.sdmse.common.SingleLiveEvent
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.navigation.navArgs
import eu.darken.sdmse.common.progress.Progress
import eu.darken.sdmse.common.uix.ViewModel3
import eu.darken.sdmse.systemcleaner.core.FilterContent
import eu.darken.sdmse.systemcleaner.core.SystemCleaner
import eu.darken.sdmse.systemcleaner.core.filter.FilterIdentifier
import eu.darken.sdmse.systemcleaner.core.hasData
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FilterContentDetailsFragmentVM @Inject constructor(
    @Suppress("unused") private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    systemCleaner: SystemCleaner,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {
    private val args by handle.navArgs<FilterContentDetailsFragmentArgs>()

    init {
        systemCleaner.data
            .filter { !it.hasData }
            .take(1)
            .onEach {
                popNavStack()
            }
            .launchInViewModel()
    }

    val events = SingleLiveEvent<FilterContentDetailsEvents>()

    val state = combine(
        systemCleaner.progress,
        systemCleaner.data
            .filterNotNull()
            .distinctUntilChangedBy { data ->
                data.filterContents.map { it.filterIdentifier }.toSet()
            },
    ) { progress, data ->
        State(
            items = data.filterContents.toList(),
            target = args.filterIdentifier,
            progress = progress,
        )
    }.asLiveData2()

    data class State(
        val items: List<FilterContent>,
        val target: FilterIdentifier?,
        val progress: Progress.Data?,
    )

    companion object {
        private val TAG = logTag("SystemCleaner", "Details", "Fragment", "VM")
    }
}