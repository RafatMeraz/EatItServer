package com.practise.eatitserver.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.practise.eatitserver.R;
import com.practise.eatitserver.interfaces.ItemClickListener;
import com.practise.eatitserver.model.Request;
import com.practise.eatitserver.utils.Common;
import com.practise.eatitserver.viewholder.OrderViewHolder;

public class OrderStatusActivity extends AppCompatActivity {

    private RecyclerView orderRecyclerView;
    private FirebaseRecyclerAdapter<Request, OrderViewHolder> orderAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRequestDR;
    private MaterialSpinner orderStatusSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        initialization();
    }

    private void initialization() {
        mDatabase = FirebaseDatabase.getInstance();
        mRequestDR = mDatabase.getReference("Requests");
        orderRecyclerView = findViewById(R.id.ordersRecyclerView);
        orderRecyclerView.setHasFixedSize(true);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadOrders();
    }

    private void loadOrders() {

        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(mRequestDR,
                                Request.class)
                        .build();

        orderAdapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.order_layout, parent, false);
                return new OrderViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder orderViewHolder, int i, @NonNull Request request) {
                orderViewHolder.orderIdTV.setText(orderAdapter.getRef(i).getKey());
                orderViewHolder.orderStatusTV.setText(convertCodeToStatus(request.getStatus()));
                orderViewHolder.orderAddressTV.setText(request.getAddress());
                orderViewHolder.orderPhoneTV.setText(request.getPhone());

                orderViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                });
            }
        };
        orderAdapter.notifyDataSetChanged();
        orderRecyclerView.setAdapter(orderAdapter);
    }

    private String convertCodeToStatus(String status) {
        if (status.equals("0")){
            return "Placed";
        } else if (status.equals("1")){
            return "On my Way";
        } else {
            return "Shipped";
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        orderAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        orderAdapter.stopListening();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(orderAdapter.getRef(item.getOrder()).getKey(), orderAdapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)){
            deleteOrder(orderAdapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);

    }

    private void deleteOrder(String key) {
        mRequestDR.child(key).removeValue();
    }

    private void showUpdateDialog(String key, final Request item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please Choose status");

        View view = this.getLayoutInflater().inflate(R.layout.update_order_layout, null);
        orderStatusSpinner = view.findViewById(R.id.orderStatusSpinner);
        orderStatusSpinner.setItems("Placed", "On my way", "Shipped");
        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(orderStatusSpinner.getSelectedIndex()));

                mRequestDR.child(localKey).setValue(item);
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
