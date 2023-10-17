package com.example.myapplication.snappadapter

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min

internal class OfferSnapHelper: RecyclerView.OnFlingListener() {

    private val MAX_SCROLL_ON_FLING_DURATION = 100 // ms

    private val MILLISECONDS_PER_INCH = 100f

    private var recyclerView: RecyclerView? = null


    private var mVerticalHelper: OrientationHelper? = null

    private var mHorizontalHelper: OrientationHelper? = null

    private val scrollListener = object : RecyclerView.OnScrollListener() {

        private var scrollHappened = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == RecyclerView.SCROLL_STATE_IDLE && scrollHappened) {
                scrollHappened = false
                snapToTargetExistingView()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(dx != 0 || dy != 0) {
                scrollHappened = true
            }
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
        this.recyclerView?.addOnScrollListener(scrollListener)
        this.recyclerView?.onFlingListener = this
        if(this.recyclerView != null) {
            snapToTargetExistingView()
        }
    }

    fun destroyCallbacks() {
        this.recyclerView?.removeOnScrollListener(scrollListener)
        this.recyclerView = null
        this.recyclerView?.onFlingListener = null
    }



    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
        val layoutManager = recyclerView?.layoutManager ?: return false
        val minFlingVelocity = recyclerView?.minFlingVelocity ?: return false
        return ((abs(velocityY) > minFlingVelocity || abs(velocityX) > minFlingVelocity)
                && snapFromFling(layoutManager, velocityX, velocityY))
    }

    private fun snapFromFling(
        layoutManager: RecyclerView.LayoutManager, velocityX: Int,
        velocityY: Int
    ): Boolean {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return false
        }
        val targetPosition: Int = findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (targetPosition == RecyclerView.NO_POSITION) {
            return false
        }
        recyclerView?.smoothScrollToPosition(targetPosition)
        return true
    }

    fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager, velocityX: Int,
        velocityY: Int
    ): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val orientationHelper: OrientationHelper = getOrientationHelper(layoutManager)
            ?: return RecyclerView.NO_POSITION

        // A child that is exactly in the center is eligible for both before and after
        var closestChildBeforeCenter: View? = null
        var distanceBefore = Int.MIN_VALUE
        var closestChildAfterCenter: View? = null
        var distanceAfter = Int.MAX_VALUE

        // Find the first view before the center, and the first view after the center
        val childCount = layoutManager.childCount
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val distance: Int = distanceToCenter(child, orientationHelper)
            if (distance <= 0 && distance > distanceBefore) {
                // Child is before the center and closer then the previous best
                distanceBefore = distance
                closestChildBeforeCenter = child
            }
            if (distance >= 0 && distance < distanceAfter) {
                // Child is after the center and closer then the previous best
                distanceAfter = distance
                closestChildAfterCenter = child
            }
        }

        // Return the position of the first child from the center, in the direction of the fling
        val forwardDirection: Boolean = isForwardFling(layoutManager, velocityX, velocityY)
        if (forwardDirection && closestChildAfterCenter != null) {
            return layoutManager.getPosition(closestChildAfterCenter)
        } else if (!forwardDirection && closestChildBeforeCenter != null) {
            return layoutManager.getPosition(closestChildBeforeCenter)
        }

        // There is no child in the direction of the fling. Either it doesn't exist (start/end of
        // the list), or it is not yet attached (very rare case when children are larger then the
        // viewport). Extrapolate from the child that is visible to get the position of the view to
        // snap to.
        val visibleView =
            (if (forwardDirection) closestChildBeforeCenter else closestChildAfterCenter)
                ?: return RecyclerView.NO_POSITION
        val visiblePosition = layoutManager.getPosition(visibleView)
        val snapToPosition = (visiblePosition
                + if (isReverseLayout(layoutManager) == forwardDirection) -1 else +1)
        return if (snapToPosition < 0 || snapToPosition >= itemCount) {
            RecyclerView.NO_POSITION
        } else snapToPosition
    }

    private fun isForwardFling(
        layoutManager: RecyclerView.LayoutManager, velocityX: Int,
        velocityY: Int
    ): Boolean {
        return if (layoutManager.canScrollHorizontally()) {
            velocityX > 0
        } else {
            velocityY > 0
        }
    }

    private fun isReverseLayout(layoutManager: RecyclerView.LayoutManager): Boolean {
        val itemCount = layoutManager.itemCount
        if (layoutManager is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            val vectorProvider = layoutManager as RecyclerView.SmoothScroller.ScrollVectorProvider
            val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
            if (vectorForEnd != null) {
                return vectorForEnd.x < 0 || vectorForEnd.y < 0
            }
        }
        return false
    }

    protected fun createScroller(
        layoutManager: RecyclerView.LayoutManager
    ): RecyclerView.SmoothScroller? {
        return if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(recyclerView?.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                if(recyclerView?.layoutManager == null) return
                val snapDistances: IntArray = calculateDistanceToFinalSnap(
                    recyclerView!!.layoutManager!!,
                    targetView
                )
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }

            override fun calculateTimeForScrolling(dx: Int): Int {
                return min(
                    MAX_SCROLL_ON_FLING_DURATION,
                    super.calculateTimeForScrolling(dx)
                )
            }
        }
    }

    private fun distanceToCenter(targetView: View, helper: OrientationHelper): Int {
        val childCenter = (helper.getDecoratedStart(targetView)
                + helper.getDecoratedMeasurement(targetView) / 2)
        val containerCenter = helper.startAfterPadding + helper.totalSpace / 2
        return childCenter - containerCenter
    }

    private fun getOrientationHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        return when {
            layoutManager.canScrollVertically() -> {
                getVerticalHelper(layoutManager)
            }
            layoutManager.canScrollHorizontally() -> {
                getHorizontalHelper(layoutManager)
            }
            else -> {
                null
            }
        }
    }

    fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(
                targetView,
                getHorizontalHelper(layoutManager)
            )
        } else {
            out[0] = 0
        }
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToCenter(
                targetView,
                getVerticalHelper(layoutManager)
            )
        } else {
            out[1] = 0
        }
        return out
    }

    fun snapToTargetExistingView() {
        if (recyclerView == null) {
            return
        }
        val layoutManager = recyclerView?.layoutManager ?: return
        val snapView = findSnapView(layoutManager) ?: return
        val snapPosition = layoutManager.getPosition(snapView)
        if (snapPosition != RecyclerView.NO_POSITION) {
            recyclerView?.smoothScrollToPosition(snapPosition)
        }
    }

    fun snapToTargetExistingViewIfNeeded(): Boolean {
        if (recyclerView == null) {
            return false
        }
        val layoutManager = recyclerView?.layoutManager ?: return false
        if(layoutManager.itemCount < 1) return false
        val snapView = findSnapView(layoutManager) ?: return false
        val distance = calculateDistanceToFinalSnap(layoutManager,snapView)
        if(abs(distance[0]) == 0 && abs(distance[1]) == 0) return false
        val snapPosition = layoutManager.getPosition(snapView)
        if (snapPosition != RecyclerView.NO_POSITION) {
            recyclerView?.smoothScrollToPosition(snapPosition)
            return true
        } else {
            return false
        }
    }

    fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if (layoutManager?.canScrollVertically() == true) {
            return findCenterView(layoutManager, getVerticalHelper(layoutManager))
        } else if (layoutManager?.canScrollHorizontally() == true) {
            return findCenterView(layoutManager, getHorizontalHelper(layoutManager))
        }
        return null
    }

    private fun findCenterView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }
        var closestChild: View? = null
        val center = helper.startAfterPadding + helper.totalSpace / 2
        var absClosest = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = (helper.getDecoratedStart(child)
                    + helper.getDecoratedMeasurement(child) / 2)
            val absDistance = Math.abs(childCenter - center)

            /* if child center is closer than previous closest, set it as closest  */if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mVerticalHelper == null || mVerticalHelper?.layoutManager !== layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper!!
    }

    private fun getHorizontalHelper(
        layoutManager: RecyclerView.LayoutManager
    ): OrientationHelper {
        if (mHorizontalHelper == null || mHorizontalHelper?.layoutManager !== layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper!!
    }
}