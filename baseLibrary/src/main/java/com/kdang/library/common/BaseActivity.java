package com.kdang.library.common;

import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowMetrics;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

//추상클래스 상속받아서 아래 내용을 사용할 수 있음
abstract public class BaseActivity<T extends ViewDataBinding> extends AppCompatActivity {
    public T binding;
    public int layoutResourceId;
    public int densityDpi;
    public float density;
    public double displayWidth  = 0;
    public double displayHeight = 0;
    public double statusBarHeight = 0;
    public double bottomBarHeight = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutResourceId = getLayoutResourceId();
        binding = DataBindingUtil.setContentView(this, layoutResourceId);
        binding.setLifecycleOwner(this);
        initPermission();
    }
    public abstract int getLayoutResourceId();
    public abstract void initPermission();
    public abstract void initView();
    public abstract void initObserver();
    public abstract void initEventListener();

    private boolean isAllGranted = true;
    public void checkPermissionList(String[] permissionList) {
        if(permissionList == null){
            //permission 없으면 실행
            initialize();
        }else{
            try {
                Log.e("base", "permissionList: "+permissionList);
                for (int index = 0; index < permissionList.length; index++){
                    String permission = permissionList[index];
                    if(PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(permission)){
                        //permission 권한이 동의하지 않았을떄 런처 실행
                        isAllGranted = false;
                        requestPermissionLauncher.launch(permission);
                        Log.e("base","requestPermissionLauncher");
                    }
                }
            }finally {
                if(isAllGranted){
                    //permission 권한이 다 동의 하였을 때 실행
                    Log.e("base","isAllGranted initialize: "+isAllGranted);
                    initialize();
                }else{
                    Log.e("base","Finish isAllGranted : "+isAllGranted);
                }
            }
        }
    }

    private void initialize(){
        //checkDisplaySize();
        checkDisplay();
        initView();
        initObserver();
        initEventListener();
    }


    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    Log.e("base", "onActivityResult : " + result);
                    if(!result){
                        isAllGranted = false;
                    }
                }
            }
    );

    private void checkDisplay(){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        double widthPixels  = 0;
        double heightPixels = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            Rect bounds = windowMetrics.getBounds();
            widthPixels = bounds.width();
            heightPixels = bounds.height();
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            defaultDisplay.getRealMetrics(displayMetrics);
            widthPixels  = displayMetrics.widthPixels;
            heightPixels = displayMetrics.heightPixels;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            int resourceIdBottom = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceIdBottom > 0) bottomBarHeight = getResources().getDimensionPixelSize(resourceIdBottom);
            displayHeight = (heightPixels - (statusBarHeight + bottomBarHeight));
            displayWidth  = widthPixels;
        }else{
            defaultDisplay.getMetrics(displayMetrics);
            widthPixels = displayMetrics.widthPixels;
            heightPixels = displayMetrics.heightPixels;
        }
        densityDpi = displayMetrics.densityDpi;
        density    = displayMetrics.density;

//        Log.e("base", "pixel = ("+widthPixels+"x"+heightPixels+") (densityDPI : "+densityDpi+") (density:"+density+")");
//        Log.e("base", "display = ("+displayWidth+"x"+displayHeight+") (densityDPI : "+densityDpi+") (density:"+density+")");
        double widthInches = widthPixels / (double) displayMetrics.densityDpi;
        double heightInches = heightPixels / (double) displayMetrics.densityDpi;
//        Log.e("base", "display = ("+widthInches+" x "+heightInches+")");
        double inch = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));

    }

    public void showToast(String message, boolean flag){
        Toast.makeText(this, message, flag ? Toast.LENGTH_SHORT:Toast.LENGTH_LONG).show();
    }

}
