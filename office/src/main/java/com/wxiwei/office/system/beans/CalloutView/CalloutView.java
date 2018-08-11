package com.wxiwei.office.system.beans.CalloutView;

import java.util.ArrayList;
import java.util.List;

import com.wxiwei.office.constant.MainConstant;
import com.wxiwei.office.system.IControl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class CalloutView extends View {
    private float zoom = 1.0f;
    private float mX, mY;
    public static final float TOUCH_TOLERANCE = 4;
    private List<PathInfo> mPathList = null;
    private PathInfo mPathInfo = null;
    private final int offset = 5;
    private IExportListener mListener;
    private int left = 0;
    private int top = 0;
    private CalloutManager calloutMgr;
    private Runnable runnable = null;
    private int index = 0;
    private MyPaint paint;
    private Rect clipRect;
    // 正常的画画
    private Canvas mNormalCanvas;
    private Bitmap mNormalBitmap;
    private Paint mNormalPaint, mErasePaint;
    private Path mNormalPath, mErasePath;
    private List<Path> mToalPaths;
    private List<Boolean> mIsErase;

    public CalloutView(Context context) {
        super(context);
        init();
    }

    public CalloutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalloutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CalloutView(Context context, IControl control,
                       IExportListener listener) {
        super(context);
        mListener = listener;
        calloutMgr = control.getSysKit().getCalloutManager();
        init();
    }

    public void init() {
        paint = new MyPaint();
        clipRect = new Rect();
        post(new Runnable() {
            @Override
            public void run() {
                mToalPaths = new ArrayList<>();
                mIsErase = new ArrayList<>();
                mNormalBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
                mNormalCanvas = new Canvas(mNormalBitmap);
                mNormalPaint = new Paint();
                mErasePaint = new Paint();
                mNormalPath = new Path();
                mErasePath = new Path();
                mNormalPaint.setColor(Color.RED);
                mNormalPaint.setStyle(Paint.Style.STROKE);
                mErasePaint.setStyle(Paint.Style.STROKE);
                mNormalPaint.setStrokeWidth(20);
                mNormalPaint.setAntiAlias(true);
                mNormalPaint.setStrokeCap(Paint.Cap.ROUND);
                mNormalPaint.setStrokeJoin(Paint.Join.ROUND);
                mErasePaint.setStrokeWidth(20);
                mErasePaint.setStrokeCap(Paint.Cap.ROUND);
                mErasePaint.setStrokeJoin(Paint.Join.ROUND);
                mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            }
        });
    }

    public void setControl(IControl control) {
        calloutMgr = control.getSysKit().getCalloutManager();
        calloutMgr.setDrawingMode(MainConstant.DRAWMODE_NORMAL);
    }

    public void setExportListener(IExportListener listener) {
        this.mListener = listener;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void setIndex(int index) {
        this.index = index;
        invalidate();
    }

    public void setColor(int color) {
        if (calloutMgr != null)
            calloutMgr.setColor(color);
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (getMode()) {
            case MainConstant.DRAWMODE_CALLOUTDRAW:
            case MainConstant.DRAWMODE_CALLOUTERASE:
            case MainConstant.DRAWMODE_NORMAL: {
                canvas.getClipBounds(clipRect);
                if (calloutMgr != null)
                    mPathList = calloutMgr.getPath(index, false);
                if (mPathList != null) {
                    for (int i = 0; i < mPathList.size(); i++) {
                        PathInfo pathInfo = mPathList.get(i);
                        paint.setStrokeWidth(pathInfo.width);
                        paint.setColor(pathInfo.color);
                        canvas.save();//保存画布的状态
                        canvas.clipRect(left, top, clipRect.right, clipRect.bottom);
                        canvas.scale(zoom, zoom);
                        canvas.drawPath(pathInfo.path, paint);
                        canvas.restore();//取出保存的状态的
                    }
                }
                break;
            }
            case MainConstant.DRAWMODE_NORMAL_NOT_DRAW:
            case MainConstant.DRAWMODE_NORMALDRAW:
            case MainConstant.DRAWMODE_NORMALERASE: {
                mNormalBitmap.eraseColor(Color.TRANSPARENT);
                for (int i = 0; i < mToalPaths.size(); i++) {
                    mNormalCanvas.drawPath(mToalPaths.get(i), mIsErase.get(i) ? mErasePaint : mNormalPaint);
                }
//                mNormalCanvas.drawPath(mNormalPath, mNormalPaint);
//                mNormalCanvas.drawPath(mErasePath, mErasePaint);
                canvas.drawBitmap(mNormalBitmap, 0, 0, mNormalPaint);
                break;
            }
        }
    }

    public void clear() {
        if (calloutMgr != null)
            calloutMgr.clear();
    }

    public void setClip(int left, int top) {
        this.left = left;
        this.top = top;
    }

    private void touch_start(float x, float y) {
        x /= zoom;
        y /= zoom;
        mX = x;
        mY = y;
        switch (calloutMgr.getDrawingMode()) {
            case MainConstant.DRAWMODE_CALLOUTDRAW: {
                mPathInfo = new PathInfo();
                mPathInfo.path.moveTo(x, y);
                mPathInfo.color = calloutMgr.getColor();
                mPathInfo.width = calloutMgr.getWidth();
                mPathList = calloutMgr.getPath(index, true);
                mPathList.add(mPathInfo);
                break;
            }
            case MainConstant.DRAWMODE_NORMALDRAW: {
                mNormalPath = new Path();
                mNormalPath.moveTo(x, y);
                mToalPaths.add(mNormalPath);
                mIsErase.add(false);
                break;
            }
            case MainConstant.DRAWMODE_NORMALERASE: {
                mErasePath = new Path();
                mErasePath.moveTo(x, y);
                mToalPaths.add(mErasePath);
                mIsErase.add(true);
                break;
            }
        }
    }

    private void touch_move(float x, float y) {
        if (calloutMgr == null) return;
        x /= zoom;
        y /= zoom;
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        switch (calloutMgr.getDrawingMode()) {
            case MainConstant.DRAWMODE_CALLOUTDRAW: {
                if ((dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) &&
                        dx <= TOUCH_TOLERANCE * 40 || dy <= TOUCH_TOLERANCE * 40) {
                    mPathInfo.path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
                break;
            }
            case MainConstant.DRAWMODE_NORMALDRAW: {
                if ((dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)) {
                    mNormalPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
                break;
            }
            case MainConstant.DRAWMODE_NORMALERASE: {
                if ((dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)) {
                    mErasePath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
                break;
            }
        }
    }

    private void touch_up() {
        if (calloutMgr == null || mPathInfo == null) return;
        switch (calloutMgr.getDrawingMode()) {
            case MainConstant.DRAWMODE_CALLOUTDRAW: {
                mPathInfo.path.lineTo(mX, mY);
                mPathInfo.x = mX + 1;
                mPathInfo.y = mY + 1;
                break;
            }
            case MainConstant.DRAWMODE_CALLOUTERASE: {
                if (mPathList != null) {
                    for (int i = 0; i < mPathList.size(); i++) {
                        PathInfo pathInfo = mPathList.get(i);
                        Path path = new Path(pathInfo.path);
                        path.lineTo(pathInfo.x, pathInfo.y);

                        RectF bounds = new RectF();
                        path.computeBounds(bounds, false);

                        Region region = new Region();
                        region.setPath(path, new Region((int) bounds.left,
                                (int) bounds.top, (int) bounds.right,
                                (int) bounds.bottom));

                        if (region.op(new Region((int) mX - offset, (int) mY - offset,
                                (int) mX + offset, (int) mY + offset), Op.INTERSECT)) {
                            mPathList.remove(i);
                        }
                    }
                }
                break;
            }
            case MainConstant.DRAWMODE_NORMALDRAW: {
                mNormalPath.lineTo(mX, mY);
//                mNormalPath.reset();
                break;
            }
            case MainConstant.DRAWMODE_NORMALERASE: {
                mErasePath.lineTo(mX, mY);
//                mErasePath.reset();
                break;
            }
        }
    }

    private void exportImage() {
        if (runnable != null) {
            removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            public void run() {
                if (mListener != null)
                    mListener.exportImage();
            }
        };
        postDelayed(runnable, 1000);
    }

    public void setMode(int mode) {
        if (calloutMgr != null)
            calloutMgr.setDrawingMode(mode);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (calloutMgr == null
                || calloutMgr.getDrawingMode()
                == MainConstant.DRAWMODE_NORMAL) {
            return false;
        }
        float x = event.getRawX();
        float y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                exportImage();
                break;
        }
        return true;
    }

    public int getMode() {
        if (calloutMgr != null)
            return calloutMgr.getDrawingMode();
        return -1;
    }
}
