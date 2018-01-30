package com.miku.toast;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miku on 2017/12/26.
 */

public class ToastCompat extends Toast{

    private static final String TAG = "ToastCompat";

    public ToastCompat(Context context) {
        super(context);
    }

    /**
     * Make a standard toast that just contains a text view.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
     *                 {@link #LENGTH_LONG}
     *
     */
    public static Toast makeText(Context context, CharSequence text,int duration) {
        ToastCompat result = new ToastCompat(context);

        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources resources=context.getResources();
        View v = inflate.inflate(resources.getIdentifier("transient_notification", "layout", "android"), null);
        TextView tv = (TextView)v.findViewById(resources.getIdentifier("message", "id", "android"));
        tv.setText(text);
        result.setView(v);
        result.setDuration(duration);
        return result;
    }

    /**
     * Make a standard toast that just contains a text view with the text from a resource.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
     *                 {@link #LENGTH_LONG}
     *
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    public static Toast makeText(Context context,int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    @Override
    public void show() {
        if(checkIfNeedToHack()){
            tryToHack();
        }
        super.show();
    }

    protected boolean checkIfNeedToHack(){
        return Build.VERSION.SDK_INT==Build.VERSION_CODES.N_MR1;
    }

    private void tryToHack(){
        try {
            Object mTN=getFieldValue(this,"mTN");
            if(mTN!=null){
                boolean isSuccess=false;

                //a hack to some device which use the code between android 6.0 and android 7.1.1
                Object rawShowRunnable=getFieldValue(mTN,"mShow");
                if(rawShowRunnable!=null && rawShowRunnable instanceof Runnable){
                    isSuccess=setFieldValue(mTN,"mShow",new InternalRunnable((Runnable)rawShowRunnable));
                }

                // hack to android 7.1.1,these cover 99% devices.
                if(!isSuccess){
                    Object rawHandler=getFieldValue(mTN,"mHandler");
                    if(rawHandler!=null && rawHandler instanceof Handler){
                        isSuccess=setFieldValue(rawHandler,"mCallback",new InternalHandlerCallback((Handler)rawHandler));
                    }
                }

                if(!isSuccess){
                    Log.e(TAG,"tryToHack error.");
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private class InternalRunnable implements Runnable{
        private final Runnable mRunnable;

        public InternalRunnable(Runnable mRunnable) {
            this.mRunnable = mRunnable;
        }

        @Override
        public void run() {
            try {
                this.mRunnable.run();
            }catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private class InternalHandlerCallback implements Handler.Callback{
        private final Handler mHandler;

        public InternalHandlerCallback(Handler mHandler) {
            this.mHandler = mHandler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            try {
                mHandler.handleMessage(msg);
            }catch (Throwable e) {
               e.printStackTrace();
            }
            return true;
        }
    }

    private static boolean setFieldValue(Object object, String fieldName, Object newFieldValue) {
        Field field = getDeclaredField(object,fieldName);
        if(field!=null){
            try {
                int accessFlags=field.getModifiers();
                if(Modifier.isFinal(accessFlags)){
                    Field modifiersField = Field.class.getDeclaredField("accessFlags");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                }
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(object, newFieldValue);
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    private static Object getFieldValue(Object obj, final String fieldName){
        Field field = getDeclaredField(obj,fieldName);
        return getFieldValue(obj,field);
    }

    private static Object getFieldValue(Object obj,Field field){
        if(field!=null){
            try {
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field.get(obj);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Field getDeclaredField(final Object obj, final String fieldName) {
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException e) {
                continue;// new add
            }
        }
        return null;
    }
}
