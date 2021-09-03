package org.xelevra.prefdata.browser;

import androidx.databinding.ViewDataBinding;

public interface DataBindingAdapter<BindingType extends ViewDataBinding> {
    void onCreate(final BindingType binding);
    void onBind(final BindingType binding, final int position);
}
