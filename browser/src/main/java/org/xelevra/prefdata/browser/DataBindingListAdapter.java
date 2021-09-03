package org.xelevra.prefdata.browser;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class DataBindingListAdapter<ItemType, BindingType extends ViewDataBinding> extends BaseAdapter implements DataBindingAdapter<BindingType>{
    protected List<ItemType> items;
    private final int layoutId, itemId;
    private DataBindingAdapter<BindingType> realisation;

    public DataBindingListAdapter(List<ItemType> items, int layoutId, int itemId){
        this.items = items;
        this.layoutId = layoutId;
        this.itemId = itemId;
    }

    public DataBindingListAdapter(List<ItemType> items, int layoutId, int itemId, DataBindingAdapter<BindingType> realisation){
        this(items, layoutId, itemId);
        this.realisation = realisation;
    }

    public void notifyDataSetChanged(List<ItemType> items){
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewDataBinding binding;
        if(convertView == null){
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
            onCreate((BindingType) binding);
        } else {
            binding = (ViewDataBinding) convertView.getTag();
        }
        binding.setVariable(itemId, items.get(position));
        onBind((BindingType) binding, position);
        binding.executePendingBindings();
        return binding.getRoot();
    }

    public void onCreate(final BindingType binding){
        if(realisation != null) realisation.onCreate(binding);
    }

    public void onBind(final BindingType binding, final int position){
        if(realisation != null) realisation.onBind(binding, position);
    }
}
