package com.mv.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Adapter.ProcessDetailAdapter;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessDeatailActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    String comment;
    String isSave;
    String msg;
    Boolean manditoryFlag = false;
    int i;
    private PreferenceHelper preferenceHelper;
    ArrayList<Task> taskList = new ArrayList<>();

    String timestamp;
    Activity context;

    Button submit, save,approve,reject;

    ProcessDetailAdapter adapter;
    RecyclerView rvProcessDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_deatail);
        context = this;
        preferenceHelper = new PreferenceHelper(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

      //  setActionbar(getString(R.string.Process_Detail));

        if (getIntent().getSerializableExtra(Constants.PROCESS_ID) != null) {
            taskList = getIntent().getParcelableArrayListExtra(Constants.PROCESS_ID);
        }
        initViews();
    }

    private void initViews() {

        setActionbar(getString(R.string.Task_List));

        rvProcessDetail = (RecyclerView) findViewById(R.id.rv_process_detail);
        adapter = new ProcessDetailAdapter(this, taskList);
        rvProcessDetail.setHasFixedSize(true);
        rvProcessDetail.setLayoutManager(new LinearLayoutManager(this));
        rvProcessDetail.setAdapter(adapter);
        timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());

        submit = (Button) findViewById(R.id.btn_submit);
        submit.setOnClickListener(this);

        save = (Button) findViewById(R.id.btn_save);
        save.setOnClickListener(this);

        approve = (Button) findViewById(R.id.btn_approve);
        approve.setOnClickListener(this);

        reject = (Button) findViewById(R.id.btn_reject);
        reject.setOnClickListener(this);
        if(preferenceHelper.getString(Constants.PROCESS_TYPE).equals(Constants.APPROVAL_PROCESS))
        {
            approve.setVisibility(View.VISIBLE);
            reject.setVisibility(View.VISIBLE);
            submit.setVisibility(View.GONE);
            save.setVisibility(View.GONE);

        }else if(preferenceHelper.getString(Constants.PROCESS_TYPE).equals(Constants.MANGEMENT_PROCESS))
        {
            approve.setVisibility(View.GONE);
            reject.setVisibility(View.GONE);
            submit.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
        }
    }

    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_submit:
                submitAllData();
                break;
            case R.id.btn_save:


                for (int i = 0; i < taskList.size(); i++) {
                /*    if(taskList.get(i).getIsSave().equals(Constants.PROCESS_STATE_SUBMIT))
                    taskList.get(i).setIsSave(Constants.PROCESS_STATE_MODIFIED);
                    else*/
                        taskList.get(i).setIsSave(Constants.PROCESS_STATE_SAVE);
                    taskList.get(i).setTimestamp__c(timestamp);
                    taskList.get(i).setMTUser__c(User.getCurrentUser(context).getId());
                    if (preferenceHelper.getBoolean(Constants.NEW_PROCESS))
                        taskList.get(i).setId(null);


                }
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(taskList);
                TaskContainerModel taskContainerModel = new TaskContainerModel();
                taskContainerModel.setTaskListString(json);

                taskContainerModel.setTaskType(Constants.TASK_ANSWER);

                taskContainerModel.setIsSave(Constants.PROCESS_STATE_SAVE);
                 taskContainerModel.setMV_Process__c(taskList.get(0).getMV_Process__c());
                 if(preferenceHelper.getBoolean(Constants.NEW_PROCESS)) {

                     //if process is new  INSERT it with timestmap as id
                     taskContainerModel.setUnique_Id( String.valueOf(Calendar.getInstance().getTimeInMillis()));
                     AppDatabase.getAppDatabase(context).userDao().insertTask(taskContainerModel);
                 }
                 else {
                     //if process is not new  UPDATE it with exiting id
                     taskContainerModel.setUnique_Id(preferenceHelper.getString(Constants.UNIQUE));
                     AppDatabase.getAppDatabase(context).userDao().updateTask(taskContainerModel);
                 }

                finish();


                break;

            case R.id.btn_approve:
                comment="";
                isSave="true";
                sendApprovedData();
                break;
            case R.id.btn_reject:
                showDialog();
                break;

        }
    }
    public void showDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProcessDeatailActivity.this);
        alertDialog.setTitle(getString(R.string.comments));
        alertDialog.setMessage("Please Enter Comment");

        final EditText input = new EditText(ProcessDeatailActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isSave="false";
                        comment = input.getText().toString();
                        if(!comment.isEmpty())
                        {
                            sendApprovedData();
                        }
                        else
                        {
                            Utills.showToast("Please Enter Comment",ProcessDeatailActivity.this);
                        }

                    }

                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
    public void saveDataToList(Task answer, int position) {
        taskList.set(position, answer);
    }

    private void submitAllData() {
        manditoryFlag = false;


        for (int i = 0; i < taskList.size(); i++) {
            taskList.get(i).setTimestamp__c(timestamp);
            taskList.get(i).setMTUser__c(User.getCurrentUser(context).getId());
            taskList.get(i).setIsSave(Constants.PROCESS_STATE_SUBMIT);
            if (preferenceHelper.getBoolean(Constants.NEW_PROCESS))
                taskList.get(i).setId(null);
            if (taskList.get(i).getIs_Response_Mnadetory__c() && taskList.get(i).getTask_Response__c().equals("")) {
                manditoryFlag = true;
                msg = "please check " + taskList.get(i).getTask_Text__c();
                break;

            }

        }
        if (!manditoryFlag) {
            // AppDatabase.getAppDatabase(context).userDao().insertTask(taskList);
            if (Utills.isConnected(this))
            callApiForSubmit(taskList);
            else
                Utills.showToast(getString(R.string.error_no_internet),this);
        } else
            Utills.showToast(msg, context);
    }


    private void callApiForSubmit(ArrayList<Task> temp) {


        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(temp);
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray(json);

            jsonObject.put("listtaskanswerlist", jsonArray);

            Utills.showProgressDialog(context);
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/ApproveCommentforProcess", gsonObject).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        JSONObject response1 = new JSONObject(response.body().string());

                        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                        String json = gson.toJson(taskList);
                        TaskContainerModel taskContainerModel = new TaskContainerModel();
                        taskContainerModel.setTaskListString(json);
                        taskContainerModel.setTaskType(Constants.TASK_ANSWER);
                        taskContainerModel.setUnique_Id(preferenceHelper.getString(Constants.UNIQUE));

                        taskContainerModel.setIsSave(Constants.PROCESS_STATE_SUBMIT);
                        taskContainerModel.setMV_Process__c(taskList.get(0).getMV_Process__c());
                        AppDatabase.getAppDatabase(context).userDao().deleteSingleTask(preferenceHelper.getString(Constants.UNIQUE),taskContainerModel.getMV_Process__c());
                       // AppDatabase.getAppDatabase(context).userDao().updateTask(taskContainerModel);
                        finish();

 
                    } catch (Exception e) {
                        e.printStackTrace();


                    }


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();

                    Utills.showToast(getString(R.string.error_something_went_wrong), context);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
           // tvResult.setText(data.getIntExtra("result",-1)+"");
            taskList=data.getParcelableArrayListExtra(Constants.PROCESS_ID);
            adapter = new ProcessDetailAdapter(this, taskList);
            rvProcessDetail.setAdapter(adapter);

        }

    }
    private void sendApprovedData() {
        if (Utills.isConnected(this)) {
            try {


                Utills.showProgressDialog(this, getString(R.string.share_post), getString(R.string.progress_please_wait));
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("uniqueId", taskList.get(0).getUnique_Id__c());
                jsonObject1.put("ApprovedBy", User.getCurrentUser(getApplicationContext()).getId());

                JSONArray jsonArrayAttchment = new JSONArray();

                // jsonObject1.put("MV_User", User.getCurrentUser(mContext).getId());
                jsonObject1.put("isApproved", isSave);
                jsonObject1.put("comment", comment);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/ApproveCommentforProcess", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            Utills.showToast(getString(R.string.submitted_successfully), ProcessDeatailActivity.this);
                            finish();
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), ProcessDeatailActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), ProcessDeatailActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), ProcessDeatailActivity.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), ProcessDeatailActivity.this);
        }
    }
}