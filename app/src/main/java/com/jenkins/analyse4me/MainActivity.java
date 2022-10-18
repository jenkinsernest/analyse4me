package com.jenkins.analyse4me;

import android.Manifest;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;


import android.text.Editable;
import android.text.TextWatcher;


import Util.Factory;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cn.pedant.SweetAlert.SweetAlertDialog;
import view_model.brains;

import com.jenkins.analyse4me.adapter.Adapter;
import com.jenkins.analyse4me.databinding.ActivityMainBinding;

import com.jenkins.analyse4me.model.listItem;
import com.jenkins.analyse4me.model.messageModel;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.List;

/*
@Author Ernest Jenkins

*/

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener
, Adapter.EventHanlder, brains.EventHanlder{

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private List<messageModel> responseList;
    RecyclerView recyclerView;
    Adapter adapter;
    EditText search;
    private SwipeRefreshLayout swipeRefreshLayout;
    SweetAlertDialog pDialog;
    LinkedHashSet<String> hash= new LinkedHashSet<String>();

       public  TextView total_month;
       public  TextView total_amount;

       public int count=0;
       public boolean load=true;
       brains viewModel;
       DecimalFormat formatter = new DecimalFormat("#,###,###");


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle("Test");
        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this,
                new  Factory(getApplication(), this)).get(brains.class);


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_);
        swipeRefreshLayout.setDistanceToTriggerSync(800);
        swipeRefreshLayout.setOnRefreshListener(this);

        // Configure the refreshing colors

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);



        search=findViewById(R.id.search);
        total_month=findViewById(R.id.t_months);
        total_amount=findViewById(R.id.t_amount);




        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               // System.out.println("working");

if(viewModel.smsLiveData.getValue()==null){

    if(!viewModel.empty_search_msg_shown) {
        show_msg("Sorry! indox is empty or app access denied \n\n Noting to search...");

        viewModel.empty_search_msg_shown=true;  //sets to true so prompt does not show again until
        //app next restart
    }

}
else {

    adapter.getFilter().filter(charSequence);
}
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });





        recyclerView = findViewById(R.id.report_list);

        GridLayoutManager linearLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(linearLayoutManager);



        viewModel.smsLiveData.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {


                count=viewModel.count;
                List<listItem> mItems;

                mItems = (List<listItem>)o;

                if(count>1) {
                    total_month.setText(count + " Months");
                }
                else if(count==0){
                    total_month.setText(count + " Month");

                }
                else{
                    total_month.setText(count + " Month");

                }
                total_amount.setText(formatter.format(viewModel.sum_amount) + " AED");




    adapter = new Adapter(MainActivity.this,responseList,hash ,mItems, total_month,total_amount,
                        count, MainActivity.this );

                recyclerView.setAdapter(adapter);

            }

        });





        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS) ==
        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            viewModel.getSms_data();
               // async.execute();
           //
        } else if (shouldShowRequestPermissionRationale("the permission is needed")) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
           // showInContextUI(...);
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
          requestPermissions(
                    new String[] { Manifest.permission.READ_SMS,Manifest.permission.READ_CONTACTS  },
                    3);
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 3:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    viewModel.getSms_data();
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }







    @Override
    public void onRefresh() {

        swipeRefreshLayout.setRefreshing(false);

        viewModel.drag_down_refresh();
    }


    public void show_msg(String msg) {
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        pDialog.setTitleText(msg);
        pDialog.setCancelable(false);
        pDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
        pDialog.show();
    }

    public void showLoader() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#59c8db"));
        pDialog.setTitleText("Fetching data...");
        pDialog.setCancelable(false);
        pDialog.show();
    }


    public void hideLoader() {
        if (pDialog!=null){
            pDialog.cancel();
        }
    }


    @Override
    public void show_loading() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                showLoader();
            }
        });
    }

    @Override
    public void close_loading() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                hideLoader();
            }
        });
    }

    @Override
    public void show_msg_inter(String a) {
        show_msg(a);
    }


    @Override
    public void Update_ui() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                total_month.setText(0 + " Month");
                total_amount.setText("0 AED");
            }
        });
    }


    @Override
    public void Update_ui_pos(int month_count, BigDecimal total) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // Stuff that updates the UI
                total_amount.setText(formatter.format(total) + " AED");


                if(month_count>1) {
                    total_month.setText(month_count + " Months");
                }
                else{
                    total_month.setText(month_count + " Month");
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // System.out.println("asyn status resume= " +async.getStatus());

        viewModel.refresh_data();

    }










    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
      //  getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}