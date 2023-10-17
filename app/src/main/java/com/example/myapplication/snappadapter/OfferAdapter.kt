package com.example.myapplication

import android.app.Notification
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.snappadapter.FakeData
import com.example.myapplication.snappadapter.OfferSnapHelper
import com.example.myapplication.snappadapter.layoutmanager.LoopingLayoutManager
import com.example.myapplication.snappadapter.layoutmanager.TestViewHolder
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
//import cab.snapp.driver.models.data_access_layer.entities.abtest.DynamicCommissionABTests
//import cab.snapp.driver.ride.R
//import cab.snapp.driver.ride.models.entities.OfferItemClick
//import cab.snapp.driver.ride.models.entities.offer.FakeData
//import com.example.myapplication.snappadapter.layoutmanager.LoopingLayoutManager
//import cab.snapp.driver.ride.units.offer.view.OfferSnapHelper
//import cab.snapp.driver.ride.utils.FIRST_DEST_MARKER_TAG
//import cab.snapp.driver.ride.utils.ORIGIN_MARKER_TAG
//import cab.snapp.driver.ride.utils.SEC_DEST_MARKER_TAG
//import cab.snapp.mapmodule.MapModule
//import cab.snapp.report.crashlytics.CrashlyticsProvider
//import cab.snapp.report.crashlytics.CrashlyticsProviders
//import com.google.android.gms.maps.model.LatLng
//import io.reactivex.Observer
//import io.reactivex.Single
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.disposables.Disposable
//import io.reactivex.functions.Consumer
//import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * This class is an instance of [ListAdapter] for handling showing of the offers
 */
class OfferAdapter(
    /**
     * The click [Consumer] to accept click events by [OfferItemClick]
     */
//    private val clicksConsumer: Consumer<OfferItemClick>,
    /**
     * The [DynamicCommissionABTests] config should be considered while showing offers
     */
//    private var dynamicCommissionABTests: DynamicCommissionABTests,
    /**
     * The [accessibilityABTests] config should be considered while showing offers
     */
//    private var accessibilityABTests: Boolean,
    /**
     * The instance of [MapModule] that the offers should make contract with it
     */
//    private val mapModule: MapModule,
    /**
     * The id of the of Map view that the offers should make contract with it
     */
//    private var mapId: Int? = null,
    /**
     * The latest valid location of the driver
     */
//    private var driverLocation: Location? = null,
    /**
     * The desired destination coordinates set by the driver
     */
//    private var desiredDestinationCoordinates: LatLng? = null,
    /**
     * The notification [Observer] to notify notification data by [Notification]
     */
    private val notificationObserver: Observer<Notification>? = null
) : ListAdapter<FakeData, RecyclerView.ViewHolder>(OfferDiffCallback()) {

    //region Fields
    /**
     * The [OfferSnapHelper] which is used for snapping the items inside the [RecyclerView] to
     * the closest location to make the [RecyclerView] to behave like ViewPager
     */
    private var pagerSnapHelper: OfferSnapHelper? = null

    /**
     * The Crashlytics used for sending non fatal crashes
     */
//    private val crashlytics = CrashlyticsProvider.getCrashlytics()

    /**
     * The instance of the attached [RecyclerView]
     */
    private var recycler: RecyclerView? = null

    /**
     * The latest position visited by user before updating the data list items
     */
    private var lastVisitedPosition: Int = -1

    /**
     * The [RecyclerView.OnScrollListener] which is used to handle and do something when
     * the [RecyclerView] scroll value or state changes
     */
    private var scrollListener = object : RecyclerView.OnScrollListener() {
        // The flag which indicates that the scroll movement happened or
        // only the RecyclerView state is updated with the new data.
        // For example if the dx == 0 and dy == 0 condition is true,
        // none scroll movement had been happened
        private var scrollHappened = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            //Return if the current item count is 0
            if (itemCount == 0) return
            //Check the state of scrolling is [RecyclerView.SCROLL_STATE_IDLE] only.
            // otherwise [RecyclerView] is scrolling already and we should not do anything yet.
            //Also we need to check that any scroll movement happened or not by checking the [scrollHappened] flag
            if (newState == RecyclerView.SCROLL_STATE_IDLE && scrollHappened) {
                //Set the [scrollHappened] to false
                scrollHappened = false
                //Try to update the currently focused [ViewHolder] with some delay
                updateFocusedViewHolderWithDelay()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            //Return if the current item count is 0
            if (itemCount == 0) return
            //Check if dx or dy is not equal to 0, then scroll movement had been happened
            if (dx != 0 || dy != 0) {
                //Set the [scrollHappened] to true
                scrollHappened = true
                //Try to update the currently focused [ViewHolder]
                updateFocusedViewHolderInstantly()
            } else {
                //Set the [scrollHappened] to false
                scrollHappened = false
                //Here probably we are in some state that the [RecyclerView] has updated its
                // children and we should try to update the currently focused [ViewHolder]
                // with some delay to make sure that the first bound child is going to
                // be updated with this change on first state
                updateFocusedViewHolderWithDelay()
            }
        }
    }

    /**
     * The [Disposable] related to the subscription for the delay as [Single] we consider
     * before updating the focused ViewHolder
     */
    private var updateFocusedViewHolderDisposable: Disposable? = null

    //endregion

    //region Overridden Functions
    /**
     * We override this method to do something on the attached [RecyclerView]
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        //Update the value of local [RecyclerView] with the attached one
        this.recycler = recyclerView
        //Set [scrollListener] as OnScrollListener on the [RecyclerView]
        this.recycler?.addOnScrollListener(scrollListener)
        //Create a new instance of [OfferSnapHelper]
        pagerSnapHelper = OfferSnapHelper()
        //Attach [pagerSnapHelper] to the [RecyclerView]
        pagerSnapHelper?.attachToRecyclerView(this.recycler)
    }

    /**
     * We override this method to do something on the detached [RecyclerView]
     */
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        //Dispose any queued job to update view holders on focused state
        disposeUpdateFocusedViewHolder()
        //Reset map to make sure that the state of the map is correct after detaching
//        resetMap()
        //Remove [scrollListener] as OnScrollListener from the [RecyclerView]
        this.recycler?.removeOnScrollListener(scrollListener)
        //Call [destroyCallbacks] method on [pagerSnapHelper]
        pagerSnapHelper?.destroyCallbacks()
        //Set [pagerSnapHelper] to null
        pagerSnapHelper = null
        //Set the local [RecyclerView] to null
        this.recycler = null
    }

    /**
     * This method is responsible to reset the map state and remove any added marker on it by offer view holders
     */
