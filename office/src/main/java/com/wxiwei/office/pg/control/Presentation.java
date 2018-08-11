/*
 * 文件名称:          Presentation.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:49:28
 */
package com.wxiwei.office.pg.control;

import java.util.List;

import com.wxiwei.office.callback.IViewCallback;
import com.wxiwei.office.common.ISlideShow;
import com.wxiwei.office.constant.EventConstant;
import com.wxiwei.office.constant.MainConstant;
import com.wxiwei.office.java.awt.Dimension;
import com.wxiwei.office.pg.animate.ShapeAnimation;
import com.wxiwei.office.pg.model.PGModel;
import com.wxiwei.office.pg.model.PGNotes;
import com.wxiwei.office.pg.model.PGSlide;
import com.wxiwei.office.pg.view.SlideDrawKit;
import com.wxiwei.office.simpletext.model.IDocument;
import com.wxiwei.office.system.IControl;
import com.wxiwei.office.system.IFind;
import com.wxiwei.office.system.SysKit;
import com.wxiwei.office.system.beans.CalloutView.IExportListener;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

public class Presentation extends FrameLayout implements IFind, IExportListener {

    public Presentation(Context context) {
        super(context);
    }

    /**
     *
     */
    public Presentation(Activity activity, PGModel pgModel, IControl control) {
        super(activity);
        this.control = control;
        this.pgModel = pgModel;
        //
        editor = new PGEditor(this);
        //
        pgPrintMode = new PGPrintMode(activity, control, pgModel, editor);
    }

    public PGPrintMode getView() {
        return pgPrintMode;
    }

    @Override
    public void exportImage() {
        if (slideshow) {
            createPicture();
        } else {
            pgPrintMode.exportImage(pgPrintMode.getListView().getCurrentPageView(), null);
        }
    }

    /**
     *
     */
    public void init() {
        init = true;
        pgPrintMode.init();
    }


    public void setViewVisible(boolean visible) {
        pgPrintMode.setVisible(visible);
    }

    public boolean showLoadingSlide() {
        if (currentIndex < getRealSlideCount()) {
            post(new Runnable() {
                /**
                 *
                 */
                public void run() {
                    setViewVisible(true);
                }
            });

            pgPrintMode.showSlideForIndex(currentIndex);
            return true;
        }
        return false;
    }

