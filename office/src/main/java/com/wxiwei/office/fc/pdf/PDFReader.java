/*
 * 文件名称:          PDFReader.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:30:30
 */
package com.wxiwei.office.fc.pdf;

import com.wxiwei.office.common.ICustomDialog;
import com.wxiwei.office.constant.EventConstant;
import com.wxiwei.office.res.ResKit;
import com.wxiwei.office.system.AbstractReader;
import com.wxiwei.office.system.IControl;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * reader PDF document 
 * <p>
 * <p>
 * Read版本:        Read V1.0
 * <p>
 * 作者:            ljj8494
 * <p>
 * 日期:            2012-9-19
 * <p>
 * 负责人:          ljj8494
 * <p>
 * 负责小组:         
 * <p>
 * <p>
 */
public class PDFReader extends AbstractReader
{
    /**
     * 
     * @param filePath
     */
    public PDFReader(IControl control, String filePath) throws Exception
    {
        this.control = control;
        this.filePath = filePath;
    }
     
    /**
     *
     */
    public Object getModel() throws Exception
    {        
        control.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false);
        //lib = new PDFLib(filePath);
        lib = PDFLib.getPDFLib();
        lib.openFileSync(filePath);
        return lib;
    }

    /**
     * 
     *
     */
    public void dispose()
    {
        super.dispose();
        lib = null;
        control = null;
    }
    
    //
    private String filePath;
    //
    private PDFLib lib;
    //
    //private AlertDialog.Builder alertBuilder;
    //
    //private EditText pwView;

}