//    private fun resetMap() {
//        mapId?.let { mapId ->
//            mapModule.clearMap(mapId)
//            mapModule.removeMarker(
//                mapId,
//                ORIGIN_MARKER_TAG
//            )
//            mapModule.removeMarker(
//                mapId,
//                FIRST_DEST_MARKER_TAG
//            )
//            mapModule.removeMarker(
//                mapId,
//                SEC_DEST_MARKER_TAG
//            )
//            mapModule.removeMarker(
//                mapId,
//                MapModule.DESIRED_DESTINATION_MARKER_TAG
//            )
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TestViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.view_offer_item, parent, false),
//            accessibilityABTests,
//            mapModule,
//            mapId,
//            driverLocation,
//            desiredDestinationCoordinates,
//            onNextOfferClick = { currentPosition ->
//                goToNextPosition(currentPosition)
//            },
//            onPreviousOfferClick = { currentPosition ->
//                goToPreviousPosition(currentPosition)
//            },
//            notificationObserver
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //Safe Cast the ViewHolder to [TestViewHolder] and call the bind method on it
        (holder as? TestViewHolder)?.bind(getItem(position))
    }

    /**
     * We override this method to make some changes on the list before submitting (including set next and previous
     * offers and create a copy from any item due to equality of their hash codes)
     */
    override fun submitList(list: List<FakeData>?) {
        //Before submitting the list data, we should update the latest visited position by the user
        lastVisitedPosition =
            (recycler?.layoutManager as? LoopingLayoutManager)?.findFirstVisibleItemPosition()
                ?: RecyclerView.NO_POSITION
        //We first set next and previous offers on each item inside the list and finally we map
        // each item to a new created copy instance
        super.submitList(list.setNextAndPreviousOffers().map { it.copy() })
    }

    /**
     * We override this method to do snapping process on the [RecyclerView] using the [pagerSnapHelper]
     * to make sure that after updating the items, the [RecyclerView] scrolls to a fully visible item
     */
    override fun onCurrentListChanged(
        previousList: MutableList<FakeData>,
        currentList: MutableList<FakeData>
    ) {
        //Get the last offer id which has been visited by user before updating the data
//        var offerId: String? = null
//        if (lastVisitedPosition in 0 until previousList.size) {
//            offerId = previousList[lastVisitedPosition].rideId
//        }
//        //Check that if the new data list contains the old offer data
//        if (offerId != null && currentList.map { it.rideId }.contains(offerId)) {
//            //Scroll to the new position dedicated for the offerId inside the new data list
//            recycler?.scrollToPosition(currentList.indexOf(currentList.filter { it.rideId == offerId }
//                .first()))
//        } else {
//            //Otherwise we try to snap the position to the closest position
//            pagerSnapHelper?.snapToTargetExistingView()
//            //And try to update the focus state of the current item view holder
//            // to make sure that it will show the correct state
//            updateFocusedViewHolderWithDelay()
//        }
    }
    //endregion

    //region Public Functions

    /**
     * This method is responsible for updating the local value of the [dynamicCommissionABTests] with the inout value
     *
     * @param dynamicCommissionABTests the dynamic commission config should be considered while showing offers
     * as [DynamicCommissionABTests] (NotNull)
     */
