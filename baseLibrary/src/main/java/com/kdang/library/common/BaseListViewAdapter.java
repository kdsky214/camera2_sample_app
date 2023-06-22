package com.kdang.library.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public class BaseListViewAdapter<T extends ViewDataBinding> extends BaseAdapter {
    T binding;
    int layoutResourceId;
    public BaseListViewAdapter(int resId){
        this.layoutResourceId = resId;

    }
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutResourceId, parent, false);
        if(convertView == null){
            convertView = binding.getRoot();
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        }

        return convertView;
    }
    public void refresh() {
        notifyDataSetChanged();
    }

    class ViewHolder {
        private View baseView = null;
        public ViewHolder(View view) {
            baseView = view;
        }
    }
}
