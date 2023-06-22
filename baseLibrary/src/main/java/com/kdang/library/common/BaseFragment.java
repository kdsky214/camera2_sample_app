package com.kdang.library.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.kdang.library.common.util.SharedPreferenceUtil;

import java.util.Map;

//추상클래스 상속받아서 아래 내용을 사용할 수 있음
abstract public class BaseFragment<T extends ViewDataBinding> extends Fragment {
    public T binding;
    public int layoutResourceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layoutResourceId = getLayoutResourceId();
        binding = DataBindingUtil.inflate(inflater, layoutResourceId, container, false);
        binding.setLifecycleOwner(this);
        View v = binding.getRoot();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initObserver();
        initEventListener();
    }
    public abstract int getLayoutResourceId();
    public abstract void initView();
    public abstract void initObserver();
    public abstract void initEventListener();

    public String loadTodoJsonString(){
        if(this.getContext() != null){
            return SharedPreferenceUtil.getDayScheduleList(this.getContext());
        }else{
            return null;
        }
    }

    public void saveTodoJsonString(String jsonString){
        if(this.getContext() != null){
            SharedPreferenceUtil.saveDayScheduleList(this.getContext(), jsonString);
        }
    }

}
