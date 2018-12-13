package com.mv.Adapter;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.AssetAllocatedListActivity;
import com.mv.Activity.AssetAllocation_Activity;
import com.mv.Activity.AssetApprovalActivity;
import com.mv.Model.Asset;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nanostuffs on 19-03-2018.
 */

public class ExpandableAssetListAdapter extends BaseExpandableListAdapter {
    private AssetAllocatedListActivity _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, ArrayList<Asset>> _listDataChild;
    private AssetAllocatedListActivity _activity;

    public ExpandableAssetListAdapter(Activity context, ArrayList<String> listDataHeader,
                                      HashMap<String, ArrayList<Asset>> listChildData) {
        this._context = (AssetAllocatedListActivity) context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._activity = (AssetAllocatedListActivity) context;
        PreferenceHelper preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        ArrayList<Asset> assets = this._listDataChild.get(this._listDataHeader.get(groupPosition));
        if (assets != null && !assets.isEmpty()) {
            return assets.get(childPosititon);
        } else {
            return null;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Asset asset = (Asset) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.each_asset, null) : null;
        }

        CardView cardView;
        TextView tvProjectDateTitle, txt_asset_name, txt_asset_id, txt_asset_issue_date;
        View view1;
        LinearLayout imgLayout;
        ImageView imgEdit, imgDelete;

        if (convertView != null) {
            imgEdit = convertView.findViewById(R.id.imgEdit);
            imgDelete = convertView.findViewById(R.id.imgDelete);
            cardView = convertView.findViewById(R.id.cardView);
            txt_asset_name = convertView.findViewById(R.id.txt_asset_name);
            tvProjectDateTitle = convertView.findViewById(R.id.tvProjectDateTitle);
            txt_asset_id = convertView.findViewById(R.id.txt_asset_id);
            txt_asset_issue_date = convertView.findViewById(R.id.txt_asset_issue_date);
            view1 = convertView.findViewById(R.id.view1);
            imgLayout = convertView.findViewById(R.id.imgLayout);

            cardView.setOnClickListener(view -> {
                if (asset.getAllocationStatus().equalsIgnoreCase("Requested")
                        && User.getCurrentUser(_context).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")) {
                    Intent intent = new Intent(_context, AssetAllocation_Activity.class);
                    intent.putExtra("Assets", asset);
                    _context.startActivity(intent);
                } else if (asset.getAllocationStatus().equalsIgnoreCase("Allocated")) {
                    Intent intent = new Intent(_context, AssetApprovalActivity.class);
                    intent.putExtra("Assets", asset);
                    _context.startActivity(intent);
                } else if (asset.getAllocationStatus().equalsIgnoreCase("Accepted")) {
                    if (User.getCurrentUser(_context).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")) {
                        Intent intent = new Intent(_context, AssetApprovalActivity.class);
                        intent.putExtra("Assets", asset);
                        _context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(_context, AssetAllocation_Activity.class);
                        intent.putExtra("Assets", asset);
                        _context.startActivity(intent);
                    }
                } else if (asset.getAllocationStatus().equalsIgnoreCase("Rejected")) {
                    Intent intent = new Intent(_context, AssetApprovalActivity.class);
                    intent.putExtra("Assets", asset);
                    _context.startActivity(intent);
                } else if (asset.getAllocationStatus().equalsIgnoreCase("Released")) {
                    Intent intent = new Intent(_context, AssetApprovalActivity.class);
                    intent.putExtra("Assets", asset);
                    _context.startActivity(intent);
                }
            });

            imgDelete.setOnClickListener(view -> {
                if (_activity != null)
                    showLogoutPopUp(asset);
            });

            imgEdit.setOnClickListener(view -> {
                if (_activity != null) {
                    _activity.editExpense(asset);
                }

            });

            if (asset.getAssetModel().equalsIgnoreCase("null")) {
                tvProjectDateTitle.setVisibility(View.GONE);
                txt_asset_id.setVisibility(View.GONE);
            } else {
                txt_asset_id.setText(asset.getCode());
                txt_asset_id.setVisibility(View.VISIBLE);
                tvProjectDateTitle.setVisibility(View.VISIBLE);
            }

            txt_asset_issue_date.setText(asset.getExpectedIssueDate());
            txt_asset_name.setText(asset.getUsername());

            if (asset.getAllocationStatus().equalsIgnoreCase("Requested")) {
                view1.setBackgroundColor(_context.getResources().getColor(R.color.purple));
            } else if (asset.getAllocationStatus().equalsIgnoreCase("Accepted")) {
                view1.setBackgroundColor(_context.getResources().getColor(R.color.green));
            } else if (asset.getAllocationStatus().equalsIgnoreCase("Allocated")) {
                view1.setBackgroundColor(_context.getResources().getColor(R.color.orrange2));
            } else if (asset.getAllocationStatus().equalsIgnoreCase("Rejected")) {
                view1.setBackgroundColor(_context.getResources().getColor(R.color.red));
            } else if (asset.getAllocationStatus().equalsIgnoreCase("Released")) {
                view1.setBackgroundColor(_context.getResources().getColor(R.color.blue));
            }

            if (asset.getAllocationStatus().equalsIgnoreCase("Requested")
                    && !(User.getCurrentUser(_context).getMvUser().getRoll().equalsIgnoreCase("Asset Manager"))) {
                imgLayout.setVisibility(View.VISIBLE);
            } else {
                imgLayout.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    private void showLogoutPopUp(Asset asset) {
        final AlertDialog alertDialog = new AlertDialog.Builder(_context).create();

        // Setting Dialog Title
        alertDialog.setTitle(_context.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(_context.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(_context.getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            // Write your code here to execute after dialog closed
          /*  listOfWrongQuestions.add(mPosition);
            prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
        });
        // Setting OK Button
        alertDialog.setButton(_context.getString(android.R.string.ok), (dialog, which) -> _activity.deleteExpense(asset));

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Asset> assets = this._listDataChild.get(this._listDataHeader.get(groupPosition));
        if (assets != null) {
            return assets.size();
        } else {
            return 0;
        }
    }


    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.list_group, null) : null;
        }

        if (convertView != null) {

            ImageView imgGroup = convertView.findViewById(R.id.imgGroup);
            if (isExpanded) {
                imgGroup.setImageResource(R.drawable.downarrow);
            } else {
                imgGroup.setImageResource(R.drawable.rightarrow);
            }

            String headerTitle = (String) getGroup(groupPosition);
            TextView txtName = convertView.findViewById(R.id.txtName);
            txtName.setText(headerTitle);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
