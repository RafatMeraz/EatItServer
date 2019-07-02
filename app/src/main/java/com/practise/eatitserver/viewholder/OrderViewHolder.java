package com.practise.eatitserver.viewholder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.practise.eatitserver.R;
import com.practise.eatitserver.interfaces.ItemClickListener;
import com.practise.eatitserver.utils.Common;

public class OrderViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener, View.OnCreateContextMenuListener {

    public TextView orderIdTV, orderStatusTV, orderPhoneTV, orderAddressTV;
    private ItemClickListener itemClickListener;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        orderIdTV = itemView.findViewById(R.id.order_item_id);
        orderStatusTV = itemView.findViewById(R.id.order_status);
        orderPhoneTV = itemView.findViewById(R.id.order_phone);
        orderAddressTV = itemView.findViewById(R.id.order_address);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0,0, getAdapterPosition(), Common.UPDATE);
        menu.add(0,1, getAdapterPosition(), Common.DELETE);
    }
}
