package ru.astrocode.shrv.library;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Astrocode on 26.03.2017.
 * <p>
 * Sticky header linear layout manager.
 */

public class SHRVLinearLayoutManager extends RecyclerView.LayoutManager {
    private final static String TAG = "SHRVLinearLayoutManager";

    private final static String ERROR_UNKNOWN_ORIENTATION = "Unknown orientation!";
    private final static String ERROR_WRONG_CLASS_TYPE = "Wrong class type!";
    private final static String ERROR_UNKNOWN_DESTINATION_CODE = "Unknown destination code!";

    private final static String TAG_HEADERS_INDEXES = "TAG_HEADERS_INDEXES";
    private final static String TAG_START_POINT_OF_FIRST_ITEM = "TAG_START_POINT_OF_FIRST_ITEM";
    private final static String TAG_START_POINT_OF_CURRENT_HEADER = "TAG_START_POINT_OF_CURRENT_HEADER";
    private final static String TAG_ADAPTER_INDEX_OF_FIRST_ITEM = "TAG_ADAPTER_INDEX_OF_FIRST_ITEM";
    private final static String TAG_ADAPTER_INDEX_OF_CURRENT_HEADER = "TAG_ADAPTER_INDEX_OF_CURRENT_HEADER";

    private final static int sTop = 0, sBottom = 1, sLeft = 2, sRight = 3;

    public final static int VERTICAL = 0, HORIZONTAL = 1;

    private int mStartPointOfFirstItem, mStartPointOfCurrentHeader;
    private int mAdapterIndexOfFirstItem, mAdapterIndexOfCurrentHeader;

    private int mOrientation;
    private ArrayList<Integer> mHeadersIndexes;

    public SHRVLinearLayoutManager(int orientation) {
        setOrientation(orientation);
        mHeadersIndexes = new ArrayList<>();

        mStartPointOfFirstItem = -1;
        mAdapterIndexOfFirstItem = 0;

        mStartPointOfCurrentHeader = -1;
        mAdapterIndexOfCurrentHeader = -1;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        if (mStartPointOfFirstItem == -1) {
            mStartPointOfFirstItem = mOrientation == VERTICAL ? getPaddingTop() : getPaddingLeft();
        }

        if (mAdapterIndexOfCurrentHeader != -1) {
            if (mOrientation == VERTICAL) {
                addViewTo(sBottom, mAdapterIndexOfCurrentHeader, 0, mStartPointOfCurrentHeader, recycler);
            } else {
                addViewTo(sRight, mAdapterIndexOfCurrentHeader, 0, mStartPointOfCurrentHeader, recycler);
            }
        }

        if (mOrientation == VERTICAL) {
            initVertical(recycler);
        } else {
            initHorizontal(recycler);
        }

    }

    private void initVertical(RecyclerView.Recycler recycler) {
        int currentY = mStartPointOfFirstItem;

        for (int i = mAdapterIndexOfFirstItem; i < getItemCount(); i++) {
            View child = addViewTo(sBottom, i, 0, currentY, recycler);

            currentY = getDecoratedBottom(child);

            if (currentY >= getHeight() - getPaddingBottom()) {
                break;
            }
        }
    }