//    fun setDynamicCommissionABTests(dynamicCommissionABTests: DynamicCommissionABTests) {
//        this.dynamicCommissionABTests = dynamicCommissionABTests
//    }

    /**
     * @param accessibilityABTests the accessibility config should be considered while showing offers
     * as [accessibilityABTests] (NotNull)
     */
//    fun setAccessibilityABTests(accessibilityABTests: Boolean) {
//        this.accessibilityABTests = accessibilityABTests
//    }

    /**
     * This method is responsible for updating the local value of the [driverLocation] with the inout value
     *
     * @param lastLocation the latest valid location of the driver as [Location] (Nullable)
     */
//    fun setDriverLastLocation(lastLocation: Location?) {
//        this.driverLocation = lastLocation
//    }

    /**
     * This method is responsible for updating the local value of the [desiredDestinationCoordinates] with the inout value
     *
     * @param coordinates the coordinates of the DesiredDestination location set by driver as [LatLng] (Nullable)
     */
//    fun setDesiredDestinationCoordinates(coordinates: LatLng?) {
//        this.desiredDestinationCoordinates = coordinates
//    }

    /**
     * This method is responsible for handling the state of accept offer error by finding the first completely
     * visible item position inside the [recycler], safe cast it [TestViewHolder] and call the onAcceptError on it
     */
