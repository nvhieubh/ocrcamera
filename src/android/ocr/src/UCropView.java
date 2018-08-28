package cordova.plugin.ocrcamera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.yalantis.ucrop.callback.CropBoundsChangeListener;
import com.yalantis.ucrop.callback.OverlayViewChangeListener;

import cordova.plugin.ocrcamera.R;
import cordova.plugin.ocrcamera.OverlayView;
import cordova.plugin.ocrcamera.GestureCropImageView;

public class UCropView extends FrameLayout {

    private GestureCropImageView mGestureCropImageView;
    private final OverlayView mViewOverlay;

    private R r;

    public UCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        r = new R(context);

        LayoutInflater.from(context).inflate(r.getId("layout", "ucrop_view"), this, true);
        mGestureCropImageView = (GestureCropImageView) findViewById(r.getId("id", "image_view_crop"));
        mViewOverlay = (OverlayView) findViewById(r.getId("id", "view_overlay"));

        TypedArray a = context.obtainStyledAttributes(attrs, r.getStyleableIntArray(context, "ucrop_UCropView"));
        mViewOverlay.processStyledAttributes(a);
        mGestureCropImageView.processStyledAttributes(a, r);
        a.recycle();
        mGestureCropImageView.setOnCropImageViewChangedListener((MainActivity)context);

        setListenersToViews();
    }

    private void setListenersToViews() {
        mGestureCropImageView.setCropBoundsChangeListener(new CropBoundsChangeListener() {
            @Override
            public void onCropAspectRatioChanged(float cropRatio) {
                mViewOverlay.setTargetAspectRatio(cropRatio);
            }
        });
//        mViewOverlay.setOverlayViewChangeListener(new OverlayViewChangeListener() {
//            @Override
//            public void onCropRectUpdated(RectF cropRect) {
//                mGestureCropImageView.setCropRect(cropRect);
//            }
//        });
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @NonNull
    public GestureCropImageView getCropImageView() {
        return mGestureCropImageView;
    }

    @NonNull
    public OverlayView getOverlayView() {
        return mViewOverlay;
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    public void resetCropImageView() {
        removeView(mGestureCropImageView);
        mGestureCropImageView = new GestureCropImageView(getContext());
        setListenersToViews();
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect());
        addView(mGestureCropImageView, 0);
    }
}