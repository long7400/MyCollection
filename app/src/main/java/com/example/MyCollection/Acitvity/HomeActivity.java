package com.example.MyCollection.Acitvity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MyCollection.Interface.ItemClickListener;
import com.example.MyCollection.Models.ImageItem;
import com.example.MyCollection.ViewHolder.MenuViewHolder;
import com.example.MyCollection.Common.Container;
import com.example.MyCollection.Models.Category;
import com.example.foodnews.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import info.hoang8f.widget.FButton;
import io.paperdb.Paper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static FirebaseDatabase database;
    public static DatabaseReference databaseReference;
    public static FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://foodnews-ea4e2.appspot.com");

    public static RecyclerView recyclerview;
    public static RecyclerView.LayoutManager layoutManager;
    public static File file;

    public static CardView cardView;
    public static FButton fUpdate, fSelete,btnTest;
    public static MaterialEditText edtName;

    static String loginId;

    FloatingActionButton fab;
    TextView nameUser;
    FirebaseAuth fAuth;
    ImageView imageUpdate;
    public static String categorID;

    NotificationCompat.Builder builder;
    NotificationManagerCompat notificationManager;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fab){
                Intent openMenu = new Intent(HomeActivity.this, CreateCategoryActivity.class);
                startActivity(openMenu);
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        builder = new NotificationCompat.Builder(this,"0007");
        notificationManager = NotificationManagerCompat.from(HomeActivity.this);
        NotificationChannel channel = new NotificationChannel("0008","NOTIFICATION", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        nameUser = (TextView)hView.findViewById(R.id.name_User);
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();

        String[] name =user.getEmail().split("@");
        nameUser.setText(name[0]);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        cardView = findViewById(R.id.card_item);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView.bringToFront();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Container.CATEGORY);
        databaseReference.keepSynced(true);

        recyclerview = findViewById(R.id.recyclerview_menu);
        recyclerview.setLayoutManager(new GridLayoutManager(HomeActivity.this,2));
        layoutManager = new LinearLayoutManager(HomeActivity.this);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerview.getContext(),R.anim.anim_layout_fall_down);
        recyclerview.setLayoutAnimation(controller);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(onClickListener);
        loginId = LoginActivity.userID;
        LoadMenu(loginId);

        loadMenu2();
    }

    public void LoadMenu(String loginID){

        Query searchByUser = databaseReference.orderByChild("gmail").equalTo(loginID);
        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(searchByUser, Category.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final MenuViewHolder menuViewHolder, int position,
                                            @NonNull final Category model) {

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                storageRef.child(model.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        uri.toString();
                        file = new File(String.valueOf(uri.toString()));
                        model.setImage(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });

                Picasso.get().load(model.getImage()).placeholder(R.drawable.icons8_hamburger_100).fit().into(MenuViewHolder.imageViiew);
                MenuViewHolder.txtMenuTitle.setText(model.getName());
                final Category clicked = model;
                System.out.println(clicked.toString());

                MenuViewHolder.setOnClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodIntent = new Intent(HomeActivity.this, ViewImageActivity.class);
                        foodIntent.putExtra("CategoryId", adapter.getRef(position).getKey());
                        categorID = adapter.getRef(position).getKey();
                        startActivity(foodIntent);
                    }
                });
            }
            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
                return new MenuViewHolder(view);
            }
        };
        Paper.init(this);
    }

    private void loadMenu2() {
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerview.setAdapter(adapter);
        recyclerview.getAdapter().notifyDataSetChanged();
        recyclerview.scheduleLayoutAnimation();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
        Activity context = HomeActivity.this;
        context.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawable = findViewById(R.id.drawer_layout);
        drawable.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getTitle().equals(Container.UPDATE)){


            showDialogUpdate( adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }else if(item.getTitle().equals(Container.DELETE)){

            builder.setSmallIcon(R.drawable.icons8_hamburger_100);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Success!! Xóa thành công"));
            notificationManager.notify(0062,builder.build());

            deleteCategory( adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {
        databaseReference = FirebaseDatabase.getInstance().getReference(Container.CATEGORY);
        databaseReference.child(key).removeValue();
    }

    private void showDialogUpdate(String key, Category cate) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
        dialog.setTitle("UPDATE CATEGORY");
        dialog.setMessage("Chưa biết ghi gì");

        LayoutInflater inflater = this.getLayoutInflater();
        View addMenu = inflater.inflate(R.layout.add_category, null);

        fUpdate = addMenu.findViewById(R.id.btnUpdate);
        fSelete = addMenu.findViewById(R.id.btnSelect);
        edtName = addMenu.findViewById(R.id.nameCate);
        imageUpdate = addMenu.findViewById(R.id.imageUpdate);

        edtName.setText(cate.getName());

        fSelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

        fUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setSmallIcon(R.drawable.icons8_hamburger_100);
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Success!! Đã tải ảnh"));
                notificationManager.notify(0064,builder.build());

                updateCategory(cate);
            }
        });

        dialog.setView(addMenu);
        dialog.setIcon(R.drawable.icons8_hamburger_100);

        dialog.setPositiveButton("YES" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                builder.setSmallIcon(R.drawable.icons8_hamburger_100);
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Success!! Cập nhập thành công"));
                notificationManager.notify(0061,builder.build());


                cate.setGmail(cate.getGmail());
                cate.setName(String.valueOf(edtName.getText()));

                databaseReference = FirebaseDatabase.getInstance().getReference(Container.CATEGORY);
                databaseReference.child(key).setValue(cate);
            }
        });

        dialog.setNegativeButton("NO" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateCategory(Category cate) {
        ProgressDialog dialogUpload = new ProgressDialog(this);
        dialogUpload.setMessage("Uploading...");
        dialogUpload.show();

        Calendar calendar = Calendar.getInstance();

        StorageReference mountainsRef = storageRef.child("image" + calendar.getTimeInMillis()+".png");
        imageUpdate.setDrawingCacheEnabled(true);
        imageUpdate.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageUpdate.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = mountainsRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(HomeActivity.this, "Fail", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        cate.setImage(uri.toString());
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double process = ( 100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                dialogUpload.setMessage("Uploaded"+process+"%");
                if(process == 100.0d){
                    dialogUpload.dismiss();
                }
            }
        });
    }

    private void requestPermission(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(HomeActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                openImagePicker();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(HomeActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private void openImagePicker(){
        TedBottomPicker.OnImageSelectedListener imageSelectedListener = new TedBottomPicker.OnImageSelectedListener(){
            @Override
            public void onImageSelected(Uri uri) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageUpdate.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(HomeActivity.this)
                .setOnImageSelectedListener(imageSelectedListener)
                .create();
        tedBottomPicker.show(getSupportFragmentManager());
    }

}