//    fun onAcceptError() {
//        //Find the first visible item position inside the [recycler]
//        val position =
//            (recycler?.layoutManager as? LoopingLayoutManager)?.findFirstVisibleItemPosition()
//        //Check if the position is not null and also it doesn't equal to [RecyclerView.NO_POSITION]
//        if (position != null && position != RecyclerView.NO_POSITION) {
//            //find the view holder by this position, safe cast it [TestViewHolder] and call the onAcceptError on it
//            (recycler?.findViewHolderForLayoutPosition(position) as? TestViewHolder)?.onAcceptError()
//        }
//    }
    //endregion

    //region Private Functions
    /**
     * This method is responsible to dispose the [updateFocusedViewHolderDisposable] if it's not disposed yet
     */
    private fun disposeUpdateFocusedViewHolder() {
        updateFocusedViewHolderDisposable?.takeIf { it.isDisposed.not() }?.dispose()
    }

    /**
     * This method is responsible for calculating the currently shown item position and
     * call the [updateFocusedViewHolderInstantly] by this position if the position isn't
     * equal to [RecyclerView.NO_POSITION]
     */
    private fun updateFocusedViewHolderWithDelay() {
        disposeUpdateFocusedViewHolder()
        updateFocusedViewHolderDisposable = Single.timer(150, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //Get the first visible position for the LayoutManager
                val position =
                    (recycler?.layoutManager as? LoopingLayoutManager)?.findFirstVisibleItemPosition()
                //Check the position is not null or equal to [RecyclerView.NO_POSITION]
                if (position != null && position != RecyclerView.NO_POSITION) {
                    //Call the [updateFocusedViewHolderInstantly] method to update the focused ViewHolder
                    updateFocusedViewHolderInstantly(position)
                }
            }, {})
    }


    /**
     * This method is responsible for updating the focused [TestViewHolder] by the input
     * position and by finding it by calling [RecyclerView.findViewHolderForLayoutPosition] method
     *
     * @param position the position of the ViewHolder that is focused right now as [Int] (NotNull).
     * The default value is [RecyclerView.NO_POSITION]
     */
    private fun updateFocusedViewHolderInstantly(position: Int = RecyclerView.NO_POSITION) {

//        //Check if all the items count is 1
//        if (itemCount == 1) {
//            //Update the only existent view holder focused state to true
//            (recycler?.findViewHolderForLayoutPosition(0) as? TestViewHolder)?.onFocusChanged(true)
//            lastVisitedPosition = 0
//        } else if (position != RecyclerView.NO_POSITION) {
//            //Check if the position not equals to RecyclerView.NO_POSITION
//            //Update the focused state to true on the view holder dedicated with the position
//            (recycler?.findViewHolderForLayoutPosition(position) as? TestViewHolder)?.onFocusChanged(
//                true
//            )
//            lastVisitedPosition = position
//        } else {
//            //Otherwise the position is RecyclerView.NO_POSITION and we should update
//            // all of the view holders focused state to false
//            for (i in 0 until itemCount) {
//                (recycler?.findViewHolderForLayoutPosition(i) as? TestViewHolder)?.onFocusChanged(
//                    false
//                )
//            }
//        }
    }

    /**
     * This method is responsible to scroll the [recycler] to the next position
     *
     * @param currentPosition the current position of the item inside the [recycler]
     */
    private fun goToNextPosition(currentPosition: Int) {
        try {
            //Check if the current position is the last item position
            if (currentPosition == itemCount - 1) {
                when (itemCount) {
                    //If the item count is 1 return
                    1 -> return
                    //Otherwise try to scroll to the first position
                    else -> recycler?.smoothScrollToPosition(0)
                }
            } else {
                //Here we should scroll to an upper position
                recycler?.smoothScrollToPosition(currentPosition + 1)
            }
        } catch (e: Exception) {
            //Log any non fatal exception happened during the scroll process
//            crashlytics.logNonFatalException(
//                e,
//                CrashlyticsProviders.AppMetrica,
//                CrashlyticsProviders.Firebase
//            )
        }
    }

    /**
     * This method is responsible to scroll the [recycler] to the previous position
     *
     * @param currentPosition the current position of the item inside the [recycler]
     */
    private fun goToPreviousPosition(currentPosition: Int) {
        try {
            //Check if the current position is the first item position
            if (currentPosition == 0) {
                when (itemCount) {
                    //If the item count is 1 return
                    1 -> return
                    //Otherwise try to scroll to the last position
                    else -> recycler?.smoothScrollToPosition(itemCount - 1)
                }
            } else {
                //Here we should scroll to a lower position
                recycler?.smoothScrollToPosition(currentPosition - 1)
            }
        } catch (e: Exception) {
            //Log any non fatal exception happened during the scroll process
//            crashlytics.logNonFatalException(
//                e,
//                CrashlyticsProviders.AppMetrica,
//                CrashlyticsProviders.Firebase
//            )
        }
    }
    //endregion

    //region Extension Functions
    /**
     * This method is an extension function to set the Next and Previous offer on each item inside the List
     */
    private fun List<FakeData>?.setNextAndPreviousOffers(): List<FakeData> {
        //Create a new list of [FakeData]
        val newItems = mutableListOf<FakeData>()
        //If the input list is empty or null, return the empty list
        if (this.isNullOrEmpty()) return newItems
        //Loop over the input list
        this.forEachIndexed { index, offerEntity ->
            when (index) {
                //If the index is 0
                0 -> {
                    //Check the size of the list
                    when (this.size) {
                        //If the size equals to 1, the Next and Previous offer should be null
                        1 -> {
                            offerEntity.nextOffer = null
                            offerEntity.previousOffer = null
                        }
                        //If the size equals to 1, the Next offer is the item of the upper index
                        // and the Previous offer should be null
                        2 -> {
                            offerEntity.nextOffer = this[index + 1]
                            offerEntity.previousOffer = null
                        }
                        //Otherwise the Next offer is the item of the upper index
                        // and the Previous offer is the last item
                        else -> {
                            offerEntity.nextOffer = this[index + 1]
                            offerEntity.previousOffer = this.last()
                        }
                    }
                }
                //If the index is the last index
                this.size - 1 -> {
                    when (this.size) {
                        //If the size equals to 1, the Next and Previous offer should be null
                        1 -> {
                            offerEntity.nextOffer = null
                            offerEntity.previousOffer = null
                        }
                        //If the size equals to 1, the Next offer is the item of the lower index
                        // and the Previous offer should be null
                        2 -> {
                            offerEntity.nextOffer = this[index - 1]
                            offerEntity.previousOffer = null
                        }
                        //Otherwise the Next offer is the first item
                        // and the Previous offer is the item of the lower index
                        else -> {
                            offerEntity.nextOffer = this.first()
                            offerEntity.previousOffer = this[index - 1]
                        }
                    }
                }
                else -> {
                    //Otherwise the Next offer is the item of the upper index
                    // and the Previous offer is the item of the lower index
                    offerEntity.nextOffer = this[index + 1]
                    offerEntity.previousOffer = this[index - 1]
                }
            }
            //Add item with index to the new items list
            newItems.add(index, offerEntity)
        }
        //return the new items list
        return newItems
    }
    //endregion
}

//region Inner Classes
/**
 * An internal class which is used by the [OfferAdapter] to detect the changes on each item inside the offers list
 */
internal class OfferDiffCallback : DiffUtil.ItemCallback<FakeData>() {

    override fun areItemsTheSame(oldItem: FakeData, newItem: FakeData): Boolean {
        return oldItem.color == newItem.color
    }

    override fun areContentsTheSame(oldItem: FakeData, newItem: FakeData): Boolean {
        return oldItem.nextOffer == newItem.nextOffer
                && oldItem.previousOffer == newItem.previousOffer
    }

}
//endregion