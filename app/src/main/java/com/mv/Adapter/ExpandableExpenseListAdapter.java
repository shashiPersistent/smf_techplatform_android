package com.mv.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.ExpenseListActivity;
import com.mv.Model.Expense;
import com.mv.R;
import com.mv.Utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableExpenseListAdapter extends BaseExpandableListAdapter {

    private ExpenseListActivity _context;
    private List<String> _listDataHeader; // header titles
    private HashMap<String, ArrayList<Expense>> _listDataChild;
    private ExpenseListActivity _activity;

    public ExpandableExpenseListAdapter(Activity context, ArrayList<String> listDataHeader,
                                        HashMap<String, ArrayList<Expense>> listChildData) {

        this._context = (ExpenseListActivity) context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._activity = (ExpenseListActivity) context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        ArrayList<Expense> expenses = this._listDataChild.get(this._listDataHeader.get(groupPosition));
        if (expenses != null && !expenses.isEmpty()) {
            return expenses.get(childPosititon);
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

        final Expense expense = (Expense) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater != null ? layoutInflater.inflate(R.layout.each_expense, null) : null;
        }

        TextView tvProjectName, tvDateName, tvNoOfPeopleName, tvNoOfPeopleTitle;
        ImageView imgEdit, imgDelete;
        View view;
        RelativeLayout textLayout;

        if (convertView != null) {
            imgEdit = convertView.findViewById(R.id.imgEdit);
            imgDelete = convertView.findViewById(R.id.imgDelete);
            tvProjectName = convertView.findViewById(R.id.tvProjectName);
            tvDateName = convertView.findViewById(R.id.tvDateName);
            tvNoOfPeopleName = convertView.findViewById(R.id.tvNoOfPeopleName);
            tvNoOfPeopleTitle = convertView.findViewById(R.id.tvNoOfPeopleTitle);
            view = convertView.findViewById(R.id.view1);
            textLayout = convertView.findViewById(R.id.textLayout);

            if (groupPosition == 1 || groupPosition == 2) {
                imgEdit.setVisibility(View.GONE);
                imgDelete.setVisibility(View.GONE);
            }

            // hiding views for team mgmt section
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                imgEdit.setVisibility(View.GONE);
                imgDelete.setVisibility(View.GONE);
            }

            textLayout.setOnClickListener(view13 -> {
                if (_context instanceof ExpenseListActivity)
                    _activity.editExpense(expense);
            });

            view.setVisibility(View.GONE);

            imgEdit.setImageResource(R.drawable.ic_form);
            imgEdit.setOnClickListener(view12 -> {
                if (_context instanceof ExpenseListActivity)
                    _activity.editExpense(expense);
            });

            imgDelete.setImageResource(R.drawable.form_delete);
            imgDelete.setOnClickListener(view1 -> {
                if (_context instanceof ExpenseListActivity)
                    showLogoutPopUp(expense);
            });

            tvProjectName.setText(expense.getPartuculars());
            tvDateName.setText(expense.getDate());

            if (expense.getStatus().equals("Approved")) {
                tvNoOfPeopleTitle.setText("Approved Amount: ");
                tvNoOfPeopleName.setText(String.format("₹ %s", expense.getApproved_Amount__c()));
            } else {
                tvNoOfPeopleTitle.setText("Amount: ");
                tvNoOfPeopleName.setText(String.format("₹ %s", expense.getAmount()));
            }
        }

        return convertView;
    }

    @SuppressWarnings("deprecation")
    private void showLogoutPopUp(Expense expense) {
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
        alertDialog.setButton(_context.getString(android.R.string.ok), (dialog, which) -> _activity.deleteExpense(expense));
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Expense> expenses = this._listDataChild.get(this._listDataHeader.get(groupPosition));
        if (expenses != null) {
            return expenses.size();
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater != null ? layoutInflater.inflate(R.layout.list_group, null) : null;
        }

        if (convertView != null) {
            ImageView imgGroup = convertView.findViewById(R.id.imgGroup);

            if (isExpanded) {
                imgGroup.setImageResource(R.drawable.downarrow);
            } else {
                imgGroup.setImageResource(R.drawable.rightarrow);
            }

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