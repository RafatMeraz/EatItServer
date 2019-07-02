package com.practise.eatitserver.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.practise.eatitserver.R;
import com.practise.eatitserver.interfaces.ItemClickListener;
import com.practise.eatitserver.model.Category;
import com.practise.eatitserver.model.Food;
import com.practise.eatitserver.utils.Common;
import com.practise.eatitserver.viewholder.FoodViewHolder;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class FoodListActivity extends AppCompatActivity {

    private RecyclerView foodRecyclerView;
    private FloatingActionButton addFoodFab;
    private ConstraintLayout rootLayout;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mFoodDR;
    private FirebaseStorage mStorage;
    private StorageReference mfoodSR;
    private Button selectImgButton, uploadButton;
    private EditText foodNameET, foodDescriptionET, foodPriceET, foodDicountET;
    private final int PICK_IMG_REQUEST = 75;
    private Uri saveUri;
    private Food newFood;


    String categoryId = "";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        initialization();
    }

    private void initialization() {
        mDatabase = FirebaseDatabase.getInstance();
        mFoodDR = mDatabase.getReference("Foods");
        mStorage = FirebaseStorage.getInstance();
        mfoodSR = mStorage.getReference();

        foodRecyclerView = findViewById(R.id.foodListRecyclerView);
        foodRecyclerView.setHasFixedSize(true);
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        rootLayout = findViewById(R.id.foodListRootLayout);

        addFoodFab = findViewById(R.id.addFoodItemFab);
        addFoodFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFoodDialog();
            }
        });

        if (getIntent() != null){
            categoryId = getIntent().getStringExtra("categoryId");
        }
        if (!categoryId.isEmpty()){
            loadFoodList(categoryId);
        }
        if (getIntent() == null){
            DynamicToast.makeError(this, "Error in Category Id", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddFoodDialog() {
        final AlertDialog.Builder addMenuDialog = new AlertDialog.Builder(this);
        addMenuDialog.setTitle("Add new Food");
        addMenuDialog.setMessage("Please Add These Info");

        LayoutInflater inflater = this.getLayoutInflater();
        View addMenuLayout = inflater.inflate(R.layout.add_new_food_layout, null);
        selectImgButton = addMenuLayout.findViewById(R.id.selectImageButton);
        uploadButton = addMenuLayout.findViewById(R.id.uploadButton);
        foodNameET = addMenuLayout.findViewById(R.id.newFoodNameET);
        foodDescriptionET = addMenuLayout.findViewById(R.id.newFoodDescriptionET);
        foodPriceET = addMenuLayout.findViewById(R.id.newFoodPriceET);
        foodDicountET = addMenuLayout.findViewById(R.id.newFoodDiscountET);

        selectImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        addMenuDialog.setView(addMenuLayout);
        addMenuDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        addMenuDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (newFood != null){
                    mFoodDR.push().setValue(newFood);
                    Snackbar.make(rootLayout, "New Food "+newFood.getName()+" was added!", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        addMenuDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        addMenuDialog.show();

    }

    private void loadFoodList(String categoryId) {
        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>()
                        .setQuery(
                                mFoodDR.orderByChild("menuId").equalTo(categoryId)
                                , Food.class)
                        .build();

        foodAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);

                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int i, @NonNull Food food) {
                foodViewHolder.foodNameTV.setText(food.getName());
                Picasso.get().load(food.getImage()).into(foodViewHolder.foodIV);

                final Food local = food;
                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
//                        Intent foodDetailIntent = new Intent(getApplicationContext(), FoodDetail.class);
//                        foodDetailIntent.putExtra("foodId", foodAdapter.getRef(position).getKey());
//                        startActivity(foodDetailIntent);
                    }
                });
            }
        };
        foodAdapter.notifyDataSetChanged();
        foodRecyclerView.setAdapter(foodAdapter);
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMG_REQUEST);
    }

    private void uploadImage() {
        if (saveUri != null){
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            final String imageName = UUID.randomUUID().toString();
            final StorageReference imgFolder = mfoodSR.child("images/"+imageName);
            imgFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            DynamicToast.makeSuccess(getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                            imgFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newFood = new Food(foodNameET.getText().toString(), uri.toString(), foodDescriptionET.getText().toString(),
                                            foodPriceET.getText().toString(), foodDicountET.getText().toString(), categoryId);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            DynamicToast.makeError(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded "+progress+"%");
                        }
                    });
        }
        else {
            DynamicToast.makeError(getApplicationContext(), "Please select an image!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            saveUri = data.getData();
            selectImgButton.setText("Image Selected!");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        foodAdapter.startListening();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateFoodDialog(foodAdapter.getRef(item.getOrder()).getKey(), foodAdapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)){
            deleteFood(foodAdapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {
        mFoodDR.child(key).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DynamicToast.makeSuccess(getApplicationContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DynamicToast.makeSuccess(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showUpdateFoodDialog(final String key, final Food item) {
        final AlertDialog.Builder addMenuDialog = new AlertDialog.Builder(this);
        addMenuDialog.setTitle("Edit Food");
        addMenuDialog.setMessage("Please Add These Info");

        LayoutInflater inflater = this.getLayoutInflater();
        View addMenuLayout = inflater.inflate(R.layout.add_new_food_layout, null);
        selectImgButton = addMenuLayout.findViewById(R.id.selectImageButton);
        uploadButton = addMenuLayout.findViewById(R.id.uploadButton);
        foodNameET = addMenuLayout.findViewById(R.id.newFoodNameET);
        foodDescriptionET = addMenuLayout.findViewById(R.id.newFoodDescriptionET);
        foodPriceET = addMenuLayout.findViewById(R.id.newFoodPriceET);
        foodDicountET = addMenuLayout.findViewById(R.id.newFoodDiscountET);

        foodNameET.setText(item.getName());
        foodDescriptionET.setText(item.getDescription());
        foodPriceET.setText(item.getPrice());
        foodDicountET.setText(item.getDiscount());

        selectImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFoodImage(item);
            }
        });

        addMenuDialog.setView(addMenuLayout);
        addMenuDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        addMenuDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (saveUri != null){
                    item.setImage(saveUri.toString());
                } else {
                    item.setImage(item.getImage());
                }

                item.setName(foodNameET.getText().toString());
                item.setPrice(foodPriceET.getText().toString());
                item.setDescription(foodDescriptionET.getText().toString());
                item.setDiscount(foodDicountET.getText().toString());

                mFoodDR.child(key).setValue(item).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                DynamicToast.makeSuccess(getApplicationContext(), item.getName()+"updated Successfully!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("ERROR: ", e.getMessage());
                                DynamicToast.makeSuccess(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
//                Snackbar.make(rootLayout, "New food "+item.getName()+" was added!", Snackbar.LENGTH_SHORT)
//                            .show();

            }
        });
        addMenuDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        addMenuDialog.show();

    }

    private void changeFoodImage(final Food item) {
        if (saveUri != null){
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            final String imageName = UUID.randomUUID().toString();
            final StorageReference imgFolder = mfoodSR.child("images/"+imageName);
            imgFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            DynamicToast.makeSuccess(getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                            imgFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            DynamicToast.makeError(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded "+progress+"%");
                        }
                    });
        }
        else {
            DynamicToast.makeError(getApplicationContext(), "Please select an image!", Toast.LENGTH_SHORT).show();
        }
    }
}
