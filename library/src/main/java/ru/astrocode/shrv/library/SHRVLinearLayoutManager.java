package ru.astrocode.shrv.library;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Astrocode on 26.03.2017.
 * <p>
 * Sticky header linear layout manager.
 */
public class SHRVLinearLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private final static String ERROR_UNKNOWN_ORIENTATION = "Unknown orientation!";
    private final static String ERROR_WRONG_CLASS_TYPE = "Wrong class type!";
    private final static String ERROR_UNKNOWN_DESTINATION_CODE = "Unknown destination code!";

    private final static String TAG_HEADERS_INDEXES = "TAG_HEADERS_INDEXES";
    private final static String TAG_START_POINT_OF_FIRST_ITEM = "TAG_START_POINT_OF_FIRST_ITEM";
    private final static String TAG_START_POINT_OF_CURRENT_HEADER = "TAG_START_POINT_OF_CURRENT_HEADER";
    private final static String TAG_ADAPTER_INDEX_OF_FIRST_ITEM = "TAG_ADAPTER_INDEX_OF_FIRST_ITEM";
    private final static String TAG_ADAPTER_INDEX_OF_CURRENT_HEADER = "TAG_ADAPTER_INDEX_OF_CURRENT_HEADER";

    private final static int sStart = 0, sEnd = 1;

    public final static int VERTICAL = OrientationHelper.VERTICAL, HORIZONTAL = OrientationHelper.HORIZONTAL;

    private int mStartPointOfFirstItem, mStartPointOfCurrentHeader;
    private int mAdapterIndexOfFirstItem, mAdapterIndexOfCurrentHeader;
    private int mScrollToPosition;

    private int mOrientation;

    private OrientationHelper mOrientationHelper;
    private ArrayList<Integer> mHeadersIndexes;

    private RecyclerView.Adapter mAdapter;


    public SHRVLinearLayoutManager(int orientation) {
        setOrientation(orientation);
        mHeadersIndexes = new ArrayList<>();

        mStartPointOfFirstItem = -1;
        mAdapterIndexOfFirstItem = 0;

        mStartPointOfCurrentHeader = -1;
        mAdapterIndexOfCurrentHeader = -1;

        mScrollToPosition = -1;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        mAdapter = view.getAdapter();
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        mAdapter = null;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        detachAndScrapAttachedViews(recycler);

        if(mScrollToPosition != -1 && !isTargetViewInVisibleArea(mScrollToPosition)) {

            if (mScrollToPosition < mAdapterIndexOfFirstItem) {
                fillViewFromScrollPositionToEnd(recycler);
            } else if (mScrollToPosition > mAdapterIndexOfFirstItem) {
                fillViewFromScrollPositionToStart(recycler);
            }

            rememberCurrentState();

        }else {

            if (mAdapterIndexOfCurrentHeader != -1) {
                addViewTo(sEnd, mAdapterIndexOfCurrentHeader, 0, mStartPointOfCurrentHeader, recycler);
            }

            int currentCoordinate = mStartPointOfFirstItem == -1 ? mOrientationHelper.getStartAfterPadding()
                    : mStartPointOfFirstItem;

            for (int i = mAdapterIndexOfFirstItem; i < getItemCount(); i++) {
                View child = addViewTo(sEnd, i, 0, currentCoordinate, recycler);

                currentCoordinate = mOrientationHelper.getDecoratedEnd(child);

                if (currentCoordinate >= mOrientationHelper.getEndAfterPadding()) {
                    break;
                }
            }
        }
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        mScrollToPosition = -1;
    }

    private boolean isTargetViewInVisibleArea(int targetViewAdapterPosition){
        boolean isInVisibleArea = false;
        int childCount = getChildCount();

        if(childCount > 0){
            View firstChild = getChildAt(0),lastChild = getChildAt(childCount-1),preLastChild;

            int biggestAdapterPosition = getPosition(firstChild);
            int lowestAdapterPosition = getPosition(lastChild);

            if(childCount > 1){
                if(getItemViewType(lastChild) == SHRVItemType.TYPE_HEADER) {
                    preLastChild = getChildAt(childCount-2);

                    if(Math.abs(getPosition(lastChild) - getPosition(preLastChild)) > 1){
                        lowestAdapterPosition = getPosition(preLastChild);
                    }
                }
            }

            isInVisibleArea = targetViewAdapterPosition <= biggestAdapterPosition && targetViewAdapterPosition >= lowestAdapterPosition;
        }

        return isInVisibleArea;
    }

    private int findCurrentHeaderFromBottom(int currentTopViewAdapterPosition){
        int currentHeaderPosition = mAdapterIndexOfCurrentHeader == -1?0:mAdapterIndexOfCurrentHeader;

        for (int i = currentHeaderPosition; i <= currentTopViewAdapterPosition; i++) {
            if(mAdapter.getItemViewType(i) == SHRVItemType.TYPE_HEADER){
                if(currentHeaderPosition != i){
                    mHeadersIndexes.add(currentHeaderPosition);
                }
                currentHeaderPosition = i;
            }
        }

        return currentHeaderPosition;
    }

    private void fillViewFromScrollPositionToStart(RecyclerView.Recycler recycler){
        int currentCoordinate = mOrientationHelper.getEndAfterPadding();

        for(int i = mScrollToPosition; i >= 0; i--) {
            View child = addViewTo(sStart, i, -1, currentCoordinate, recycler);
            currentCoordinate = mOrientationHelper.getDecoratedStart(child);

            if (currentCoordinate <= mOrientationHelper.getStartAfterPadding()) {
                mAdapterIndexOfCurrentHeader = findCurrentHeaderFromBottom(i);

                if(mAdapter.getItemViewType(i + 1) == SHRVItemType.TYPE_HEADER){
                    if(mAdapterIndexOfCurrentHeader != i){
                        addViewTo(sEnd,mAdapterIndexOfCurrentHeader,-1,mOrientationHelper.getDecoratedStart(child),recycler);
                    }
                }else {
                    if(mAdapterIndexOfCurrentHeader == i){
                        mOrientationHelper.offsetChild(child,-mOrientationHelper.getDecoratedStart(child));
                    }else {
                        addViewTo(sEnd,mAdapterIndexOfCurrentHeader,-1,mOrientationHelper.getStartAfterPadding(),recycler);
                    }
                }
                break;
            }
        }
    }

    private int findCurrentHeaderFromTop(int currentTopViewAdapterPosition){
        int currentHeaderPosition = mAdapterIndexOfCurrentHeader;

        for (int i = mAdapterIndexOfCurrentHeader; i >= 0; i--) {
            if (mHeadersIndexes.remove((Integer) i)) {
                currentHeaderPosition = i;
                if (i <= currentTopViewAdapterPosition) {
                    break;
                }
            }
        }

        return  currentHeaderPosition;
    }

    private void fillViewFromScrollPositionToEnd(RecyclerView.Recycler recycler){
        int currentCoordinate = mOrientationHelper.getStartAfterPadding();

        for(int i = mScrollToPosition; i < getItemCount(); i++) {
            View child = addViewTo(sEnd, i, 0, currentCoordinate, recycler);
            currentCoordinate = mOrientationHelper.getDecoratedEnd(child);

            if(i == mScrollToPosition) {
                mAdapterIndexOfCurrentHeader = findCurrentHeaderFromTop(i);

                if (mAdapterIndexOfCurrentHeader != i) {
                    child = addViewTo(sEnd, mAdapterIndexOfCurrentHeader, -1, mOrientationHelper.getStartAfterPadding(), recycler);
                    for (int j = getChildCount() - 2; j >= 0; j--) {
                        mOrientationHelper.offsetChild(getChildAt(j),mOrientationHelper.getDecoratedMeasurement(child));
                    }
                    currentCoordinate += mOrientationHelper.getDecoratedMeasurement(child);
                }
            }

            if (currentCoordinate >= mOrientationHelper.getEndAfterPadding()) {
                break;
            }
        }
    }

    /**
     * Change orientation of the layout manager
     *
     * @param orientation New orientation.
     */
    public void setOrientation(int orientation) {

        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(ERROR_UNKNOWN_ORIENTATION);
        }

        assertNotInLayoutOrScroll(null);

        if (orientation == mOrientation) {
            if (mOrientationHelper == null) {
                mOrientationHelper = OrientationHelper.createOrientationHelper(this, orientation);
            }
            return;
        }

        mOrientation = orientation;
        mOrientationHelper = OrientationHelper.createOrientationHelper(this, orientation);

        requestLayout();
    }

    private void rememberCurrentState(){
        View firstView = getChildAt(getChildCount() - 1);

        if (getItemViewType(firstView) == SHRVItemType.TYPE_HEADER) {
            mAdapterIndexOfCurrentHeader = getPosition(firstView);
            mStartPointOfCurrentHeader = mOrientationHelper.getDecoratedStart(firstView);

            View secondView = getChildAt(getChildCount() - 2);
            if (secondView != null) {
                mAdapterIndexOfFirstItem = getPosition(secondView);
                mStartPointOfFirstItem = mOrientationHelper.getDecoratedStart(secondView);
            }
        }else{
            mAdapterIndexOfFirstItem = getPosition(firstView);
            mStartPointOfFirstItem = mOrientationHelper.getDecoratedStart(firstView);
        }
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int offset = 0;
        int childCount = getChildCount();

        if (childCount > 0) {
            if (dy > 0) {
                offset = addViewsToEnd(dy, recycler);

                if (offset > 0) {
                    offsetToStart(offset);
                }
            } else if (dy < 0) {
                offset = addViewsToStart(dy, recycler);

                if (offset < 0) {
                    offsetToEnd(offset);
                }
            }else{
                return offset;
            }
            deleteInvisibleViews(recycler);
            rememberCurrentState();
        }

        return offset;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int offset = 0;
        int childCount = getChildCount();

        if (childCount > 0) {
            if (dx > 0) {
                offset = addViewsToEnd(dx, recycler);

                if (offset > 0) {
                    offsetToStart(offset);
                }

            } else if (dx < 0) {
                offset = addViewsToStart(dx, recycler);

                if (offset < 0) {
                    offsetToEnd(offset);
                }
            }else {
                return offset;
            }
            deleteInvisibleViews(recycler);
            rememberCurrentState();
        }

        return offset;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle data = new Bundle();

        if (mHeadersIndexes.size() > 0) {
            data.putIntegerArrayList(TAG_HEADERS_INDEXES, mHeadersIndexes);
        }

        data.putInt(TAG_START_POINT_OF_FIRST_ITEM, mStartPointOfFirstItem);
        data.putInt(TAG_ADAPTER_INDEX_OF_FIRST_ITEM, mAdapterIndexOfFirstItem);

        data.putInt(TAG_START_POINT_OF_CURRENT_HEADER, mStartPointOfCurrentHeader);
        data.putInt(TAG_ADAPTER_INDEX_OF_CURRENT_HEADER, mAdapterIndexOfCurrentHeader);

        return data;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle data = (Bundle) state;
            if (data.containsKey(TAG_HEADERS_INDEXES)) {
                mHeadersIndexes = data.getIntegerArrayList(TAG_HEADERS_INDEXES);
            }

            mStartPointOfFirstItem = data.getInt(TAG_START_POINT_OF_FIRST_ITEM);
            mAdapterIndexOfFirstItem = data.getInt(TAG_ADAPTER_INDEX_OF_FIRST_ITEM);

            mStartPointOfCurrentHeader = data.getInt(TAG_START_POINT_OF_CURRENT_HEADER);
            mAdapterIndexOfCurrentHeader = data.getInt(TAG_ADAPTER_INDEX_OF_CURRENT_HEADER);
        } else {
            throw new ClassCastException(ERROR_WRONG_CLASS_TYPE);
        }
    }

    /**
     * Add views (if need ) to the end of the recyclerView and return maximum possible offset(not above the original offset).
     *
     * @param offset   Original offset.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get view.
     * @return
     */
    private int addViewsToEnd(int offset, RecyclerView.Recycler recycler) {
        View currentLastView = getChildAt(0);

        int currentAvailableOffset = mOrientationHelper.getDecoratedEnd(currentLastView) > mOrientationHelper.getEndAfterPadding()
                ? mOrientationHelper.getDecoratedEnd(currentLastView) - mOrientationHelper.getEndAfterPadding() : 0;
        int currentAdapterIndex = getPosition(currentLastView) + 1;

        while (currentAdapterIndex <= getItemCount() - 1 && currentAvailableOffset < offset) {
            View newView = addViewTo(sEnd, currentAdapterIndex, 0, mOrientationHelper.getDecoratedEnd(currentLastView), recycler);

            currentLastView = newView;
            currentAvailableOffset += mOrientationHelper.getDecoratedMeasurement(newView);

            currentAdapterIndex++;
        }

        return currentAvailableOffset > offset ? offset : currentAvailableOffset;
    }

    /**
     * Scroll content to end(bottom or right).
     *
     * @param offset   The value to which we must scroll the content.
     */
    private void offsetToEnd(int offset) {
        final int startPadding = mOrientationHelper.getStartAfterPadding();

        View lastSectionView = null, endSectionView = null;
        int currentViewIndex = 0;

        while (currentViewIndex < getChildCount()) {
            View currentView = getChildAt(currentViewIndex);
            int currentViewOffset = -offset;

            if (getItemViewType(currentView) == SHRVItemType.TYPE_HEADER) {
                if (lastSectionView != null && endSectionView != null) {
                    final int currentViewStart = mOrientationHelper.getDecoratedStart(currentView);
                    final int currentViewEnd = mOrientationHelper.getDecoratedEnd(currentView);

                    if (currentViewStart == startPadding) {
                        final int lastSectionStart = mOrientationHelper.getDecoratedStart(lastSectionView);

                        if (lastSectionStart >= currentViewEnd) {
                            currentViewOffset = lastSectionStart - currentViewEnd;
                        } else {
                            currentViewOffset = 0;
                        }
                    }else if(currentViewStart < startPadding){
                        final int currentViewMeasurement = mOrientationHelper.getDecoratedMeasurement(currentView);
                        final int endSectionEnd = mOrientationHelper.getDecoratedEnd(endSectionView);

                        if(endSectionEnd > startPadding + currentViewMeasurement){
                            currentViewOffset = startPadding - currentViewStart;
                        }else {
                            currentViewOffset = endSectionEnd - currentViewEnd;
                        }
                    }
                }
                endSectionView = null;
            } else {
                lastSectionView = currentView;
                if (endSectionView == null) {
                    endSectionView = lastSectionView;
                }
            }
            mOrientationHelper.offsetChild(currentView, currentViewOffset);

            currentViewIndex++;
        }
    }

    /**
     * Add views (if need ) to the start of the recyclerView and return maximum possible offset(not below the original offset).
     *
     * @param offset   Original offset.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get view.
     * @return
     */
    private int addViewsToStart(int offset, RecyclerView.Recycler recycler) {
        View currentHeader = null, currentFirstView = getChildAt(getChildCount() - 1);

        int currentStart = mOrientationHelper.getDecoratedStart(currentFirstView);

        int currentAvailableOffset = currentStart;
        int currentAdapterIndex = getPosition(currentFirstView) - 1;


        if (getItemViewType(currentFirstView) == SHRVItemType.TYPE_HEADER) {
            View subViewHeader = getChildAt(getChildCount() - 2);

            if (subViewHeader != null && getItemViewType(subViewHeader) != SHRVItemType.TYPE_HEADER) {
                if (getPosition(subViewHeader) - getPosition(currentFirstView) == 1) {
                    currentAvailableOffset = mOrientationHelper.getDecoratedStart(subViewHeader) - (mOrientationHelper.getDecoratedMeasurement(currentFirstView) + mOrientationHelper.getStartAfterPadding());
                    currentStart = mOrientationHelper.getDecoratedStart(subViewHeader) - mOrientationHelper.getDecoratedMeasurement(currentFirstView);
                } else {
                    currentAdapterIndex = getPosition(subViewHeader) - 1;
                    currentAvailableOffset = currentStart = mOrientationHelper.getDecoratedStart(subViewHeader);
                }
            }
            currentHeader = currentFirstView;
        }

        while (currentAdapterIndex >= 0 && currentAvailableOffset > offset) {
            if (currentHeader != null) {
                if (getPosition(currentHeader) > currentAdapterIndex) {
                    if (mHeadersIndexes.size() > 0) {
                        currentHeader = addViewTo(sStart, mHeadersIndexes.remove(mHeadersIndexes.size() - 1), -1, currentStart, recycler);
                    } else {
                        currentHeader = null;
                    }
                } else if (getPosition(currentHeader) == currentAdapterIndex) {
                    currentAvailableOffset -= mOrientationHelper.getDecoratedMeasurement(currentHeader);
                    currentStart -= mOrientationHelper.getDecoratedMeasurement(currentHeader);
                    currentAdapterIndex--;
                    continue;
                }
            }

            View newView = addViewTo(sStart, currentAdapterIndex, currentHeader != null ? getChildCount() - 1 : -1, currentStart, recycler);

            currentStart = mOrientationHelper.getDecoratedStart(newView);
            currentAvailableOffset -= mOrientationHelper.getDecoratedMeasurement(newView);
            currentAdapterIndex--;
        }

        return currentAvailableOffset < offset ? offset : currentAvailableOffset;
    }

    /**
     * Scroll content to start(top or left).
     *
     * @param offset   The value to which we must scroll the content.
     */
    private void offsetToStart(int offset) {
        final int startPadding = mOrientationHelper.getStartAfterPadding();

        View currentHeader = null;
        int currentViewIndex = getChildCount() - 1;

        while (currentViewIndex >= 0) {
            View currentView = getChildAt(currentViewIndex);
            int currentViewOffset = -offset;

            if (getItemViewType(currentView) == SHRVItemType.TYPE_HEADER) {
                final int currentViewStart = mOrientationHelper.getDecoratedStart(currentView);
                final int currentViewFutureStart = currentViewStart - offset;

                if (currentHeader != null) {
                    final int currentHeaderEnd = mOrientationHelper.getDecoratedEnd(currentHeader);

                    if (currentViewFutureStart <= startPadding) {
                        currentViewOffset = startPadding - currentViewStart;

                        mOrientationHelper.offsetChild(currentHeader,-currentHeaderEnd);

                        currentHeader = currentView;
                    } else {
                        if (currentViewFutureStart < currentHeaderEnd) {
                            mOrientationHelper.offsetChild(currentHeader,-(currentHeaderEnd - currentViewFutureStart));
                        }
                    }
                } else {
                    if (currentViewIndex == getChildCount() - 1) {
                        currentViewOffset = 0;
                        currentHeader = currentView;
                    } else {
                        if (currentViewFutureStart <= startPadding) {
                            currentViewOffset = startPadding - currentViewStart;
                            currentHeader = currentView;
                        }
                    }
                }
            }
            mOrientationHelper.offsetChild(currentView, currentViewOffset);

            currentViewIndex--;
        }
    }

    /**
     * Delete all invisible views.
     *
     * @param recycler
     */
    private void deleteInvisibleViews(RecyclerView.Recycler recycler) {
        final int startPadding = mOrientationHelper.getStartAfterPadding();
        final int endPadding = mOrientationHelper.getEndAfterPadding();

        int currentViewIndex = 0;

        while (currentViewIndex < getChildCount()) {
            View view = getChildAt(currentViewIndex);

            final int currentViewEnd = mOrientationHelper.getDecoratedEnd(view);
            final int currentViewStart = mOrientationHelper.getDecoratedStart(view);

            if (currentViewEnd <= startPadding) {
                if (getItemViewType(view) == SHRVItemType.TYPE_HEADER) {
                    mHeadersIndexes.add(getPosition(view));
                }
                removeAndRecycleView(view, recycler);
            } else if (currentViewStart >= endPadding) {
                removeAndRecycleView(view, recycler);
            } else {
                currentViewIndex++;
            }
        }
    }

    /**
     * Add new(or cached) view to recyclerView.
     *
     * @param destination     Code indicating where to add a view.
     * @param adapterPosition Adapter position of  the view.
     * @param layoutPosition  Layout position of the view.
     * @param value           Top or left(depend on destination code) coordinate of the view.
     * @param recycler        {@link android.support.v7.widget.RecyclerView.Recycler} to get view.
     * @return {@link View}.
     */
    private View addViewTo(int destination, int adapterPosition, int layoutPosition, int value, RecyclerView.Recycler recycler) {
        View newView = recycler.getViewForPosition(adapterPosition);
        int left, top, right, bottom;

        if (layoutPosition == -1) {
            addView(newView);
        } else {
            addView(newView, layoutPosition);
        }

        measureChildWithMargins(newView, 0, 0);

        switch (destination) {
            case sStart:
                if (mOrientation == VERTICAL) {
                    left = getPaddingLeft();
                    top = value - getDecoratedMeasuredHeight(newView);
                    right = left + getDecoratedMeasuredWidth(newView);
                    bottom = value;
                } else {
                    left = value - getDecoratedMeasuredWidth(newView);
                    top = getPaddingTop();
                    right = value;
                    bottom = top + getDecoratedMeasuredHeight(newView);
                }
                break;
            case sEnd:
                if (mOrientation == VERTICAL) {
                    left = getPaddingLeft();
                    top = value;
                    right = left + getDecoratedMeasuredWidth(newView);
                    bottom = value + getDecoratedMeasuredHeight(newView);
                } else {
                    left = value;
                    top = getPaddingTop();
                    right = value + getDecoratedMeasuredWidth(newView);
                    bottom = top + getDecoratedMeasuredHeight(newView);
                }
                break;
            default:
                throw new IllegalArgumentException(ERROR_UNKNOWN_DESTINATION_CODE);
        }

        layoutDecoratedWithMargins(newView, left, top, right, bottom);

        return newView;
    }

    @Override
    public View findViewByPosition(int position) {
        View view = super.findViewByPosition(position);
        if(view != null){
            View lastChild = getChildAt(getChildCount() - 1);
            if(view == lastChild && getItemViewType(view) == SHRVItemType.TYPE_HEADER) {
                if (getChildCount() > 1) {
                    if (Math.abs(position - getPosition(getChildAt(getChildCount() - 2))) > 1) {
                        view = null;
                    }
                }
            }
        }
        return view;
    }

    @Override
    public void scrollToPosition(int position) {
        if(position >= 0 && position <= getItemCount()-1){
            mScrollToPosition = position;
            requestLayout();
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final SHRVLinearSmoothScroller linearSmoothScroller = new SHRVLinearSmoothScroller(recyclerView.getContext());

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }

        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos ? -1 : 1;

        if (mOrientation == HORIZONTAL) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    public int findLastVisibleItemPosition(){
        View lastVisibleView = null;

        int childCount = getChildCount();
        int currentChildIndex = 0;

        while(lastVisibleView == null && currentChildIndex < childCount){
            View view = getChildAt(currentChildIndex);
            if(mOrientationHelper.getDecoratedEnd(view) > mOrientationHelper.getStartAfterPadding() &&
                    mOrientationHelper.getDecoratedStart(view) < mOrientationHelper.getEndAfterPadding()){
                lastVisibleView = view;
            }
            currentChildIndex++;
        }

        return lastVisibleView != null?getPosition(lastVisibleView):-1;
    }

    public class SHRVLinearSmoothScroller extends LinearSmoothScroller{

        public SHRVLinearSmoothScroller(Context context) {
            super(context);
        }

        private static final float MILLISECONDS_PER_INCH = 100f;

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return SHRVLinearLayoutManager.this
                    .computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected float calculateSpeedPerPixel
                (DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
        }

        @Override
        protected void onChildAttachedToWindow(View child) {
            int childAdapterPosition = getChildPosition(child);
            if (childAdapterPosition == getTargetPosition()) {
                if (getItemViewType(child) == SHRVItemType.TYPE_HEADER) {
                    int childCount = getChildCount();
                    View lastChild = getChildAt(childCount - 1);

                    if (child == lastChild) {
                        if (childCount > 1) {
                            if (Math.abs(getChildPosition(getChildAt(childCount - 2)) - childAdapterPosition) == 1) {
                                super.onChildAttachedToWindow(child);
                            } else {
                                super.onChildAttachedToWindow(null);
                            }
                        }
                    }else {
                        super.onChildAttachedToWindow(child);
                    }
                } else {
                    super.onChildAttachedToWindow(child);
                }
            }
        }

    }
}
