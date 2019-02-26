package run.brief.util.swipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import run.brief.R;
import run.brief.util.Cal;


public class SwipeDismissListViewTouchListener implements View.OnTouchListener {
 // Cached ViewConfiguration and system-wide constant values
 private int mSlop;
 private int mMinFlingVelocity;
 private int mMaxFlingVelocity;
 private long mAnimationTime;

 // Fixed properties
 private ListView mListView;
 private OnDismissCallback mCallback;
 private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

 private static int minSwipePixels=20;

 // Transient properties
 private List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
 private int mDismissAnimationRefCount = 0;
 private float mDownX;
 private float mDownY;
 private float mStartDownY;
 private boolean mSwiping;
private boolean mInSwiping;
 private VelocityTracker mVelocityTracker;
 private int mDownPosition;
 private View mDownContainer;
 private View mDownView;
 //private boolean mPaused;
 private static boolean dismissRight=false;
 
 private static boolean cancelled =false;
 
 private boolean allowDismissRight=false;
 private boolean allowDismissLeft=false;
 
 private static long lastscrolltime;
 private static final long WAIT_AFTER_SCROLL_MILLIS=1000;
 private static final int MIN_FLING=800;
 

 public interface OnDismissCallback {

     void onDismiss(ListView listView, int[] reverseSortedPositions);
	 
 }
 public void setDismissLeftRight(boolean left, boolean right) {
	 this.allowDismissRight=right;
	 this.allowDismissLeft=left;
 }
 public static boolean isDismissRight() {
	 return dismissRight;
 }

 public SwipeDismissListViewTouchListener(ListView listView, OnDismissCallback callback) {
     ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
     
     WindowManager wm = (WindowManager) listView.getContext().getSystemService(Context.WINDOW_SERVICE);
     Display display = wm.getDefaultDisplay();
     //Display display = lgetWindowManager().getDefaultDisplay();
     Point size = new Point();
     display.getSize(size);
     minSwipePixels = size.x/4;
    //BLog.e("SWIPE", "min px: "+minSwipePixels);
     
     mSlop = vc.getScaledTouchSlop();
     mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
     if(mMinFlingVelocity<MIN_FLING)
    	 mMinFlingVelocity=MIN_FLING;
     mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();

     mAnimationTime = listView.getContext().getResources().getInteger(
             android.R.integer.config_shortAnimTime);
     mListView = listView;
     mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
            if(cancelled) {
                lastscrolltime = Cal.getUnixTime();
                cancelled = false;
                lastscrolltime=Cal.getUnixTime();
                //setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                cancelled =false;

            }

            //BLog.e("SCROLL", "is scrolling 1------");
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
            //BLog.e("SCROLL", "is scrolling 2---------");
			if(visibleItemCount>1) {
                if(lastscrolltime!=0 && Cal.getUnixTime()-lastscrolltime<200) {
                    cancelled =true;
                    lastscrolltime = Cal.getUnixTime();
                    mSwiping=false;
                    //mSwiping=false;
                    //BLog.e("SCROLL", "c: "+cancelled+",  [ "+(Cal.getUnixTime()-lastscrolltime));
                } else {
                    if(cancelled) {
                        lastscrolltime = Cal.getUnixTime();
                        cancelled = false;
                    }
                }


				//cancelled =true;
				//BLog.e("SCROLL", "is scrolling 1");
			}
		}
	});
     mCallback = callback;
 }


 //public void setEnabled(boolean enabled) {
 //    mPaused = !enabled;
 //}