    /**
     * 显示指定的slide
     */
    public void showSlide(int index, boolean find) {
        if (!find) {
            control.getMainFrame().setFindBackForwardState(false);
        }
        if (index >= pgModel.getSlideCount()) {
            return;
        }
        if (!slideshow) {
            currentIndex = index;
            if (index < getRealSlideCount()) {
                pgPrintMode.showSlideForIndex(index);
            } else {
                setViewVisible(false);
            }
        } else {
            int old = currentIndex;
            currentIndex = index;
            currentSlide = pgModel.getSlide(index);
            if (old != currentIndex) {
                control.actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null);
                // 显示的slide，需要dispose不显示slide的
                SlideDrawKit.instance().disposeOldSlideView(pgModel, pgModel.getSlide(old));
            }
            postInvalidate();
            // to picture
            post(new Runnable() {

                @Override
                public void run() {
                    if (control != null) {
                        control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null);
                    }
                }
            });
        }

    }

    /**
     *
     */
    protected void onDraw(Canvas canvas) {
        if (!init || !slideshow) {
            return;
        }
        try {
            // auto test code
            if (control.isAutoTest()) {
                if (currentIndex < getRealSlideCount() - 1) {
                    showSlide(currentIndex + 1, false);
                } else {
                    control.actionEvent(EventConstant.SYS_AUTO_TEST_FINISH_ID, true);
                }
            }
            if (preShowSlideIndex != currentIndex) {
                control.getMainFrame().changePage();
                preShowSlideIndex = currentIndex;
            }
        } catch (NullPointerException ex) {
            control.getSysKit().getErrorKit().writerLog(ex);
        }
    }

    /**
     *
     */
    public void createPicture() {
    }

    /**
     * @param destBitmap
     * @return
     */
    public Bitmap getSnapshot(Bitmap destBitmap) {
        if (destBitmap == null) {
            return null;
        }

        if (!init || !slideshow) {
            return pgPrintMode.getSnapshot(destBitmap);
        }

        return destBitmap;
    }

    /**
     * page to image for page number (base 1)
     *
     * @return bitmap raw data
     */
    public Bitmap slideToImage(int slideNumber) {
        if (slideNumber <= 0 || slideNumber > getRealSlideCount()) {
            return null;
        }
        return SlideDrawKit.instance().slideToImage(pgModel, editor, pgModel.getSlide(slideNumber - 1));
    }

    /**
     * page to image for page number (base 1)
     *
     * @return bitmap raw data
     */
    public Bitmap slideAreaToImage(int slideNumber, int srcLeft, int srcTop, int srcWidth, int srcHeight, int desWidth, int desHeight) {
        if (slideNumber <= 0 || slideNumber > getRealSlideCount()
                || !SysKit.isValidateRect((int) (getPageSize().getWidth()), (int) (getPageSize().getHeight()), srcLeft, srcTop, srcWidth, srcHeight)) {
            return null;
        }
        return SlideDrawKit.instance().slideAreaToImage(pgModel, editor, pgModel.getSlide(slideNumber - 1),
                srcLeft, srcTop, srcWidth, srcHeight, desWidth, desHeight);
    }

    /**
     * page to image for page number (base 1)
     *
     * @return bitmap raw data
     */
    public Bitmap getThumbnail(int slideNumber, float zoom) {
        if (slideNumber <= 0 || slideNumber > getRealSlideCount()) {
            return null;
        }
        return SlideDrawKit.instance().getThumbnail(pgModel, editor, pgModel.getSlide(slideNumber - 1), zoom);
    }


    /**
     * get slide node for slide number (base 1)
     *
     * @param slideNumber slide number
     * @return slide note
     */
    public String getSldieNote(int slideNumber) {
        if (slideNumber <= 0 || slideNumber > getSlideCount()) {
            return null;
        }
        PGNotes note = pgModel.getSlide(slideNumber - 1).getNotes();
        return note == null ? "" : note.getNotes();
    }

    /**
     *
     *
     */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isConfigurationChanged = true;
    }

    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        processPageSize(w, h);
    }

    private void processPageSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        /*if (layoutParams == null)
        {
            return;
        }*/
        if (isConfigurationChanged || slideshow) {
            if (isConfigurationChanged) {
                isConfigurationChanged = false;
            }

            fitZoom = getFitZoom();

            if (slideshow) {
                // to picture
                post(new Runnable() {
                    public void run() {
                        control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null);
                    }
                });
            }
        }
    }

    /**
     *
     */
    public float getFitZoom() {
        if (slideshow) {
            Dimension pageSize = getPageSize();
            return Math.min((float) (mWidth) / pageSize.width,
                    (float) (mHeight) / pageSize.height);
        }
        return pgPrintMode.getFitZoom();
    }

    /**
     * 返回当前显示slide的index
     */
    public int getCurrentIndex() {
        return (slideshow ? slideIndex_SlideShow : pgPrintMode.getCurrentPageNumber() - 1);
    }

    /**
     *
     */
    public int getSlideCount() {
        return pgModel.getSlideCount();
    }

    /**
     *
     */
    public int getRealSlideCount() {
        return pgModel.getRealSlideCount();
    }

    /**
     *
     */
    public PGSlide getSlide(int index) {
        return pgModel.getSlide(index);
    }

    /**
     *
     */
    public IControl getControl() {
        return control;
    }

    /**
     * @return Returns the mWidth.
     */
    public int getmWidth() {
        return mWidth;
    }

    /**
     * @param mWidth The mWidth to set.
     */
    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    /**
     * @return Returns the mHeight.
     */
    public int getmHeight() {
        return mHeight;
    }

    /**
     * @param mHeight The mHeight to set.
     */
    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    /**
     * @param w
     * @param h
     */
    public void setSize(int w, int h) {
        this.mWidth = w;
        this.mHeight = h;
    }

    /**
     * @return Returns the zoom.
     */
    public float getZoom() {
        return (slideshow ? fitZoom : pgPrintMode.getZoom());
    }

    /**
     * @param zoom The zoom to set.
     */
    public void setZoom(float zoom, int pointX, int pointY) {
        if (slideshow) {
            return;
        }
        pgPrintMode.setZoom(zoom, pointX, pointY);
    }

    /**
     * set fit size for PPT，Word view mode, PDf
     *
     * @param value fit size mode
     *              = 0, fit size of get minimum value of pageWidth / viewWidth and pageHeight / viewHeight;
     *              = 1, fit size of pageWidth
     *              = 2, fit size of PageHeight
     */
    public void setFitSize(int value) {
        if (slideshow) {
            return;
        }
        pgPrintMode.setFitSize(value);
    }

    /**
     * get fit size statue
     *
     * @return fit size statue
     * = 0, left/right and top/bottom don't alignment
     * = 1, top/bottom alignment
     * = 2, left/right alignment
     * = 3, left/right and top/bottom alignment
     */
    public int getFitSizeState() {
        if (slideshow) {
            return 0;
        }
        return pgPrintMode.getFitSizeState();
    }

    /**
     * @return Returns the pageSize.
     */
    public Dimension getPageSize() {
        return pgModel.getPageSize();
    }

    /**
     * @return Returns the doc.
     */
    public IDocument getRenderersDoc() {
        return pgModel.getRenderersDoc();
    }

    /**
     *
     */
    public PGSlide getCurrentSlide() {
        if (slideshow) {
            return pgModel.getSlide(slideIndex_SlideShow);
        } else {
            return pgPrintMode.getCurrentPGSlide();
        }

    }

    /**
     * @param value
     * @return true: finded  false: not finded
     */
    public boolean find(String value) {
        return false;
    }

    /**
     * need call function find first and finded
     *
     * @return
     */
    public boolean findBackward() {
        return false;
    }

    /**
     * need call function find first and finded
     *
     * @return
     */
    public boolean findForward() {
        return false;
    }


    /**
     *
     */
    public void resetSearchResult() {
    }

    /**
     *
     */
    public int getPageIndex() {
        return -1;
    }

    /**
     * @return Returns the pgEditor.
     */
    public PGEditor getEditor() {
        return editor;
    }

    /**
     * set animation duration(ms), should be called before begin slideshow
     *
     * @param duration
     */
    public void setAnimationDuration(int duration) {
    }

    /**
     * @return
     */
    public boolean hasNextSlide_Slideshow() {
        synchronized (this) {
            if (slideshow) {
                return slideIndex_SlideShow < pgModel.getSlideCount() - 1;
            }
            return false;
        }
    }

    /**
     * @return
     */
    public boolean hasPreviousSlide_Slideshow() {
        synchronized (this) {
            if (slideshow) {
                return slideIndex_SlideShow >= 1;
            }
            return false;
        }
    }

    /**
     * has next action or not
     *
     * @return
     */
    public boolean hasNextAction_Slideshow() {
        return false;
    }

    /**
     * has previous action or not
     *
     * @return
     */
    public boolean hasPreviousAction_Slideshow() {
        return false;
    }

    /**
     * show or hide shape one by one
     */
    public void slideShow(byte type) {
    }


    /**
     * slideshow end
     */
    public void endSlideShow() {
        synchronized (this) {
            if (slideshow) {
                control.getSysKit().getCalloutManager().setDrawingMode(MainConstant.DRAWMODE_NORMAL);
                setOnTouchListener(null);
                pgPrintMode.setVisibility(View.VISIBLE);
                Object bg = control.getMainFrame().getViewBackground();
                if (bg != null) {
                    if (bg instanceof Integer) {
                        setBackgroundColor((Integer) bg);
                    } else if (bg instanceof Drawable) {
                        setBackgroundDrawable((Drawable) bg);
                    }
                }
                currentIndex = slideIndex_SlideShow;
                slideshow = false;
                showSlide(currentIndex, false);
                // to picture
                post(new Runnable() {

                    @Override
                    public void run() {
                        ISlideShow iSlideshow = control.getSlideShow();
                        if (iSlideshow != null) {
                            iSlideshow.exit();
                        }
                    }
                });
            }
        }
    }

    /**
     * it's slideshowing or not.
     *
     * @return
     */
    public boolean isSlideShow() {
        return slideshow;
    }

    public PGModel getPGModel() {
        return pgModel;
    }

    /**
     * animation steps for current slide
     *
     * @param slideIndex(based 1)
     * @return
     */
    public int getSlideAnimationSteps(int slideIndex) {
        synchronized (this) {
            List<ShapeAnimation> shapeAnimLst = pgModel.getSlide(slideIndex - 1).getSlideShowAnimation();
            if (shapeAnimLst != null) {
                return shapeAnimLst.size() + 1;
            } else {
                return 1;
            }
        }
    }

    /**
     *
     */
    public void dispose() {
        control = null;
        currentSlide = null;
        pgModel.dispose();
        pgModel = null;
    }

    // 
    private boolean isConfigurationChanged;
    //
    private boolean init;
    //
    private int preShowSlideIndex = -1;
    // 当前显示的slide index值
    private int currentIndex = -1;
    // 组件的宽度
    private int mWidth;
    // 组件的高度
    private int mHeight;
    //current zoom value
    private float zoom = 1F;
    //
    private PGEditor editor;
    //
    private IControl control;
    //
    private PGSlide currentSlide;
    // PG model 后期需修改
    private PGModel pgModel;
    //
    private boolean slideshow;
    private int slideIndex_SlideShow;
    private float fitZoom = 1f;
    private Rect slideSize = null;
    private PGPrintMode pgPrintMode;
}
