package rahulramkumar.com.gpacalculator;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rahulramkumar.com.gpacalculator.MainActivity;

/**
 * FFEE5A6D
 * Created by Rahul Ramkumar on 25-03-2018.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayAdapter<String> mCourseAdapter;
    private ArrayAdapter<String> mGradeLetterAdapter;
    private HashMap<Integer, String>  mCourseInputs;
    private HashMap<Integer, String> mGradeInputs;
    private int mItemCount;

    public RecyclerAdapter(ArrayAdapter<String> courseAdapter, ArrayAdapter<String> gradeLetterAdapter, int itemCount) {
        mCourseAdapter = courseAdapter;
        mGradeLetterAdapter = gradeLetterAdapter;
        mItemCount = itemCount;
        mCourseInputs = new HashMap<>();
        mGradeInputs = new HashMap<>();
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.single_course_input;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
        holder.bind(mCourseAdapter, mGradeLetterAdapter, position);

    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public HashMap<Integer, String> getCourseInputs() {
        return mCourseInputs;
    }

    public HashMap<Integer, String> getGradeInputs() {
        return mGradeInputs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public AutoCompleteTextView courseInput;
        public Spinner gradeInput;


        public ViewHolder(View itemView) {
            super(itemView);
            courseInput = itemView.findViewById(R.id.course_input);
            gradeInput= itemView.findViewById(R.id.grade_spinner);
        }

        public void bind (ArrayAdapter<String> courseAdapter, ArrayAdapter<String> gradeLetterAdapter, final int positionInRecyclerView) {
            courseInput.setAdapter(courseAdapter);
            courseInput.setThreshold(1);
            gradeInput.setAdapter(gradeLetterAdapter);

            courseInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mCourseInputs.put(positionInRecyclerView, s.toString());
                }
            });

            gradeInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    mGradeInputs.put(positionInRecyclerView, selectedItem);
                }
                public void onNothingSelected(AdapterView<?> parent)
                {

                }
            });
        }
    }
}