public boolean isSwiping() {
    return mSwiping;
}
    /*
 public AbsListView.OnScrollListener makeScrollListener() {
     return new AbsListView.OnScrollListener() {
         @Override
         public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        	 lastscrolltime=Cal.getUnixTime();
             setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
             cancelled =true;
             //BLog.e("SCROLL", "is scrolling 2");
             mSwiping=false;
         }

         @Override
         public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        	 cancelled =true;
        	 //BLog.e("SCROLL", "is scrolling 2");
         }
     };
 }
    */

	public static long getLastScrolledTime() {
		return lastscrolltime;
	}
	public static boolean allowDrag() {
		if(!cancelled && lastscrolltime+WAIT_AFTER_SCROLL_MILLIS<Cal.getUnixTime())
			return true;
		return false;
	}


 @Override
 public boolean onTouch(View view, MotionEvent motionEvent) {
     boolean sendsblock=false;
     if(mDownContainer!=null) {
         String ratingid = (String) mDownContainer.getTag();
         //BLog.e("RTID",ratingid);
         if(ratingid.contains("--1"))   // bad little hack, BriefSend's are the ony ones that will contain this string in the briefrating applied in the view tag
             sendsblock=true;
     }
     if (mDownView != null)
         //BLog.e("touch",""+((String) mDownView.getTag()));
         if (mViewWidth < 2) {
             mViewWidth = mListView.getWidth();
         }
     float deltaX = motionEvent.getRawX() - mDownX;
     float deltaY = motionEvent.getRawY() - mDownY;

     switch (motionEvent.getActionMasked()) {

         case MotionEvent.ACTION_DOWN: {

             cancelled = false;
             //BLog.e("Swipe","down: "+allowDrag());
             if (allowDrag()) {
                 // Find the child view that was touched (perform a hit test)
                 Rect rect = new Rect();
                 int childCount = mListView.getChildCount();
                 int[] listViewCoords = new int[2];
                 mListView.getLocationOnScreen(listViewCoords);
                 int x = (int) motionEvent.getRawX() - listViewCoords[0];
                 int y = (int) motionEvent.getRawY() - listViewCoords[1];
                 View child;
                 for (int i = 0; i < childCount; i++) {
                     child = mListView.getChildAt(i);
                     child.getHitRect(rect);
                     if (rect.contains(x, y)) {
                         mDownContainer = child;
                         mDownView = mDownContainer.findViewById(R.id.brief_item_pod_holder);
                         break;
                     }
                 }

                 if (mDownView != null) {
                     mDownX = motionEvent.getRawX();
                     mDownY = motionEvent.getRawY();
                     mStartDownY = motionEvent.getRawY();

                     try {
                         mDownPosition = mListView.getPositionForView(mDownView);
                     } catch (Exception e) {
                         // for some reason call throw null pointer, but mDownView previously tested for null, so internal android issues assumed.
                     }
                     mVelocityTracker = VelocityTracker.obtain();
                     mVelocityTracker.addMovement(motionEvent);
                     //mVelocityTracker.recycle();
                 }


             }
             view.onTouchEvent(motionEvent);
             return true;
         }

         case MotionEvent.ACTION_CANCEL:

         case MotionEvent.ACTION_UP: {

             //BLog.e("Swipe","up: "+allowDrag());


             cancelled = false;
             if (mVelocityTracker == null) {
                 break;
             }

             mDownContainer.setBackgroundColor(view.getContext().getResources().getColor(R.color.transparent));


             mVelocityTracker.addMovement(motionEvent);
             mVelocityTracker.computeCurrentVelocity(1000);
             float velocityX = Math.abs(mVelocityTracker.getXVelocity());
             float velocityY = Math.abs(mVelocityTracker.getYVelocity());
             boolean dismiss = false;
             dismissRight = false;
             float endY = motionEvent.getRawY();
             if (endY - mStartDownY > 70 || endY - mStartDownY < -70) {
                 // halt dismissal and Y movement to big
                 //BLog.e("STOP","Stop swipe dismaiss: "+endY+" - "+mStartDownY+" - "+(endY-mStartDownY));
                 dismiss = false;
             } else if (deltaX != 0 && Math.abs(deltaX) > mViewWidth / 2 && deltaX > (deltaY * 3)) {
                 dismiss = true;
                 dismissRight = deltaX > 0;
             } else if (deltaX != 0 && mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity
                     && velocityY < velocityX) {
                 dismiss = true;
                 dismissRight = mVelocityTracker.getXVelocity() > 0;
             }
             if (!sendsblock && dismiss) {
                 // dismiss
                 final View downView = mDownView; // mDownView gets null'd before animation ends
                 final int downPosition = mDownPosition;
                 ++mDismissAnimationRefCount;
                 mDownView.animate()
                         .translationX(dismissRight ? mViewWidth : -mViewWidth)
                         .alpha(0)
                         .setDuration(mAnimationTime)
                         .setListener(new AnimatorListenerAdapter() {
                             @Override
                             public void onAnimationEnd(Animator animation) {
                                 performDismiss(downView, downPosition);
                             }
                         });
             } else {
                 // cancel
                 mDownView.animate()
                         .translationX(0)
                         .alpha(1)
                         .setDuration(mAnimationTime)
                         .setListener(null);
             }
             mVelocityTracker = null;
             mDownX = 0;
             mDownY = 0;

             mDownView = null;
             mDownPosition = ListView.INVALID_POSITION;
             mSwiping = false;


             break;

         }

         case MotionEvent.ACTION_MOVE: {
             //if (mVelocityTracker == null || mPaused) {
             //	 lastscrolltime=Cal.getUnixTime();
             //	 break;
             //}

             if (!sendsblock && allowDrag() && mVelocityTracker != null) {
                 mVelocityTracker.addMovement(motionEvent);

                 boolean allowDismiss = false;
                 if (deltaX < 4 && allowDismissLeft)
                     allowDismiss = true;
                 else if (deltaX > 4 && allowDismissRight)
                     allowDismiss = true;
                 //BLog.e("Swipe","deltaX: "+Math.abs(deltaX)+", move: "+allowDismiss);
                 if (
                         (deltaX != 0 && Math.abs(deltaX) > minSwipePixels)
                                 && allowDismiss
                         ) {
                     //BLog.e("Swipe","move in: "+allowDrag());
                     mSwiping = true;
                     mListView.requestDisallowInterceptTouchEvent(true);

                     // Cancel ListView's touch (un-highlighting the item)
                     MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                     cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                             (motionEvent.getActionIndex()
                                     << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                     mListView.onTouchEvent(cancelEvent);
                 }

                 if (mSwiping) {
                     //BLog.e("Swipe","move in2: "+allowDrag());
                     boolean isDismissableOnRelease = false;
                     if (deltaX != 0 && Math.abs(deltaX) > mViewWidth / 2 && deltaX > (deltaY * 3)) {
                         isDismissableOnRelease = true;
                         //dismissRight = deltaX > 0;

                     } else {
                         //mDownContainer.setBackgroundColor(view.getContext().getResources().getColor(R.color.white));
                         //mDownContainer.setBackgroundColor(view.getContext().getResources().getColor(R.color.black_alpha));
                     }
                     mDownContainer.setBackgroundColor(view.getContext().getResources().getColor(R.color.black_alpha));

                     mDownView.setTranslationX(deltaX);
                     mDownView.setAlpha(Math.max(0f, Math.min(1f,
                             1f - 2f * Math.abs(deltaX) / mViewWidth)));
                     return true;
                 }
             }
             break;
             //}
         }
     }

     return false;
 }

 class PendingDismissData implements Comparable<PendingDismissData> {
     public int position;
     public View view;

     public PendingDismissData(int position, View view) {
         this.position = position;
         this.view = view;
     }

     @Override
     public int compareTo(PendingDismissData other) {
         // Sort by descending position
         return other.position - position;
     }
 }

 private void performDismiss(final View dismissView, final int dismissPosition) {
     // Animate the dismissed list item to zero-height and fire the dismiss callback when
     // all dismissed list item animations have completed. This triggers layout on each animation
     // frame; in the future we may want to do something smarter and more performant.

     final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
     final int originalHeight = dismissView.getHeight();

     ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

     animator.addListener(new AnimatorListenerAdapter() {
         @Override
         public void onAnimationEnd(Animator animation) {
             --mDismissAnimationRefCount;
             if (mDismissAnimationRefCount == 0) {
                 // No active animations, process all pending dismisses.
                 // Sort by descending position
                 Collections.sort(mPendingDismisses);

                 int[] dismissPositions = new int[mPendingDismisses.size()];
                 for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                     dismissPositions[i] = mPendingDismisses.get(i).position;
                 }
                 

                 ViewGroup.LayoutParams lp;
                 for (PendingDismissData pendingDismiss : mPendingDismisses) {
                     // Reset view presentation
                     pendingDismiss.view.setAlpha(1f);
                     pendingDismiss.view.setTranslationX(0);
                     lp = pendingDismiss.view.getLayoutParams();
                     //lp.height = 0;//originalHeight;
                     pendingDismiss.view.setLayoutParams(lp);
                     //mListView.removeView(dismissView);

                     //View bye=(View) mListView.getChildAt(dismissPosition);
                     //bye.setVisibility(View.GONE);
                     //pendingDismiss.view.setVisibility(View.GONE);
                 }

                 mPendingDismisses.clear();
                 //lp = dismissView.getLayoutParams();
                 //lp.height = 0;
                 ///dismissView.setLayoutParams(lp);
                 //dismissView.setVisibility(View.GONE);
                 
                 mCallback.onDismiss(mListView, dismissPositions);
                 //dismissView.setLayoutParams(new LayoutParams());
             }
         }
     });

     animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
         @Override
         public void onAnimationUpdate(ValueAnimator valueAnimator) {
             lp.height = (Integer) valueAnimator.getAnimatedValue();
             dismissView.setLayoutParams(lp);
         }
     });

     mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
     animator.start();
 }

}