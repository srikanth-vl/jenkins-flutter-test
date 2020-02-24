package com.vassar.unifiedapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.TableRow;
import com.vassar.unifiedapp.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddendumRecyclerViewAdapter extends RecyclerView.Adapter<AddendumRecyclerViewAdapter.AddendumRecyclerViewHolder> {

    TableRow mTableRow;
    Map<Integer, Map<String, String>> jsonArrayMap =  new HashMap<>();
    Context mContext ;
    String mParentKey;
    LayoutInflater mInflater;
    Map<String, String> mAdditionalInfo = new HashMap<>();

    public AddendumRecyclerViewAdapter(TableRow tableRow, String parentKey, Map<Integer,Map<String, String>> jsonMap, Context context, Map<String, String> additionalInfo, LayoutInflater inflater) {
        mTableRow = tableRow;
        mParentKey = parentKey;
        mContext = context;
        mAdditionalInfo = additionalInfo;
        jsonArrayMap =  jsonMap;
        mInflater = inflater;

        // TODO : Get map from JsonArray
    }

    @NonNull
    @Override
    public AddendumRecyclerViewAdapter.AddendumRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = ((LayoutInflater) UAAppContext.getInstance().getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.addendum_field_recycler_view_holder, parent, false);
        return new AddendumRecyclerViewAdapter.AddendumRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddendumRecyclerViewAdapter.AddendumRecyclerViewHolder holder, int position) {
        Map<String, String> keyToValueMap = jsonArrayMap.get(position);

        // Add components to LinearLayout
        if (mTableRow.mRowComponents != null &&
                mTableRow.mRowComponents.size() > 0) {

            for (FormField rowComponent : mTableRow.mRowComponents) {

                if(rowComponent == null) {
                    continue;
                }

                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams
                        (0, ViewGroup.LayoutParams.MATCH_PARENT);

                if (rowComponent.mWeight > 0) {
                    cellParams.weight = rowComponent.mWeight;
                } else {
                    cellParams.weight = 1;
                }

                // Iterate through row components
                switch (rowComponent.mUiType) {

                    case Constants.TABULAR_FORM_EDITTEXT_FIELD :

                        EditText editText = new EditText(mContext);

                        editText.setLayoutParams(cellParams);

                        editText.setBackgroundResource(R.drawable.tabular_edittext_background);

                        if (rowComponent.mLabel != null && !rowComponent.mLabel.isEmpty()) {
                            editText.setHint(rowComponent.mLabel);
                        }

                        switch (rowComponent.mDatatype) {
                            case "int":
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                                break;
                            case "double":
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                break;
                            case "email":
                                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                                break;
                            case "string":
                                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                                break;
                            case "multiplelines":
                                editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                                break;
                            case "password":
                                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                break;
                        }

                        String value = keyToValueMap.get(rowComponent.mIdentifier);

                        if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
                            editText.setText(value);
                        }

                        if (rowComponent.mEditable) {
                            editText.setEnabled(true);
                        } else {
                            editText.setEnabled(false);
                        }

                        holder.mLinearLayout.addView(editText);
                        break;

                    default:
                        break;
                }
            }

            if (mTableRow.mFilteringKey != null && !mTableRow.mFilteringKey.isEmpty()) {
                // Filtering key present. Only make those rows visible, that have the filtering key in their JSON
                String filteringKey = mTableRow.mFilteringKey;

                List<String> keyElements = new ArrayList<>();
                keyElements.addAll(Arrays.asList(filteringKey.split("\\$")));

                if (keyElements.size() > 0) {

                    String additionalInfoKey = keyElements.get(keyElements.size() - 1);

                    String filterValue = mAdditionalInfo != null ? mAdditionalInfo.get(additionalInfoKey) : null;

                    String cellValue = keyToValueMap.get(filteringKey);

                    //  TODO :: add value to map
//                    ((TabularFormActivity) mContext).addToUserEnteredValuesMap(mParentKey,
//                            finalK, filteringKey, cellValue);

                    if (cellValue != null && !cellValue.isEmpty() &&
                            filterValue != null && !filterValue.isEmpty() &&
                            filterValue.equalsIgnoreCase(cellValue)) {

                    } else {
                        holder.mRootLayout.setVisibility(View.GONE);
                    }
                }

            }

        }

    }

    @Override
    public int getItemCount() {
        return jsonArrayMap.size();
    }

    public class AddendumRecyclerViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout mRootLayout;
        public LinearLayout mLinearLayout;
        public TextView mTextButton;

        public AddendumRecyclerViewHolder(View itemView) {
            super(itemView);
            mRootLayout = itemView.findViewById(R.id.addendum_row_recycler_view_root);
            mLinearLayout = itemView.findViewById(R.id.addendum_recycler_view_linear_layout);
            mTextButton = itemView.findViewById(R.id.addendum_row_recycler_view_text_button);
        }
    }
}