    private void initHorizontal(RecyclerView.Recycler recycler) {
        int currentX = mStartPointOfFirstItem;

        for (int i = mAdapterIndexOfFirstItem; i < getItemCount(); i++) {
            View child = addViewTo(sRight, i, 0, currentX, recycler);

            currentX = getDecoratedRight(child);

            if (currentX >= getWidth() - getPaddingRight()) {
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
            return;
        }

        mOrientation = orientation;
        requestLayout();
    }

    private void rememberCurrentStateVertical() {
        View firstView = getChildAt(getChildCount() - 1);

        if (getItemViewType(firstView) == SHRVItemType.TYPE_HEADER) {
            mAdapterIndexOfCurrentHeader = getPosition(firstView);
            mStartPointOfCurrentHeader = getDecoratedTop(firstView);

            View secondView = getChildAt(getChildCount() - 2);
            if (secondView != null) {
                mAdapterIndexOfFirstItem = getPosition(secondView);
                mStartPointOfFirstItem = getDecoratedTop(secondView);
            }
        } else {
            mAdapterIndexOfFirstItem = getPosition(firstView);
            mStartPointOfFirstItem = getDecoratedTop(firstView);
        }
    }

    private void rememberCurrentStateHorizontal() {
        View firstView = getChildAt(getChildCount() - 1);

        if (getItemViewType(firstView) == SHRVItemType.TYPE_HEADER) {
            mAdapterIndexOfCurrentHeader = getPosition(firstView);
            mStartPointOfCurrentHeader = getDecoratedLeft(firstView);

            View secondView = getChildAt(getChildCount() - 2);
            if (secondView != null) {
                mAdapterIndexOfFirstItem = getPosition(secondView);
                mStartPointOfFirstItem = getDecoratedLeft(secondView);
            }
        } else {
            mAdapterIndexOfFirstItem = getPosition(firstView);
            mStartPointOfFirstItem = getDecoratedLeft(firstView);
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

        if (childCount > 0 && dy > 0) {
            offset = addViewsToBottom(dy, recycler);

            offsetToBottomAndDelete(offset, recycler);
            rememberCurrentStateVertical();
        } else if (childCount > 0 && dy < 0) {
            offset = addViewsToTop(dy, recycler);

            offsetToTopAndDelete(offset, recycler);
            rememberCurrentStateVertical();
        }

        return offset;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int offset = 0;
        int childCount = getChildCount();

        if (childCount > 0 && dx > 0) {
            offset = addViewsToRight(dx, recycler);

            offsetToRightAndDelete(offset, recycler);
            rememberCurrentStateHorizontal();
        } else if (childCount > 0 && dx < 0) {
            offset = addViewsToLeft(dx, recycler);

            offsetToLeftAndDelete(offset, recycler);
            rememberCurrentStateHorizontal();
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
     * Add views (if need ) to the top of the recyclerView and return maximum possible offset(not above the original offset).
     *
     * @param dx       Original offset.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get view.
     * @return
     */
    private int addViewsToRight(int dx, RecyclerView.Recycler recycler) {
        View currentLastView = getChildAt(0);

        int currentAvailableOffset = getDecoratedRight(currentLastView) > (getWidth() - getPaddingRight()) ? getDecoratedRight(currentLastView) - (getWidth() - getPaddingRight()) : 0;
        int currentAdapterIndex = getPosition(currentLastView) + 1;

        while (currentAdapterIndex <= getItemCount() - 1 && currentAvailableOffset < dx) {
            View newView = addViewTo(sRight, currentAdapterIndex, 0, getDecoratedRight(currentLastView), recycler);

            currentLastView = newView;
            currentAvailableOffset += getDecoratedMeasuredWidth(newView);

            currentAdapterIndex++;
        }

        return currentAvailableOffset > dx ? dx : currentAvailableOffset;
    }

    /**
     * Scroll content to right.All view that reached out of the screen are recycled.
     *
     * @param offset   The value to which we must scroll the content.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get or recycle view.
     */
    private void offsetToRightAndDelete(int offset, RecyclerView.Recycler recycler) {
        View currentHeader = null;

        int currentViewIndex = getChildCount() - 1;

        while (currentViewIndex >= 0) {
            View currentView = getChildAt(currentViewIndex);

            if (getItemViewType(currentView) == SHRVItemType.TYPE_HEADER) {
                int currentViewFutureLeft = getDecoratedLeft(currentView) - offset;

                if (currentHeader != null) {
                    if (currentViewFutureLeft <= getPaddingLeft()) {
                        mHeadersIndexes.add(getPosition(currentHeader));

                        removeAndRecycleView(currentHeader, recycler);

                        currentView.offsetLeftAndRight(getPaddingLeft() - getDecoratedLeft(currentView));

                        currentHeader = currentView;
                    } else {
                        if (currentViewFutureLeft < getDecoratedRight(currentHeader)) {
                            currentHeader.offsetLeftAndRight(-(getDecoratedRight(currentHeader) - currentViewFutureLeft));
                        }
                        currentView.offsetLeftAndRight(-offset);
                    }
                } else {
                    if (currentViewIndex == getChildCount() - 1) {
                        currentHeader = currentView;
                    } else {
                        if (currentViewFutureLeft <= getPaddingLeft()) {
                            currentView.offsetLeftAndRight(getPaddingLeft() - getDecoratedLeft(currentView));
                            currentHeader = currentView;
                        } else {
                            currentView.offsetLeftAndRight(-offset);
                        }
                    }
                }
            } else {
                if (getDecoratedRight(currentView) - offset <= getPaddingLeft()) {
                    removeAndRecycleView(currentView, recycler);
                } else {
                    currentView.offsetLeftAndRight(-offset);
                }
            }
            currentViewIndex--;
        }
    }

    /**
     * Add views (if need ) to the left of the recyclerView and return maximum possible offset(not below the original offset).
     *
     * @param dx       Original offset.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get view.
     * @return
     */
    private int addViewsToLeft(int dx, RecyclerView.Recycler recycler) {
        View currentHeader = null, currentFirstView = getChildAt(getChildCount() - 1);

        int currentLeft = getDecoratedLeft(currentFirstView);

        int currentAvailableOffset = getDecoratedLeft(currentFirstView);
        int currentAdapterIndex = getPosition(currentFirstView) - 1;

        if (getItemViewType(currentFirstView) == SHRVItemType.TYPE_HEADER) {
            View subViewHeader = getChildAt(getChildCount() - 2);

            if (subViewHeader != null && getItemViewType(subViewHeader) != SHRVItemType.TYPE_HEADER) {
                if (getPosition(subViewHeader) - getPosition(currentFirstView) == 1) {
                    currentAvailableOffset = getDecoratedLeft(subViewHeader) - (getDecoratedMeasuredWidth(currentFirstView) + getPaddingLeft());
                    currentLeft = getDecoratedLeft(subViewHeader) - getDecoratedMeasuredWidth(currentFirstView);
                } else {
                    currentAdapterIndex = getPosition(subViewHeader) - 1;
                    currentAvailableOffset = currentLeft = getDecoratedLeft(subViewHeader);
                }
            }
            currentHeader = currentFirstView;
        }

        while (currentAdapterIndex >= 0 && currentAvailableOffset > dx) {
            if (currentHeader != null) {
                if (getPosition(currentHeader) > currentAdapterIndex) {
                    if (mHeadersIndexes.size() > 0) {
                        currentHeader = addViewTo(sLeft, mHeadersIndexes.remove(mHeadersIndexes.size() - 1), -1, currentLeft, recycler);
                    } else {
                        currentHeader = null;
                    }
                } else if (getPosition(currentHeader) == currentAdapterIndex) {
                    currentAvailableOffset -= getDecoratedMeasuredWidth(currentHeader);
                    currentLeft -= getDecoratedMeasuredWidth(currentHeader);
                    currentAdapterIndex--;
                    continue;
                }
            }
            View newView = addViewTo(sLeft, currentAdapterIndex, currentHeader != null ? getChildCount() - 1 : -1, currentLeft, recycler);

            currentLeft = getDecoratedLeft(newView);
            currentAvailableOffset -= getDecoratedMeasuredWidth(newView);
            currentAdapterIndex--;
        }

        return currentAvailableOffset < dx ? dx : currentAvailableOffset;
    }

    /**
     * Scroll content to left.All view that reached out of the screen are recycled.
     *
     * @param offset   The value to which we must scroll the content.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get or recycle view.
     */
    private void offsetToLeftAndDelete(int offset, RecyclerView.Recycler recycler) {
        int currentViewIndex = 0;
        View lastSectionView = null, endSectionView = null;

        while (currentViewIndex < getChildCount()) {
            View currentView = getChildAt(currentViewIndex);
            int currentViewFutureLeft = getDecoratedLeft(currentView) - offset;

            if (getItemViewType(currentView) == SHRVItemType.TYPE_HEADER) {
                if (lastSectionView != null && endSectionView != null) {
                    if (getPosition(lastSectionView) - getPosition(currentView) == 1) {
                        if (getDecoratedLeft(lastSectionView) - getDecoratedMeasuredWidth(currentView) >= getWidth() - getPaddingRight()) {
                            removeAndRecycleView(currentView, recycler);
                            currentViewIndex--;
                        } else {
                            if (getDecoratedLeft(currentView) <= getPaddingLeft()) {
                                if (getDecoratedLeft(lastSectionView) > getDecoratedMeasuredWidth(currentView) + getPaddingLeft()) {
                                    currentView.offsetLeftAndRight(getDecoratedLeft(lastSectionView) - getDecoratedRight(currentView));
                                } else {
                                    if (getDecoratedLeft(endSectionView) >= getPaddingLeft()) {
                                        currentView.offsetLeftAndRight(getPaddingLeft() - getDecoratedLeft(currentView));
                                    } else {
                                        if (getDecoratedRight(endSectionView) >= getDecoratedMeasuredWidth(currentView) + getPaddingLeft()) {
                                            currentView.offsetLeftAndRight(getPaddingLeft() - getDecoratedLeft(currentView));
                                        } else {
                                            currentView.offsetLeftAndRight(getDecoratedRight(endSectionView) - getDecoratedRight(currentView));
                                        }
                                    }
                                }
                            } else {
                                currentView.offsetLeftAndRight(-offset);
                            }
                        }
                    } else {
                        if (currentViewFutureLeft > getPaddingLeft()) {
                            currentView.offsetLeftAndRight(getPaddingLeft() - getDecoratedLeft(currentView));
                        } else {
                            currentView.offsetLeftAndRight(-offset);
                        }
                    }

                } else {
                    if (currentViewFutureLeft >= getWidth() - getPaddingRight()) {
                        removeAndRecycleView(currentView, recycler);
                        currentViewIndex--;
                    } else {
                        currentView.offsetLeftAndRight(-offset);
                    }
                }
                endSectionView = null;
            } else {
                if (currentViewFutureLeft >= getWidth() - getPaddingRight()) {
                    removeAndRecycleView(currentView, recycler);
                    currentViewIndex--;
                } else {
                    currentView.offsetLeftAndRight(-offset);
                }

                lastSectionView = currentView;
                if (endSectionView == null) {
                    endSectionView = lastSectionView;
                }
            }
            currentViewIndex++;
        }
    }

    /**
     * Add views (if need ) to the top of the recyclerView and return maximum possible offset(not below the original offset).
     *
     * @param dy       Original offset.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get view.
     * @return
     */
    private int addViewsToTop(int dy, RecyclerView.Recycler recycler) {
        View currentHeader = null, currentFirstView = getChildAt(getChildCount() - 1);

        int currentTop = getDecoratedTop(currentFirstView);

        int currentAvailableOffset = getDecoratedTop(currentFirstView);
        int currentAdapterIndex = getPosition(currentFirstView) - 1;

        if (getItemViewType(currentFirstView) == SHRVItemType.TYPE_HEADER) {
            View subViewHeader = getChildAt(getChildCount() - 2);

            if (subViewHeader != null && getItemViewType(subViewHeader) != SHRVItemType.TYPE_HEADER) {
                if (getPosition(subViewHeader) - getPosition(currentFirstView) == 1) {
                    currentAvailableOffset = getDecoratedTop(subViewHeader) - (getDecoratedMeasuredHeight(currentFirstView) + getPaddingTop());
                    currentTop = getDecoratedTop(subViewHeader) - getDecoratedMeasuredHeight(currentFirstView);
                } else {
                    currentAdapterIndex = getPosition(subViewHeader) - 1;
                    currentAvailableOffset = currentTop = getDecoratedTop(subViewHeader);
                }
            }
            currentHeader = currentFirstView;
        }


        while (currentAdapterIndex >= 0 && currentAvailableOffset > dy) {
            if (currentHeader != null) {
                if (getPosition(currentHeader) > currentAdapterIndex) {
                    if (mHeadersIndexes.size() > 0) {
                        currentHeader = addViewTo(sTop, mHeadersIndexes.remove(mHeadersIndexes.size() - 1), -1, currentTop, recycler);
                    } else {
                        currentHeader = null;
                    }
                } else if (getPosition(currentHeader) == currentAdapterIndex) {
                    currentAvailableOffset -= getDecoratedMeasuredHeight(currentHeader);
                    currentTop -= getDecoratedMeasuredHeight(currentHeader);
                    currentAdapterIndex--;
                    continue;
                }
            }
            View newView = addViewTo(sTop, currentAdapterIndex, currentHeader != null ? getChildCount() - 1 : -1, currentTop, recycler);

            currentTop = getDecoratedTop(newView);
            currentAvailableOffset -= getDecoratedMeasuredHeight(newView);
            currentAdapterIndex--;
        }

        return currentAvailableOffset < dy ? dy : currentAvailableOffset;
    }

    /**
     * Scroll content to top.All view that reached out of the screen are recycled.
     *
     * @param offset   The value to which we must scroll the content.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get or recycle view.
     */
    private void offsetToTopAndDelete(int offset, RecyclerView.Recycler recycler) {
        int currentViewIndex = 0;
        View lastSectionView = null, endSectionView = null;

        while (currentViewIndex < getChildCount()) {
            View currentView = getChildAt(currentViewIndex);
            int currentViewFutureTop = getDecoratedTop(currentView) - offset;

            if (getItemViewType(currentView) == SHRVItemType.TYPE_HEADER) {
                if (lastSectionView != null && endSectionView != null) {
                    if (getPosition(lastSectionView) - getPosition(currentView) == 1) {
                        if (getDecoratedTop(lastSectionView) - getDecoratedMeasuredHeight(currentView) >= getHeight() - getPaddingBottom()) {
                            removeAndRecycleView(currentView, recycler);
                            currentViewIndex--;
                        } else {
                            if (getDecoratedTop(currentView) <= getPaddingTop()) {
                                if (getDecoratedTop(lastSectionView) > getDecoratedMeasuredHeight(currentView) + getPaddingTop()) {
                                    currentView.offsetTopAndBottom(getDecoratedTop(lastSectionView) - getDecoratedBottom(currentView));
                                } else {
                                    if (getDecoratedTop(endSectionView) >= getPaddingTop()) {
                                        currentView.offsetTopAndBottom(getPaddingTop() - getDecoratedTop(currentView));
                                    } else {
                                        if (getDecoratedBottom(endSectionView) >= getDecoratedMeasuredHeight(currentView) + getPaddingTop()) {
                                            currentView.offsetTopAndBottom(getPaddingTop() - getDecoratedTop(currentView));
                                        } else {
                                            currentView.offsetTopAndBottom(getDecoratedBottom(endSectionView) - getDecoratedBottom(currentView));
                                        }
                                    }
                                }
                            } else {
                                currentView.offsetTopAndBottom(-offset);
                            }
                        }
                    } else {
                        if (currentViewFutureTop > getPaddingTop()) {
                            currentView.offsetTopAndBottom(getPaddingTop() - getDecoratedTop(currentView));
                        } else {
                            currentView.offsetTopAndBottom(-offset);
                        }
                    }

                } else {
                    if (currentViewFutureTop >= getHeight() - getPaddingBottom()) {
                        removeAndRecycleView(currentView, recycler);
                        currentViewIndex--;
                    } else {
                        currentView.offsetTopAndBottom(-offset);
                    }
                }
                endSectionView = null;
            } else {
                if (currentViewFutureTop >= getHeight() - getPaddingBottom()) {
                    removeAndRecycleView(currentView, recycler);
                    currentViewIndex--;
                } else {
                    currentView.offsetTopAndBottom(-offset);
                }

                lastSectionView = currentView;
                if (endSectionView == null) {
                    endSectionView = lastSectionView;
                }
            }
            currentViewIndex++;
        }
    }

    /**
     * Add views (if need ) to the bottom of the recyclerView and return maximum possible offset(not above the original offset).
     *
     * @param dy       Original offset.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get view.
     * @return
     */
    private int addViewsToBottom(int dy, RecyclerView.Recycler recycler) {
        View currentLastView = getChildAt(0);

        int currentAvailableOffset = getDecoratedBottom(currentLastView) > getHeight() - getPaddingBottom() ? getDecoratedBottom(currentLastView) - (getHeight() - getPaddingBottom()) : 0;
        int currentAdapterIndex = getPosition(currentLastView) + 1;

        while (currentAdapterIndex <= getItemCount() - 1 && currentAvailableOffset < dy) {
            View newView = addViewTo(sBottom, currentAdapterIndex, 0, getDecoratedBottom(currentLastView), recycler);

            currentLastView = newView;
            currentAvailableOffset += getDecoratedMeasuredHeight(newView);

            currentAdapterIndex++;
        }

        return currentAvailableOffset > dy ? dy : currentAvailableOffset;
    }

    /**
     * Scroll content to bottom.All view that reached out of the screen are recycled.
     *
     * @param offset   The value to which we must scroll the content.
     * @param recycler {@link android.support.v7.widget.RecyclerView.Recycler} to get or recycle view.
     */
    private void offsetToBottomAndDelete(int offset, RecyclerView.Recycler recycler) {
        View currentHeader = null;

        int currentViewIndex = getChildCount() - 1;

        while (currentViewIndex >= 0) {
            View currentView = getChildAt(currentViewIndex);

            if (getItemViewType(currentView) == SHRVItemType.TYPE_HEADER) {
                int currentViewFutureTop = getDecoratedTop(currentView) - offset;

                if (currentHeader != null) {
                    if (currentViewFutureTop <= getPaddingTop()) {
                        mHeadersIndexes.add(getPosition(currentHeader));

                        removeAndRecycleView(currentHeader, recycler);

                        currentView.offsetTopAndBottom(getPaddingTop() - getDecoratedTop(currentView));

                        currentHeader = currentView;
                    } else {
                        if (currentViewFutureTop < getDecoratedBottom(currentHeader)) {
                            currentHeader.offsetTopAndBottom(-(getDecoratedBottom(currentHeader) - currentViewFutureTop));
                        }
                        currentView.offsetTopAndBottom(-offset);
                    }
                } else {
                    if (currentViewIndex == getChildCount() - 1) {
                        currentHeader = currentView;
                    } else {
                        if (currentViewFutureTop <= getPaddingTop()) {
                            currentView.offsetTopAndBottom(getPaddingTop() - getDecoratedTop(currentView));
                            currentHeader = currentView;
                        } else {
                            currentView.offsetTopAndBottom(-offset);
                        }
                    }
                }
            } else {
                if (getDecoratedBottom(currentView) - offset <= getPaddingTop()) {
                    removeAndRecycleView(currentView, recycler);
                } else {
                    currentView.offsetTopAndBottom(-offset);
                }
            }
            currentViewIndex--;
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
            case sTop:
                left = getPaddingLeft();
                top = value - getDecoratedMeasuredHeight(newView);
                right = left + getDecoratedMeasuredWidth(newView);
                bottom = value;
                break;
            case sBottom:
                left = getPaddingLeft();
                top = value;
                right = left + getDecoratedMeasuredWidth(newView);
                bottom = value + getDecoratedMeasuredHeight(newView);
                break;
            case sLeft:
                left = value - getDecoratedMeasuredWidth(newView);
                top = getPaddingTop();
                right = value;
                bottom = top + getDecoratedMeasuredHeight(newView);
                break;
            case sRight:
                left = value;
                top = getPaddingTop();
                right = value + getDecoratedMeasuredWidth(newView);
                bottom = top + getDecoratedMeasuredHeight(newView);
                break;
            default:
                throw new IllegalArgumentException(ERROR_UNKNOWN_DESTINATION_CODE);
        }

        layoutDecoratedWithMargins(newView, left, top, right, bottom);

        return newView;
    }

}
